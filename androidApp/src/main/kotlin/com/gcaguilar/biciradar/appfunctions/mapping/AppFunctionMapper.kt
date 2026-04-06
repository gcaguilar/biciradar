package com.gcaguilar.biciradar.appfunctions.mapping

import android.os.Bundle
import com.gcaguilar.biciradar.appfunctions.parameters.StationPreference
import com.gcaguilar.biciradar.core.AssistantAction

/**
 * Maps between App Functions and existing Assistant Actions
 * Maintains backward compatibility with existing assistant shortcuts
 */
object AppFunctionMapper {

    fun toAssistantAction(
        functionId: String,
        params: Bundle
    ): AssistantAction? {
        return when (functionId) {
            "findNearbyStation" -> {
                val preference = params.getString("preference")?.let {
                    StationPreference.valueOf(it)
                } ?: StationPreference.ANY

                when (preference) {
                    StationPreference.ANY -> AssistantAction.NearestStation
                    StationPreference.WITH_BIKES -> AssistantAction.NearestStationWithBikes
                    StationPreference.WITH_SLOTS -> AssistantAction.NearestStationWithSlots
                }
            }
            "getStationStatus" -> {
                val stationId = params.getString("stationId") ?: return null
                AssistantAction.StationStatus(stationId)
            }
            "getFavorites" -> AssistantAction.FavoriteStations
            else -> null
        }
    }

    fun fromAssistantAction(action: AssistantAction): Pair<String, Bundle>? {
        val bundle = Bundle()
        return when (action) {
            is AssistantAction.NearestStation -> {
                "findNearbyStation" to bundle.apply {
                    putString("preference", StationPreference.ANY.name)
                }
            }
            is AssistantAction.NearestStationWithBikes -> {
                "findNearbyStation" to bundle.apply {
                    putString("preference", StationPreference.WITH_BIKES.name)
                }
            }
            is AssistantAction.NearestStationWithSlots -> {
                "findNearbyStation" to bundle.apply {
                    putString("preference", StationPreference.WITH_SLOTS.name)
                }
            }
            is AssistantAction.StationStatus -> {
                "getStationStatus" to bundle.apply {
                    putString("stationId", action.stationId)
                }
            }
            is AssistantAction.FavoriteStations -> {
                "getFavorites" to bundle
            }
            else -> null
        }
    }
}
