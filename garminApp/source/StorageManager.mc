using Toybox.Application;
using Toybox.Lang;
using Toybox.Time;

module StorageManager {
    private static const KEY_STATIONS_DATA = "biciStations";
    private static const KEY_LAST_UPDATE = "lastUpdate";
    private static const KEY_SELECTED_CITY = "selectedCity";

    private static var _cachedData as StationsData or Null;
    private static var _nearestStation as BikeStationModel or Null;
    private static var _lastUpdateTime as Number or Null;
    private static var _selectedCity as String;

    public function loadCachedStations() as Void {
        _nearestStation = null;
        _lastUpdateTime = null;
        _selectedCity = "";

        try {
            var data = Storage.getValue(KEY_STATIONS_DATA);
            if (data != null && data instanceof Dictionary) {
                var stationsData = StationsData.fromDict(data);
                if (stationsData != null) {
                    _nearestStation = stationsData.nearest;
                    _lastUpdateTime = stationsData.timestamp;
                }
            }

            var city = Storage.getValue(KEY_SELECTED_CITY);
            if (city != null && city instanceof String) {
                _selectedCity = city;
            }

            System.println("StorageManager: Loaded cached station: " + 
                (_nearestStation != null ? _nearestStation.name : "none"));
        } catch (ex) {
            System.println("StorageManager: Failed to load cached stations: " + ex.getMessage());
        }
    }

    public function saveStationsData(data as StationsData) as Void {
        try {
            Storage.setValue(KEY_STATIONS_DATA, data.toDict());
            _nearestStation = data.nearest;
            _lastUpdateTime = data.timestamp;
            System.println("StorageManager: Saved " + data.backup.size() + " stations");
        } catch (ex) {
            System.println("StorageManager: Failed to save stations: " + ex.getMessage());
        }
    }

    public function getNearestStation() as BikeStationModel or Null {
        return _nearestStation;
    }

    public function getLastUpdateTime() as Number or Null {
        return _lastUpdateTime;
    }

    public function getSelectedCity() as String {
        return _selectedCity;
    }

    public function setSelectedCity(cityId as String) as Void {
        _selectedCity = cityId;
        Storage.setValue(KEY_SELECTED_CITY, cityId);
    }

    public function clearAllData() as Void {
        Storage.deleteValue(KEY_STATIONS_DATA);
        Storage.deleteValue(KEY_LAST_UPDATE);
        _nearestStation = null;
        _lastUpdateTime = null;
        System.println("StorageManager: Cleared all data");
    }

    public function isDataFresh() as Boolean {
        if (_lastUpdateTime == null) {
            return false;
        }

        var ageSeconds = Time.now().value() - _lastUpdateTime;
        return ageSeconds < 300;
    }
}
