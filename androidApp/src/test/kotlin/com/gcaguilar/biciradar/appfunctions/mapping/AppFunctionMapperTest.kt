package com.gcaguilar.biciradar.appfunctions.mapping

import android.os.Bundle
import com.gcaguilar.biciradar.appfunctions.parameters.StationPreference
import com.gcaguilar.biciradar.core.AssistantAction
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppFunctionMapperTest {

    @Test
    fun `toAssistantAction returns NearestStation for ANY preference`() {
        // Given
        val bundle = Bundle().apply {
            putString("preference", StationPreference.ANY.name)
        }

        // When
        val result = AppFunctionMapper.toAssistantAction("findNearbyStation", bundle)

        // Then
        assertTrue(result is AssistantAction.NearestStation)
    }

    @Test
    fun `toAssistantAction returns NearestStationWithBikes for WITH_BIKES preference`() {
        // Given
        val bundle = Bundle().apply {
            putString("preference", StationPreference.WITH_BIKES.name)
        }

        // When
        val result = AppFunctionMapper.toAssistantAction("findNearbyStation", bundle)

        // Then
        assertTrue(result is AssistantAction.NearestStationWithBikes)
    }

    @Test
    fun `toAssistantAction returns StationStatus with correct id`() {
        // Given
        val stationId = "station123"
        val bundle = Bundle().apply {
            putString("stationId", stationId)
        }

        // When
        val result = AppFunctionMapper.toAssistantAction("getStationStatus", bundle)

        // Then
        assertTrue(result is AssistantAction.StationStatus)
        assertEquals(stationId, (result as AssistantAction.StationStatus).stationId)
    }

    @Test
    fun `toAssistantAction returns null for unknown function`() {
        // When
        val result = AppFunctionMapper.toAssistantAction("unknownFunction", Bundle())

        // Then
        assertNull(result)
    }

    @Test
    fun `fromAssistantAction returns findNearbyStation for NearestStation`() {
        // Given
        val action = AssistantAction.NearestStation

        // When
        val (functionId, bundle) = AppFunctionMapper.fromAssistantAction(action)!!

        // Then
        assertEquals("findNearbyStation", functionId)
        assertEquals(StationPreference.ANY.name, bundle.getString("preference"))
    }

    @Test
    fun `fromAssistantAction returns getStationStatus for StationStatus`() {
        // Given
        val stationId = "station456"
        val action = AssistantAction.StationStatus(stationId)

        // When
        val (functionId, bundle) = AppFunctionMapper.fromAssistantAction(action)!!

        // Then
        assertEquals("getStationStatus", functionId)
        assertEquals(stationId, bundle.getString("stationId"))
    }

    @Test
    fun `fromAssistantAction returns null for unsupported action`() {
        // Given
        val action = AssistantAction.RouteToStation("station123")

        // When
        val result = AppFunctionMapper.fromAssistantAction(action)

        // Then
        assertNull(result)
    }

    @Test
    fun `bidirectional conversion preserves data`() {
        // Given
        val originalAction = AssistantAction.NearestStationWithSlots

        // When
        val (functionId, bundle) = AppFunctionMapper.fromAssistantAction(originalAction)!!
        val convertedAction = AppFunctionMapper.toAssistantAction(functionId, bundle)

        // Then
        assertTrue(convertedAction is AssistantAction.NearestStationWithSlots)
    }
}
