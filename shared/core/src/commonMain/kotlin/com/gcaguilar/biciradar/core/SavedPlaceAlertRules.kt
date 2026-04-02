package com.gcaguilar.biciradar.core

fun findSavedPlaceAlertRule(
  rules: List<SavedPlaceAlertRule>,
  target: SavedPlaceAlertTarget,
): SavedPlaceAlertRule? = rules.firstOrNull { rule ->
  rule.target.identityKey() == target.identityKey()
}
