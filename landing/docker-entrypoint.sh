#!/bin/sh
set -e
if [ ! -x /usr/bin/curl ]; then
  echo "biciradar-landing: /usr/bin/curl is missing; Coolify healthchecks need curl in the final image." >&2
  exit 1
fi
exec "$@"
