package com.gcaguilar.biciradar.appfunctions

import android.os.Bundle
import androidx.appfunctions.AppFunctionService
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
     * @throws IllegalArgumentException if the functionId is not recognized
     */
    override suspend fun onExecute(
        functionId: String,
        parameters: Bundle
    ): Bundle {
        return when (functionId) {
            "findNearbyStation" -> findNearbyStationFunction.execute(parameters)
            "getStationStatus" -> getStationStatusFunction.execute(parameters)
            "getFavorites" -> getFavoritesFunction.execute(parameters)
            else -> throw IllegalArgumentException("Unknown function: $functionId")
        }
    }
}
