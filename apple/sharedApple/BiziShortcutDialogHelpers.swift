import Foundation

// MARK: - Station Dialog String Builders
//
// Helpers compartidos entre AppleShortcutRunner (iOS) y WatchShortcutRunner (watchOS).
// Centraliza la redacción de respuestas de asistente relacionadas con estaciones
// para garantizar consistencia entre plataformas.
//
// Las funciones son genéricas sobre BinaryInteger para ser compatibles tanto con
// Int (Swift nativo) como con Int32 (salida de Kotlin/Native).

/// Retorna el texto completo de estado de una estación (bicis + huecos).
func stationStatusText<N: BinaryInteger>(name: String, bikesAvailable: N, slotsFree: N) -> String {
    "\(name) tiene \(bikesAvailable) bicis disponibles y \(slotsFree) huecos libres."
}

/// Retorna el texto de conteo de bicis de una estación.
func stationBikeCountText<N: BinaryInteger>(name: String, bikesAvailable: N) -> String {
    "\(name) tiene \(bikesAvailable) bicis disponibles."
}

/// Retorna el texto de conteo de huecos de una estación.
func stationSlotCountText<N: BinaryInteger>(name: String, slotsFree: N) -> String {
    "\(name) tiene \(slotsFree) huecos libres."
}

// MARK: - Common Dialog Messages

enum BiziDialogMessage {
    // Estaciones (iOS)
    static let stationNotFound = "No he encontrado esa estación en Bici Radar."
    static let stationStateError = "No he podido consultar el estado de esa estación."
    static let stationBikeError = "No he podido consultar las bicis de esa estación."
    static let stationSlotError = "No he podido consultar los huecos de esa estación."

    // Estaciones (watchOS)
    static let stationNotFoundWatch = "No he encontrado esa estación en el reloj."
    static let stationStateErrorWatch = "No he podido consultar el estado de esa estación en el reloj."
    static let stationBikeErrorWatch = "No he podido consultar las bicis de esa estación en el reloj."
    static let stationSlotErrorWatch = "No he podido consultar los huecos de esa estación en el reloj."

    // Favoritos
    static let noFavoritesConfigured = "Abre Bici Radar. Todavía no tienes estaciones favoritas guardadas."
    static let noFavoritesWatch = "Todavía no tengo favoritas sincronizadas desde el iPhone."
    static let favoritesError = "Abre Bici Radar para mostrar tus favoritas."
    static let favoritesErrorWatch = "No he podido consultar tus favoritas en el reloj."

    // Rutas
    static let routeNotFound = "No he encontrado esa estación en Bici Radar."
    static let routeNotFoundWatch = "No he encontrado esa estación para enviar la ruta al iPhone."
    static let routeError = "No he podido preparar esa ruta ahora mismo."
    static let routeErrorWatch = "No he podido preparar esa ruta desde el reloj."
    static let routeNoContact = "No he podido contactar con el iPhone ahora mismo."
    static let monitorError = "No he podido preparar esa monitorización ahora mismo."

    // Ciudades
    static let cityNotFound = "No he encontrado esa ciudad en Bici Radar."
    static let cityChangeError = "No he podido cambiar de ciudad ahora mismo."
}
