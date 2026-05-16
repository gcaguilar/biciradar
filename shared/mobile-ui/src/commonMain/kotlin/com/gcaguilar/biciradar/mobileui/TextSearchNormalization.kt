package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.SearchTextNormalizer

internal fun String.normalizedForSearch(): String = SearchTextNormalizer.normalizePanEuropean(this)
