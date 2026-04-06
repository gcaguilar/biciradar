package com.gcaguilar.biciradar.appfunctions

import android.os.Bundle
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import com.gcaguilar.biciradar.appfunctions.functions.FindNearbyStationFunction
import com.gcaguilar.biciradar.appfunctions.functions.GetFavoritesFunction
import com.gcaguilar.biciradar.appfunctions.functions.GetStationStatusFunction
import com.gcaguilar.biciradar.appfunctions.parameters.FindNearbyStationParams
import com.gcaguilar.biciradar.appfunctions.parameters.StationPreference
import javax.inject.Inject

/**
 * Service that exposes BiciRadar functionality as Android App Functions.
 */
class BiciRadarAppFunctionService : AppFunctionService() {

    @Inject
    lateinit var findNearbyStationFunction: FindNearbyStationFunction
    
    @Inject
    lateinit var getStationStatusFunction: GetStationStatusFunction
    
    @Inject
    lateinit var getFavoritesFunction: GetFavoritesFunction

    override suspend fun executeFunction(request: ExecuteAppFunctionRequest): ExecuteAppFunctionResponse {
        val functionId = request.functionId
        val parameters = request.parameters
        
        val resultBundle = when (functionId) {
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
        
        return ExecuteAppFunctionResponse(resultBundle)
    }
}