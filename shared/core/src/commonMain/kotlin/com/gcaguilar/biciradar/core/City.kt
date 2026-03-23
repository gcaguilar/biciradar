package com.gcaguilar.biciradar.core

import kotlinx.serialization.Serializable

@Serializable
enum class City(
    val id: String,
    val displayName: String,
    val gbfsDiscoveryUrl: String,
    val defaultLatitude: Double,
    val defaultLongitude: Double,
    val supportsUsagePatterns: Boolean,
    val supportsEbikes: Boolean,
) {
    ZARAGOZA(
        id = "zaragoza",
        displayName = "Zaragoza",
        gbfsDiscoveryUrl = "https://api.citybik.es/gbfs/2/bizi/gbfs.json",
        defaultLatitude = 41.6488,
        defaultLongitude = -0.8751,
        supportsUsagePatterns = true,
        supportsEbikes = true,
    ),
    MADRID(
        id = "madrid",
        displayName = "Madrid",
        gbfsDiscoveryUrl = "https://api.citybik.es/gbfs/2/bicimad/gbfs.json",
        defaultLatitude = 40.4168,
        defaultLongitude = -3.7038,
        supportsUsagePatterns = false,
        supportsEbikes = true,
    ),
    BARCELONA(
        id = "barcelona",
        displayName = "Barcelona",
        gbfsDiscoveryUrl = "https://api.citybik.es/gbfs/2/bicing/gbfs.json",
        defaultLatitude = 41.3851,
        defaultLongitude = 2.1734,
        supportsUsagePatterns = false,
        supportsEbikes = true,
    ),
    VALENCIA(
        id = "valencia",
        displayName = "Valencia",
        gbfsDiscoveryUrl = "https://api.citybik.es/gbfs/2/mibisivalencia/gbfs.json",
        defaultLatitude = 39.4699,
        defaultLongitude = -0.3763,
        supportsUsagePatterns = false,
        supportsEbikes = true,
    ),
    SEVILLA(
        id = "sevilla",
        displayName = "Sevilla",
        gbfsDiscoveryUrl = "https://api.citybik.es/gbfs/2/sevici/gbfs.json",
        defaultLatitude = 37.3891,
        defaultLongitude = -5.9845,
        supportsUsagePatterns = false,
        supportsEbikes = true,
    ),
    SABADELL(
        id = "sabadell",
        displayName = "Sabadell",
        gbfsDiscoveryUrl = "https://api.citybik.es/gbfs/2/sabadell/gbfs.json",
        defaultLatitude = 41.6083,
        defaultLongitude = 2.1085,
        supportsUsagePatterns = false,
        supportsEbikes = true,
    );

    companion object {
        fun fromId(id: String): City? = entries.find { it.id == id }
        
        fun defaultCity(): City = ZARAGOZA
    }
}
