package com.gcaguilar.biciradar.mobileui

internal fun String.normalizedForSearch(): String =
  buildString(length) {
    this@normalizedForSearch.lowercase().forEach { char ->
      append(
        when (char) {
          'á', 'à', 'ä', 'â', 'ã', 'å', 'ā', 'ă', 'ą' -> 'a'
          'ç', 'ć', 'ĉ', 'ċ', 'č' -> 'c'
          'ď', 'đ' -> 'd'
          'é', 'è', 'ë', 'ê', 'ē', 'ĕ', 'ė', 'ę', 'ě' -> 'e'
          'í', 'ì', 'ï', 'î', 'ĩ', 'ī', 'ĭ', 'į', 'ı' -> 'i'
          'ñ', 'ń', 'ņ', 'ň', 'ŉ' -> 'n'
          'ó', 'ò', 'ö', 'ô', 'õ', 'ō', 'ŏ', 'ő', 'ø' -> 'o'
          'ŕ', 'ŗ', 'ř' -> 'r'
          'ś', 'ŝ', 'ş', 'š' -> 's'
          'ť', 'ţ', 'ŧ' -> 't'
          'ú', 'ù', 'ü', 'û', 'ũ', 'ū', 'ŭ', 'ů', 'ű', 'ų' -> 'u'
          'ý', 'ÿ', 'ŷ' -> 'y'
          'ź', 'ż', 'ž' -> 'z'
          else -> char
        },
      )
    }
  }
