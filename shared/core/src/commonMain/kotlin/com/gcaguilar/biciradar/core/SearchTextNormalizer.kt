package com.gcaguilar.biciradar.core

/**
 * Módulo profundo de normalización de texto para búsqueda.
 *
 * Unifica la normalización de texto que antes estaba duplicada en tres lugares:
 * - `normalizeStationSearchQuery` en StationMatching.kt (acentos ES, abreviaturas, stopwords)
 * - `normalizedForSearch` en TextSearchNormalization.kt (acentos pan-europeos)
 * - `normalizeShortcutSearchText` en Swift (Unicode folding)
 *
 * La Interfaz ofrece dos modos:
 * - `normalizeStationSearchQuery`: normalización de estaciones con abreviaturas españolas y stopwords
 * - `normalizePanEuropean`: normalización ligera de acentos para búsqueda de ciudades y atajos
 */
object SearchTextNormalizer {
  private val STATION_STOPWORDS =
    setOf(
      "de",
      "del",
      "la",
      "las",
      "el",
      "los",
    )

  /**
   * Normaliza texto para búsqueda de estaciones.
   * Incluye: lowercased, acentos ES, abreviaturas (c/→calle, pza→plaza, avda→avenida),
   * stopwords, puntuación, colapso de espacios.
   */
  fun normalizeStationSearchQuery(value: String?): String =
    value
      .orEmpty()
      .trim()
      .lowercase()
      .replace("á", "a")
      .replace("à", "a")
      .replace("ä", "a")
      .replace("é", "e")
      .replace("è", "e")
      .replace("ë", "e")
      .replace("í", "i")
      .replace("ì", "i")
      .replace("ï", "i")
      .replace("ó", "o")
      .replace("ò", "o")
      .replace("ö", "o")
      .replace("ú", "u")
      .replace("ù", "u")
      .replace("ü", "u")
      .replace("ñ", "n")
      .replace("\\bc/\\s*".toRegex(), " calle ")
      .replace("\\bpza\\.?\\b".toRegex(), " plaza ")
      .replace("\\bavda\\.?\\b".toRegex(), " avenida ")
      .replace("\\bav\\.?\\b".toRegex(), " avenida ")
      .replace("[^a-z0-9 ]".toRegex(), " ")
      .replace("\\s+".toRegex(), " ")
      .split(' ')
      .filter { token -> token.isNotBlank() && token !in STATION_STOPWORDS }
      .joinToString(" ")

  /**
   * Normaliza texto para búsqueda de ciudades y atajos.
   * Incluye: lowercased, acentos pan-europeos (30+ chars), sin abreviaturas ni stopwords.
   * Equivalente a `normalizedForSearch` de TextSearchNormalization.kt.
   */
  fun normalizePanEuropean(value: String?): String {
    val input = value ?: return ""
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    return buildString(trimmed.length) {
      trimmed.lowercase().forEach { char ->
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
  }
}
