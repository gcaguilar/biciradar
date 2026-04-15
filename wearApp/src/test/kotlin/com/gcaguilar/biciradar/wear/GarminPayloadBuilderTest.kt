package com.gcaguilar.biciradar.wear

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GarminPayloadBuilderTest {

    private val builder = GarminPayloadBuilder()

    @Test
    fun `build with stations returns correct payload`() {
        val stations = listOf(
            Station(
                id = "1",
                name = "Plaza España",
                address = "Calle Mayor 1",
                location = GeoPoint(41.65, -0.88),
                bikesAvailable = 5,
                slotsFree = 10,
                distanceMeters = 150,
            ),
            Station(
                id = "2",
                name = "Estación Cercana",
                address = "Calle Falsa 2",
                location = GeoPoint(41.66, -0.87),
                bikesAvailable = 3,
                slotsFree = 7,
                distanceMeters = 300,
            )
        )

        val payload = builder.build(stations)

        assertNotNull(payload.nearest)
        assertEquals("Plaza España", payload.nearest!!.name)
        assertEquals(5, payload.nearest!!.bikes)
        assertEquals(150, payload.nearest!!.distance)
        assertEquals(1, payload.backup.size)
        assertEquals("Estación Cercana", payload.backup[0].name)
        assertNotNull(payload.timestamp)
    }

    @Test
    fun `build with empty stations returns null nearest`() {
        val stations = emptyList<Station>()
        val payload = builder.build(stations)

        assertNull(payload.nearest)
        assertEquals(0, payload.backup.size)
    }

    @Test
    fun `build truncates long names`() {
        val longName = "Estación de la Plaza del Pilar con nombre muy largo"
        val stations = listOf(
            Station(
                id = "1",
                name = longName,
                address = "Calle Falsa",
                location = GeoPoint(41.65, -0.88),
                bikesAvailable = 5,
                slotsFree = 10,
                distanceMeters = 150,
            )
        )

        val payload = builder.build(stations)

        assertEquals(20, payload.nearest!!.name.length)
        assertEquals("…", payload.nearest!!.name.takeLast(1))
    }

    @Test
    fun `serializeToJson creates valid JSON`() {
        val stations = listOf(
            Station(
                id = "1",
                name = "Test Station",
                address = "Test Address",
                location = GeoPoint(41.65, -0.88),
                bikesAvailable = 5,
                slotsFree = 10,
                distanceMeters = 150,
            )
        )
        val payload = builder.build(stations)
        val json = builder.serializeToJson(payload)

        assert(json.contains("\"nearest\""))
        assert(json.contains("\"name\":\"Test Station\""))
        assert(json.contains("\"bikes\":5"))
        assert(json.contains("\"distance\":150"))
        assert(json.contains("\"backup\":[]"))
        assert(json.contains("\"timestamp\":"))
    }

    @Test
    fun `build with many stations only includes 4 backup`() {
        val stations = (1..10).map { i ->
            Station(
                id = "$i",
                name = "Station $i",
                address = "Address $i",
                location = GeoPoint(41.65 + i * 0.001, -0.88),
                bikesAvailable = i,
                slotsFree = 10 - i,
                distanceMeters = i * 100,
            )
        }

        val payload = builder.build(stations)

        assertEquals("1", payload.nearest!!.id)
        assertEquals(4, payload.backup.size)
        assertEquals("2", payload.backup[0].id)
        assertEquals("5", payload.backup[3].id)
    }

    @Test
    fun `build handles ebikes`() {
        val stations = listOf(
            Station(
                id = "1",
                name = "Test",
                address = "Address",
                location = GeoPoint(41.65, -0.88),
                bikesAvailable = 8,
                slotsFree = 5,
                distanceMeters = 100,
                ebikesAvailable = 3,
                regularBikesAvailable = 5,
            )
        )

        val payload = builder.build(stations)

        assertEquals(3, payload.nearest!!.ebikes)
    }
}
