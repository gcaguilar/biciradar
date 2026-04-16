using Toybox.Application;
using Toybox.Application.Storage;
using Toybox.Time;

module StorageManager {
    const KEY_STATIONS_DATA = "biciStations";
    const KEY_SELECTED_CITY = "selectedCity";

    var _nearestStation = null;
    var _backupStations = [];
    var _lastUpdateTime = null;
    var _selectedCity = "";

    public function loadCachedStations() as Void {
        _nearestStation = null;
        _backupStations = [];
        _lastUpdateTime = null;
        _selectedCity = "";

        var data = Storage.getValue(KEY_STATIONS_DATA);
        if (data != null) {
            var stationsData = StationsData.fromDict(data);
            if (stationsData != null) {
                _nearestStation = stationsData.nearest;
                _backupStations = stationsData.backup;
                _lastUpdateTime = stationsData.timestamp;
            }
        }

        var city = Storage.getValue(KEY_SELECTED_CITY);
        if (city != null) {
            _selectedCity = city;
        }
    }

    public function saveRawStationsData(data) as Void {
        Storage.setValue(KEY_STATIONS_DATA, data);
        var stationsData = StationsData.fromDict(data);
        if (stationsData != null) {
            _nearestStation = stationsData.nearest;
            _backupStations = stationsData.backup;
            _lastUpdateTime = stationsData.timestamp;
        }
    }

    public function getNearestStation() {
        return _nearestStation;
    }

    public function getLastUpdateTime() {
        return _lastUpdateTime;
    }

    public function getNearbyStations() {
        var stations = [];
        if (_nearestStation != null) {
            stations.add(_nearestStation);
        }

        stations.addAll(_backupStations);

        return stations;
    }

    public function getSelectedCity() {
        return _selectedCity;
    }

    public function setSelectedCity(cityId) as Void {
        _selectedCity = cityId;
        Storage.setValue(KEY_SELECTED_CITY, cityId);
    }

    public function clearAllData() as Void {
        Storage.deleteValue(KEY_STATIONS_DATA);
        _nearestStation = null;
        _backupStations = [];
        _lastUpdateTime = null;
    }

    public function loadDemoStations() as Void {
        var demoData = {
            "nearest" => {
                "id" => "1",
                "name" => "Plaza Espana",
                "bikes" => 8,
                "distance" => 120,
                "ebikes" => 2
            },
            "backup" => [
                {
                    "id" => "2",
                    "name" => "Paraninfo",
                    "bikes" => 4,
                    "distance" => 260,
                    "ebikes" => 1
                },
                {
                    "id" => "3",
                    "name" => "Delicias",
                    "bikes" => 1,
                    "distance" => 480,
                    "ebikes" => 0
                },
                {
                    "id" => "4",
                    "name" => "Puerta Carmen",
                    "bikes" => 6,
                    "distance" => 620,
                    "ebikes" => 2
                },
                {
                    "id" => "5",
                    "name" => "Universidad",
                    "bikes" => 3,
                    "distance" => 910,
                    "ebikes" => 1
                },
                {
                    "id" => "6",
                    "name" => "La Romareda",
                    "bikes" => 0,
                    "distance" => 1450,
                    "ebikes" => 0
                }
            ],
            "timestamp" => Time.now().value()
        };

        saveRawStationsData(demoData);
    }

    public function isDataFresh() {
        if (_lastUpdateTime == null) {
            return false;
        }

        var ageSeconds = Time.now().value() - _lastUpdateTime;
        return ageSeconds < 300;
    }
}
