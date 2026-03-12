package com.gcaguilar.bizizaragoza

import com.gcaguilar.bizizaragoza.mobileui.AssistantLaunchRequest
import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidAssistantShortcutsTest {
  @Test
  fun `shortcutIdFor maps app launch requests to declared shortcuts`() {
    assertEquals(FAVORITE_STATIONS_ACTION, shortcutIdFor(MobileLaunchRequest.Favorites, null))
    assertEquals(NEAREST_STATION_ACTION, shortcutIdFor(MobileLaunchRequest.NearestStation, null))
    assertEquals(
      NEAREST_STATION_WITH_BIKES_ACTION,
      shortcutIdFor(MobileLaunchRequest.NearestStationWithBikes, null),
    )
    assertEquals(
      NEAREST_STATION_WITH_SLOTS_ACTION,
      shortcutIdFor(MobileLaunchRequest.NearestStationWithSlots, null),
    )
    assertEquals(STATION_STATUS_ACTION, shortcutIdFor(MobileLaunchRequest.StationStatus, null))
    assertEquals(
      ROUTE_TO_STATION_ACTION,
      shortcutIdFor(MobileLaunchRequest.RouteToStation(stationId = "48"), null),
    )
    assertEquals(
      SHOW_STATION_ACTION,
      shortcutIdFor(MobileLaunchRequest.ShowStation(stationId = "48"), null),
    )
  }

  @Test
  fun `shortcutIdFor maps assistant requests to declared shortcuts`() {
    assertEquals(
      STATION_STATUS_ACTION,
      shortcutIdFor(null, AssistantLaunchRequest.StationStatus(stationQuery = "Plaza Espana")),
    )
    assertEquals(
      STATION_BIKE_COUNT_ACTION,
      shortcutIdFor(null, AssistantLaunchRequest.StationBikeCount(stationQuery = "48")),
    )
    assertEquals(
      STATION_SLOT_COUNT_ACTION,
      shortcutIdFor(null, AssistantLaunchRequest.StationSlotCount(stationQuery = "48")),
    )
    assertEquals(
      ROUTE_TO_STATION_ACTION,
      shortcutIdFor(null, AssistantLaunchRequest.RouteToStation(stationQuery = "Plaza Aragon")),
    )
    assertEquals(
      SHOW_STATION_ACTION,
      shortcutIdFor(null, AssistantLaunchRequest.SearchStation("Plaza Espana")),
    )
  }

  @Test
  fun `shortcuts xml stays aligned with reportable shortcut ids`() {
    val xml = parseShortcutsXml()
    val shortcuts = xml.getElementsByTagName("shortcut")
    val shortcutIds = buildSet {
      for (index in 0 until shortcuts.length) {
        val shortcut = shortcuts.item(index)
        val shortcutId = shortcut.attributes?.getNamedItem("android:shortcutId")?.nodeValue
          ?: shortcut.attributes?.getNamedItem("shortcutId")?.nodeValue
          ?: continue
        add(shortcutId)
      }
    }

    val expectedIds = setOf(
      FAVORITE_STATIONS_ACTION,
      NEAREST_STATION_ACTION,
      NEAREST_STATION_WITH_BIKES_ACTION,
      NEAREST_STATION_WITH_SLOTS_ACTION,
      STATION_STATUS_ACTION,
      STATION_BIKE_COUNT_ACTION,
      STATION_SLOT_COUNT_ACTION,
      ROUTE_TO_STATION_ACTION,
      SHOW_STATION_ACTION,
    )

    assertEquals(expectedIds, shortcutIds)
    assertTrue(OPEN_ASSISTANT_ACTION !in shortcutIds)
  }

  @Test
  fun `show station shortcut is bound to get thing capability`() {
    val xml = parseShortcutsXml()
    val shortcuts = xml.getElementsByTagName("shortcut")
    var foundShortcut = false
    var foundBinding = false
    for (index in 0 until shortcuts.length) {
      val shortcut = shortcuts.item(index)
      val shortcutId = shortcut.attributes?.getNamedItem("android:shortcutId")?.nodeValue
        ?: shortcut.attributes?.getNamedItem("shortcutId")?.nodeValue
      if (shortcutId != SHOW_STATION_ACTION) continue
      foundShortcut = true
      val children = shortcut.childNodes
      for (childIndex in 0 until children.length) {
        val child = children.item(childIndex)
        if (child.nodeName != "capability-binding") continue
        val capabilityKey = child.attributes?.getNamedItem("android:key")?.nodeValue
          ?: child.attributes?.getNamedItem("key")?.nodeValue
        if (capabilityKey == "actions.intent.GET_THING") {
          foundBinding = true
        }
      }
    }

    assertTrue("Expected manifest shortcut for show_station", foundShortcut)
    assertTrue("Expected show_station to bind actions.intent.GET_THING", foundBinding)
  }

  private fun parseShortcutsXml() = DocumentBuilderFactory.newInstance()
    .newDocumentBuilder()
    .parse(shortcutsXmlFile())

  private fun shortcutsXmlFile(): File {
    val candidates = listOf(
      File("src/androidMain/res/xml/shortcuts.xml"),
      File("androidApp/src/androidMain/res/xml/shortcuts.xml"),
    )
    return candidates.firstOrNull(File::exists)
      ?: error("Unable to locate shortcuts.xml in $candidates")
  }
}
