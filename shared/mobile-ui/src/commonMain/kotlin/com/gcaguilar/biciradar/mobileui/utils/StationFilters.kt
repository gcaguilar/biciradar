package com.gcaguilar.biciradar.mobileui.utils

import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.filterStationsByQuery

/**
 * Filtra estaciones según una query de búsqueda.
 *
 * @param stations Lista de estaciones a filtrar
 * @param searchQuery Query de búsqueda
 * @return Lista filtrada de estaciones
 */
fun filterStations(
    stations: List<Station>,
    searchQuery: String,
): List<Station> = filterStationsByQuery(stations, searchQuery)
