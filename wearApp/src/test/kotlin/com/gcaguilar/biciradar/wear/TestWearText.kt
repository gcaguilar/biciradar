package com.gcaguilar.biciradar.wear

import java.io.File

/**
 * [WearText] de pruebas que lee los textos reales de `res/values/strings.xml` (español),
 * así los tests comprueban el contenido localizado sin necesitar Robolectric.
 */
internal val testWearText: WearText by lazy {
  val strings =
    Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
      .findAll(File("src/main/res/values/strings.xml").readText())
      .associate { match ->
        match.groupValues[1] to
          match.groupValues[2]
            .replace("\\'", "'")
            .replace("\\n", "\n")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
      }
  val namesById =
    R.string::class.java.fields
      .filter { it.type == Int::class.javaPrimitiveType }
      .associate { it.getInt(null) to it.name }

  WearText { id, args ->
    val name = checkNotNull(namesById[id]) { "Recurso de texto desconocido: $id" }
    val template = checkNotNull(strings[name]) { "Falta $name en values/strings.xml" }
    if (args.isEmpty()) template else String.format(template, *args)
  }
}
