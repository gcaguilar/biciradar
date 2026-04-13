package com.gcaguilar.biciradar.core

fun systemCategoryLabel(categoryId: String): String =
  when (categoryId) {
    FavoriteCategoryIds.FAVORITE -> FavoriteCategoryIds.FAVORITE
    FavoriteCategoryIds.HOME -> FavoriteCategoryIds.HOME
    FavoriteCategoryIds.WORK -> FavoriteCategoryIds.WORK
    else -> categoryId
  }

fun FavoriteCategory.resolvedLabel(): String =
  if (isSystem) {
    systemCategoryLabel(id)
  } else {
    label
  }
