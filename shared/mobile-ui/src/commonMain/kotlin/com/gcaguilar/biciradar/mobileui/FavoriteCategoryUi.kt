package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.FavoriteCategory
import com.gcaguilar.biciradar.core.FavoriteCategoryIds

internal fun favoriteCategoryLabel(
  category: FavoriteCategory,
  homeLabel: String,
  workLabel: String,
  favoriteLabel: String,
): String =
  favoriteCategoryLabel(
    categoryId = category.id,
    fallback = category.label,
    homeLabel = homeLabel,
    workLabel = workLabel,
    favoriteLabel = favoriteLabel,
  )

internal fun favoriteCategoryLabel(
  categoryId: String,
  fallback: String,
  homeLabel: String,
  workLabel: String,
  favoriteLabel: String,
): String =
  when (categoryId) {
    FavoriteCategoryIds.HOME -> homeLabel
    FavoriteCategoryIds.WORK -> workLabel
    FavoriteCategoryIds.FAVORITE -> favoriteLabel
    else -> fallback
  }
