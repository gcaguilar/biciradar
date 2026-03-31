package com.gcaguilar.biciradar.core

/**
 * Semantic-ish version compare: splits on `.`, compares numeric segments, non-numeric suffixes sort after digits.
 * Returns negative if [a] < [b], zero if equal, positive if [a] > [b].
 */
fun compareAppVersionStrings(a: String, b: String): Int {
  val pa = parseVersion(a)
  val pb = parseVersion(b)
  val maxLen = maxOf(pa.size, pb.size)
  for (i in 0 until maxLen) {
    val cmp = compareVersionPart(pa.getOrElse(i) { VersionPart(0, "") }, pb.getOrElse(i) { VersionPart(0, "") })
    if (cmp != 0) return cmp
  }
  return 0
}

/**
 * Extracts a catalog-compatible semantic version from app version strings like:
 * - "0.19.0"
 * - "0.19.0 (29568090)"
 * - "v0.19.0-beta+12"
 */
fun normalizeAppVersionForCatalog(version: String?): String? {
  val raw = version?.trim().orEmpty()
  if (raw.isBlank()) return null
  val match = Regex("""(\d+\.\d+\.\d+)""").find(raw)?.value
  return match ?: raw.takeIf { it.any(Char::isDigit) }
}

private data class VersionPart(val number: Int, val suffix: String)

private fun parseVersion(v: String): List<VersionPart> {
  val trimmed = v.trim().ifBlank { "0" }
  return trimmed.split('.').map { segment ->
    val digits = segment.takeWhile { it.isDigit() }
    val rest = segment.drop(digits.length)
    val num = digits.toIntOrNull() ?: 0
    VersionPart(num, rest.lowercase())
  }
}

private fun compareVersionPart(a: VersionPart, b: VersionPart): Int {
  val n = a.number.compareTo(b.number)
  if (n != 0) return n
  return a.suffix.compareTo(b.suffix)
}

/**
 * Changelog for [currentAppVersion] should show once if there is a catalog entry for that exact version
 * and it is newer than [lastSeenChangelogAppVersion] (or lastSeen is null before baseline is applied — caller should baseline first).
 */
fun pendingChangelogVersion(
  currentAppVersion: String,
  lastSeenChangelogAppVersion: String?,
  catalogVersions: Set<String>,
): String? {
  val normalizedCurrent = normalizeAppVersionForCatalog(currentAppVersion) ?: return null
  val normalizedLastSeen = normalizeAppVersionForCatalog(lastSeenChangelogAppVersion) ?: return null
  if (!catalogVersions.contains(normalizedCurrent)) return null
  if (compareAppVersionStrings(normalizedCurrent, normalizedLastSeen) <= 0) return null
  return normalizedCurrent
}
