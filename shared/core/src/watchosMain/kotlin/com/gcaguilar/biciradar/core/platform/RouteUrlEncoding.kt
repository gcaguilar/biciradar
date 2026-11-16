package com.gcaguilar.biciradar.core.platform

private const val HEX_DIGITS = "0123456789ABCDEF"

/**
 * Percent-encodes a value for use in a URL query. `NSURL.URLWithString` returns null for names
 * with spaces, accents or reserved characters, which silently aborted the route launch.
 */
internal fun String.percentEncodedQueryValue(): String =
  buildString {
    this@percentEncodedQueryValue.encodeToByteArray().forEach { byte ->
      val code = byte.toInt() and 0xFF
      val char = code.toChar()
      if (code < 0x80 && (char.isLetterOrDigit() || char in "-._~")) {
        append(char)
      } else {
        append('%')
        append(HEX_DIGITS[code shr 4])
        append(HEX_DIGITS[code and 0x0F])
      }
    }
  }
