#!/usr/bin/env bash

set -euo pipefail

PLATFORM="${1:-}"

if [[ -z "$PLATFORM" ]]; then
  echo "Usage: $0 <ios|watchos>" >&2
  exit 1
fi

export PLATFORM

SIMCTL_JSON="${SIMCTL_JSON_OVERRIDE:-}"

if [[ -z "$SIMCTL_JSON" ]]; then
  SIMCTL_JSON="$(xcrun simctl list devices available -j 2>/dev/null || true)"
fi

if [[ -z "$SIMCTL_JSON" ]]; then
  echo "Unable to query available simulators from simctl." >&2
  exit 1
fi

printf '%s' "$SIMCTL_JSON" | ruby -rjson -e '
platform = ENV.fetch("PLATFORM")
devices_by_runtime = JSON.parse($stdin.read).fetch("devices")

family, runtime_prefix, name_pattern =
  case platform
  when "ios"
    ["iOS Simulator", "com.apple.CoreSimulator.SimRuntime.iOS", /^iPhone/]
  when "watchos"
    ["watchOS Simulator", "com.apple.CoreSimulator.SimRuntime.watchOS", /Apple Watch/]
  else
    abort("Unsupported platform #{platform.inspect}. Use ios or watchos.")
  end

def runtime_sort_key(runtime_id)
  suffix = runtime_id.sub("com.apple.CoreSimulator.SimRuntime.", "")
  _, version = suffix.split("-", 2)
  version.to_s.split("-").map(&:to_i)
end

candidates = devices_by_runtime.flat_map do |runtime_id, devices|
  next [] unless runtime_id.start_with?(runtime_prefix)

  devices.map do |device|
    next unless device["isAvailable"]
    next unless device["name"]&.match?(name_pattern)

    {
      family: family,
      name: device.fetch("name"),
      runtime_id: runtime_id,
      udid: device.fetch("udid"),
    }
  end.compact
end

abort("No available #{platform} simulator found.") if candidates.empty?

preferred_name_patterns =
  case platform
  when "ios"
    [/^iPhone .*Pro Max$/, /^iPhone .*Pro$/, /^iPhone \d+$/, /^iPhone/]
  when "watchos"
    [/Apple Watch Ultra/, /Apple Watch Series/, /Apple Watch/]
  else
    []
  end

target = candidates.max_by do |candidate|
  preference_index = preferred_name_patterns.find_index { |pattern| candidate[:name].match?(pattern) } || preferred_name_patterns.length
  [runtime_sort_key(candidate[:runtime_id]), -preference_index, candidate[:name]]
end

puts("platform=#{target[:family]},id=#{target[:udid]}")
'
