package com.gcaguilar.biciradar.appfunctions

import android.os.Bundle
import androidx.appfunctions.AppFunctionService
import com.gcaguilar.biciradar.appfunctions.functions.FindNearbyStationFunction
import com.gcaguilar.biciradar.appfunctions.functions.GetFavoritesFunction
import com.gcaguilar.biciradar.appfunctions.functions.GetStationStatusFunction
import com.gcaguilar.biciradar.appfunctions.parameters.FindNearbyStationParams
import com.gcaguilar.biciradar.appfunctions.parameters.GetStationStatusParams
import com.gcaguilar.biciradar.appfunctions.parameters.StationPreference
import javax.inject.Inject

/**
 * Service that handles the execution of App Functions for BiciRadar.
 *
 * This service extends [AppFunctionService] and acts as the entry point for all
 * App Function calls. It routes function execution requests to the appropriate
 * function handlers based on the functionId.
 *
 * The function handlers are injected via Dagger/Hilt dependency injection.
 */
class BiciRadarAppFunctionService : AppFunctionService() {

    @Inject
    lateinit var findNearbyStationFunction: FindNearbyStationFunction

    @Inject
    lateinit var getStationStatusFunction: GetStationStatusFunction

    @Inject
    lateinit var getFavoritesFunction: GetFavoritesFunction

    /**
     * Executes the requested App Function based on the functionId.
     *
     * @param functionId The identifier of the function to execute
     * @param parameters The parameters passed to the function
     * @return The result of the function execution as a Bundle
     * @throws IllegalArgumentException if the functionId is not recognized or required parameters are missing
     */
    override suspend fun onExecute(
        functionId: String,
        parameters: Bundle
    ): Bundle {
        return when (functionId) {
            "findNearbyStation" -> {
                val params = FindNearbyStationParams(
                    preference = parameters.getString("preference")?.let {
                        StationPreference.valueOf(it)
                    } ?: StationPreference.ANY,
                    maxDistance = parameters.getInt("maxDistance").takeIf { it > 0 }
                )
                val result = findNearbyStationFunction.execute(params)
                Bundle().apply {
                    putParcelableArrayList("stations", ArrayList(result))
                }
            }
            "getStationStatus" -> {
                val stationId = parameters.getString("stationId")
                    ?: throw IllegalArgumentException("stationId required")
                val params = GetStationStatusParams(stationId = stationId)
                val result = getStationStatusFunction.execute(params)
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
