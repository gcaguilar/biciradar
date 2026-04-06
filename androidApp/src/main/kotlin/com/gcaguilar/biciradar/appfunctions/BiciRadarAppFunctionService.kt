package com.gcaguilar.biciradar.appfunctions

import android.content.Intent
import android.os.Bundle
import com.gcaguilar.biciradar.appfunctions.functions.FindNearbyStationFunction
import com.gcaguilar.biciradar.appfunctions.functions.GetFavoritesFunction
import com.gcaguilar.biciradar.appfunctions.functions.GetStationStatusFunction
import com.gcaguilar.biciradar.appfunctions.parameters.FindNearbyStationParams
import com.gcaguilar.biciradar.appfunctions.parameters.StationPreference
import javax.inject.Inject

/**
 * Service that exposes BiciRadar functionality as Android App Functions.
 * 
 * This is a placeholder implementation. The full App Functions integration
 * requires Android 14+ and proper API bindings.
 */
class BiciRadarAppFunctionService {

    @Inject
    lateinit var findNearbyStationFunction: FindNearbyStationFunction
    
    @Inject
    lateinit var getStationStatusFunction: GetStationStatusFunction
    
    @Inject
    lateinit var getFavoritesFunction: GetFavoritesFunction

    /**
     * Executes an app function based on the intent action.
     * This is a simplified implementation for compatibility.
     */
    suspend fun execute(intent: Intent): Bundle {
        val functionId = intent.action ?: throw IllegalArgumentException("Action required")
        val parameters = intent.extras ?: Bundle()
        
        return when (functionId) {
            "findNearbyStation" -> {
                val preferenceStr = parameters.getString("preference", "ANY")
                val preference = try {
                    StationPreference.valueOf(preferenceStr)
                } catch (e: IllegalArgumentException) {
                    StationPreference.ANY
                }
                val maxDistance = parameters.getInt("maxDistance", 0).takeIf { it > 0 }
                
                val params = FindNearbyStationParams(
                    preference = preference,
                    maxDistance = maxDistance
                )
                val result = findNearbyStationFunction.execute(params)
                Bundle().apply {
                    putParcelableArrayList("stations", ArrayList(result))
                }
            }
            "getStationStatus" -> {
                val stationId = parameters.getString("stationId") 
                    ?: throw IllegalArgumentException("stationId required")
                val result = getStationStatusFunction.execute(stationId)
                    ?: throw IllegalArgumentException("Station not found")
                Bundle().apply {
                    putParcelable("station", result)
                }
            }
            "getFavorites" -> {
                val result = getFavoritesFunction.execute()
                Bundle().apply {
                    putParcelable("favorites", result)
                }
            }
            else -> throw IllegalArgumentException("Unknown function: $functionId")
        }
    }
}