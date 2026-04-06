package com.gcaguilar.biciradar.appfunctions.functions

import app.cash.turbine.test
import com.gcaguilar.biciradar.appfunctions.parameters.FindNearbyStationParams
import com.gcaguilar.biciradar.appfunctions.parameters.StationPreference
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocationProvider
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class FindNearbyStationFunctionTest {

    private lateinit var function: FindNearbyStationFunction
    private val stationsRepository: StationsRepository = mock()
    private val favoritesRepository: FavoritesRepository = mock()
    private val locationProvider: LocationProvider = mock()
    private val stationsStateFlow = MutableStateFlow(StationsState())
    private val favoriteIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    @Before
    fun setup() {
        whenever(stationsRepository.state).thenReturn(stationsStateFlow)
        whenever(favoritesRepository.favoriteIds).thenReturn(favoriteIdsFlow)

        function = FindNearbyStationFunction(
            stationsRepository = stationsRepository,
            favoritesRepository = favoritesRepository,
            locationProvider = locationProvider
        )
    }

    @Test
    fun `execute returns stations sorted by distance`() = runTest {
        // Given
        val userLocation = GeoPoint(40.4168, -3.7038)
        whenever(locationProvider.currentLocation()).thenReturn(userLocation)

        val stations = listOf(
            createStation("1", "Estación 1", 10, 5, 40.4170, -3.7040),
            createStation("2", "Estación 2", 5, 10, 40.4160, -3.7030)
        )
        stationsStateFlow.value = StationsState(stations = stations, isLoading = false)

        // When
        val params = FindNearbyStationParams(preference = StationPreference.ANY)
        val result = function.execute(params)

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0].distance < result[1].distance)
    }

    @Test
    fun `execute filters stations with bikes when preference is WITH_BIKES`() = runTest {
        // Given
        val userLocation = GeoPoint(40.4168, -3.7038)
        whenever(locationProvider.currentLocation()).thenReturn(userLocation)

        val stations = listOf(
            createStation("1", "Con Bicis", 5, 5, 40.4170, -3.7040),
            createStation("2", "Sin Bicis", 0, 10, 40.4160, -3.7030)
        )
        stationsStateFlow.value = StationsState(stations = stations, isLoading = false)

        // When
        val params = FindNearbyStationParams(preference = StationPreference.WITH_BIKES)
        val result = function.execute(params)

        // Then
        assertEquals(1, result.size)
        assertEquals("Con Bicis", result[0].name)
        assertEquals(5, result[0].bikesAvailable)
    }

    @Test
    fun `execute filters stations with slots when preference is WITH_SLOTS`() = runTest {
        // Given
        val userLocation = GeoPoint(40.4168, -3.7038)
        whenever(locationProvider.currentLocation()).thenReturn(userLocation)

        val stations = listOf(
            createStation("1", "Con Plazas", 5, 5, 40.4170, -3.7040),
            createStation("2", "Sin Plazas", 5, 0, 40.4160, -3.7030)
        )
        stationsStateFlow.value = StationsState(stations = stations, isLoading = false)

        // When
        val params = FindNearbyStationParams(preference = StationPreference.WITH_SLOTS)
        val result = function.execute(params)

        // Then
        assertEquals(1, result.size)
        assertEquals("Con Plazas", result[0].name)
        assertEquals(5, result[0].slotsAvailable)
    }

    @Test
    fun `execute marks stations as favorite when in favorites list`() = runTest {
        // Given
        val userLocation = GeoPoint(40.4168, -3.7038)
        whenever(locationProvider.currentLocation()).thenReturn(userLocation)
        favoriteIdsFlow.value = setOf("1")

        val stations = listOf(
            createStation("1", "Favorita", 5, 5, 40.4170, -3.7040),
            createStation("2", "No Favorita", 5, 5, 40.4160, -3.7030)
        )
        stationsStateFlow.value = StationsState(stations = stations, isLoading = false)

        // When
        val params = FindNearbyStationParams()
        val result = function.execute(params)

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0].isFavorite)
        assertTrue(!result[1].isFavorite)
    }

    @Test
    fun `execute returns empty list when location is null`() = runTest {
        // Given
        whenever(locationProvider.currentLocation()).thenReturn(null)

        // When
        val params = FindNearbyStationParams()
        val result = function.execute(params)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute returns empty list when stations list is empty`() = runTest {
        // Given
        val userLocation = GeoPoint(40.4168, -3.7038)
        whenever(locationProvider.currentLocation()).thenReturn(userLocation)
        stationsStateFlow.value = StationsState(stations = emptyList(), isLoading = false)

        // When
        val params = FindNearbyStationParams()
        val result = function.execute(params)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute filters by max distance when provided`() = runTest {
        // Given
        val userLocation = GeoPoint(40.4168, -3.7038)
        whenever(locationProvider.currentLocation()).thenReturn(userLocation)

        // Stations at different distances
        val stations = listOf(
            createStation("1", "Muy Cerca", 5, 5, 40.4169, -3.7039), // ~14m
            createStation("2", "Lejos", 5, 5, 40.4268, -3.7138)      // ~1400m
        )
        stationsStateFlow.value = StationsState(stations = stations, isLoading = false)

        // When
        val params = FindNearbyStationParams(maxDistance = 100) // 100 meters max
        val result = function.execute(params)

        // Then
        assertEquals(1, result.size)
        assertEquals("Muy Cerca", result[0].name)
    }

    @Test
    fun `execute returns all stations when preference is ANY`() = runTest {
        // Given
        val userLocation = GeoPoint(40.4168, -3.7038)
        whenever(locationProvider.currentLocation()).thenReturn(userLocation)

        val stations = listOf(
            createStation("1", "Con Bicis", 5, 5, 40.4170, -3.7040),
            createStation("2", "Sin Bicis", 0, 10, 40.4160, -3.7030),
            createStation("3", "Con Plazas", 3, 3, 40.4180, -3.7050)
        )
        stationsStateFlow.value = StationsState(stations = stations, isLoading = false)

        // When
        val params = FindNearbyStationParams(preference = StationPreference.ANY)
        val result = function.execute(params)

        // Then
        assertEquals(3, result.size)
    }

    private fun createStation(
        id: String,
        name: String,
        bikes: Int,
        slots: Int,
        lat: Double,
        lon: Double
    ): Station {
        return Station(
            id = id,
            name = name,
            address = "",
            bikesAvailable = bikes,
            slotsFree = slots,
            latitude = lat,
            longitude = lon,
            isOpen = true,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
