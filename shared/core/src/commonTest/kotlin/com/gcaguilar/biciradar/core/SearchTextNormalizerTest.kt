package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchTextNormalizerTest {
  @Test
  fun `normalize returns empty string for null input`() {
    assertEquals("", SearchTextNormalizer.normalizeStationSearchQuery(null))
  }

  @Test
  fun `normalize returns empty string for blank input`() {
    assertEquals("", SearchTextNormalizer.normalizeStationSearchQuery("   "))
  }

  @Test
  fun `normalize trims leading and trailing whitespace`() {
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("  plaza españa  "))
  }

  @Test
  fun `normalize lowercases input`() {
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("Plaza ESPAÑA"))
  }

  @Test
  fun `normalize strips Spanish accented vowels`() {
    assertEquals("a a a a a a a a a", SearchTextNormalizer.normalizeStationSearchQuery("á a a a a a a a a"))
    assertEquals("e e e e e e e e e", SearchTextNormalizer.normalizeStationSearchQuery("é e e e e e e e e"))
    assertEquals("i i i i n n n n n n", SearchTextNormalizer.normalizeStationSearchQuery("í i i i ñ ñ ñ ñ ñ ñ"))
    assertEquals("o o o o o o o o o o", SearchTextNormalizer.normalizeStationSearchQuery("ó o o o o o o o o o"))
    assertEquals("u u u u u u u u u u", SearchTextNormalizer.normalizeStationSearchQuery("ú u u u u u u u u u"))
  }

  @Test
  fun `normalize converts ñ to n`() {
    assertEquals("n", SearchTextNormalizer.normalizeStationSearchQuery("ñ"))
    assertEquals("plaza n", SearchTextNormalizer.normalizeStationSearchQuery("plaza ñ"))
  }

  @Test
  fun `normalizeStationSearchQuery expands c slash abbreviation to calle`() {
    assertEquals("calle espana", SearchTextNormalizer.normalizeStationSearchQuery("c/ españa"))
    assertEquals("calle espana", SearchTextNormalizer.normalizeStationSearchQuery("C/ España"))
    assertEquals("calle espana", SearchTextNormalizer.normalizeStationSearchQuery("c/ España"))
  }

  @Test
  fun `normalizeStationSearchQuery expands pza abbreviation to plaza`() {
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("pza. españa"))
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("pza españa"))
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("PZA. ESPAÑA"))
  }

  @Test
  fun `normalizeStationSearchQuery expands avda abbreviation to avenida`() {
    assertEquals("avenida liberalidad", SearchTextNormalizer.normalizeStationSearchQuery("avda. liberalidad"))
    assertEquals("avenida liberalidad", SearchTextNormalizer.normalizeStationSearchQuery("avda liberalidad"))
  }

  @Test
  fun `normalizeStationSearchQuery expands av abbreviation to avenida`() {
    assertEquals("avenida liberalidad", SearchTextNormalizer.normalizeStationSearchQuery("av liberalidad"))
  }

  @Test
  fun `normalize strips punctuation and special characters`() {
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("Plaza España!"))
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("Plaza España."))
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("Plaza España?"))
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("Plaza @ España"))
  }

  @Test
  fun `normalize collapses multiple whitespace`() {
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("plaza   españa"))
    assertEquals("plaza espana delicias", SearchTextNormalizer.normalizeStationSearchQuery("plaza  españa  delicias"))
  }

  @Test
  fun `normalize removes stopwords`() {
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("la plaza españa"))
    assertEquals("plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("de la plaza españa"))
    assertEquals("plaza", SearchTextNormalizer.normalizeStationSearchQuery("el de la plaza"))
    assertEquals("", SearchTextNormalizer.normalizeStationSearchQuery("las de el los"))
  }

  @Test
  fun `normalize preserves numeric tokens`() {
    assertEquals("42", SearchTextNormalizer.normalizeStationSearchQuery("42"))
    assertEquals("42 plaza espana", SearchTextNormalizer.normalizeStationSearchQuery("42 plaza españa"))
  }

  @Test
  fun `normalizeStationSearchQuery handles abbreviation with stopwords together`() {
    assertEquals("plaza liberalidad", SearchTextNormalizer.normalizeStationSearchQuery("pza. de la liberalidad"))
    assertEquals("calle liberalidad", SearchTextNormalizer.normalizeStationSearchQuery("c/ de la liberalidad"))
  }

  @Test
  fun `normalizePanEuropean strips non-ASCII accented characters`() {
    assertEquals("a", SearchTextNormalizer.normalizePanEuropean("á"))
    assertEquals("a", SearchTextNormalizer.normalizePanEuropean("à"))
    assertEquals("a", SearchTextNormalizer.normalizePanEuropean("ä"))
    assertEquals("a", SearchTextNormalizer.normalizePanEuropean("â"))
    assertEquals("a", SearchTextNormalizer.normalizePanEuropean("ã"))
    assertEquals("c", SearchTextNormalizer.normalizePanEuropean("ç"))
    assertEquals("e", SearchTextNormalizer.normalizePanEuropean("é"))
    assertEquals("e", SearchTextNormalizer.normalizePanEuropean("è"))
    assertEquals("e", SearchTextNormalizer.normalizePanEuropean("ë"))
    assertEquals("e", SearchTextNormalizer.normalizePanEuropean("ê"))
    assertEquals("i", SearchTextNormalizer.normalizePanEuropean("í"))
    assertEquals("i", SearchTextNormalizer.normalizePanEuropean("ì"))
    assertEquals("i", SearchTextNormalizer.normalizePanEuropean("ï"))
    assertEquals("i", SearchTextNormalizer.normalizePanEuropean("î"))
    assertEquals("n", SearchTextNormalizer.normalizePanEuropean("ñ"))
    assertEquals("o", SearchTextNormalizer.normalizePanEuropean("ó"))
    assertEquals("o", SearchTextNormalizer.normalizePanEuropean("ò"))
    assertEquals("o", SearchTextNormalizer.normalizePanEuropean("ö"))
    assertEquals("o", SearchTextNormalizer.normalizePanEuropean("ô"))
    assertEquals("o", SearchTextNormalizer.normalizePanEuropean("õ"))
    assertEquals("u", SearchTextNormalizer.normalizePanEuropean("ú"))
    assertEquals("u", SearchTextNormalizer.normalizePanEuropean("ù"))
    assertEquals("u", SearchTextNormalizer.normalizePanEuropean("ü"))
    assertEquals("u", SearchTextNormalizer.normalizePanEuropean("û"))
    assertEquals("y", SearchTextNormalizer.normalizePanEuropean("ý"))
    assertEquals("y", SearchTextNormalizer.normalizePanEuropean("ÿ"))
    assertEquals("y", SearchTextNormalizer.normalizePanEuropean("ŷ"))
  }

  @Test
  fun `normalizePanEuropean preserves basic latin and digits`() {
    assertEquals("abc123", SearchTextNormalizer.normalizePanEuropean("abc123"))
    assertEquals("plaza espana", SearchTextNormalizer.normalizePanEuropean("Plaza España"))
  }

  @Test
  fun `normalizePanEuropean lowercases input`() {
    assertEquals("plaza", SearchTextNormalizer.normalizePanEuropean("PLAZA"))
    assertEquals("plaza espana", SearchTextNormalizer.normalizePanEuropean("Plaza ESPAÑA"))
  }
}
