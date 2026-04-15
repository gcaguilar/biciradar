using Toybox.Lang;

class BikeStationModel {
    public var id as String;
    public var name as String;
    public var bikes as Number;
    public var distance as Number;
    public var ebikes as Number;

    public function initialize(id as String, name as String, bikes as Number, distance as Number, ebikes as Number) {
        self.id = id;
        self.name = name;
        self.bikes = bikes;
        self.distance = distance;
        self.ebikes = ebikes;
    }

    public static function fromDict(data as Dictionary?) as BikeStationModel or Null {
        if (data == null) {
            return null;
        }

        var id = data.get("id") as String or Number;
        var name = data.get("name") as String;
        var bikes = data.get("bikes") as Number;
        var distance = data.get("distance") as Number;
        var ebikes = data.get("ebikes") as Number;

        if (name == null || bikes == null || distance == null) {
            return null;
        }

        return new BikeStationModel(
            id != null ? id.toString() : "",
            name,
            bikes,
            distance,
            ebikes != null ? ebikes : 0
        );
    }

    public function toDict() as Dictionary {
        return {
            "id" => id,
            "name" => name,
            "bikes" => bikes,
            "distance" => distance,
            "ebikes" => ebikes
        };
    }
}

class StationsData {
    public var nearest as BikeStationModel or Null;
    public var backup as Array<BikeStationModel>;
    public var timestamp as Number;

    public function initialize() {
        nearest = null;
        backup = new Array<BikeStationModel>[0];
        timestamp = 0;
    }

    public static function fromDict(data as Dictionary?) as StationsData or Null {
        if (data == null) {
            return null;
        }

        var result = new StationsData();

        var nearestData = data.get("nearest") as Dictionary?;
        if (nearestData != null) {
            result.nearest = BikeStationModel.fromDict(nearestData);
        }

        var backupData = data.get("backup") as Array?;
        if (backupData != null) {
            result.backup = new Array<BikeStationModel>[0];
            for (var i = 0; i < backupData.size(); i += 1) {
                var station = BikeStationModel.fromDict(backupData[i] as Dictionary?);
                if (station != null) {
                    result.backup.add(station);
                }
            }
        }

        var ts = data.get("timestamp") as Number;
        result.timestamp = ts != null ? ts : 0;

        return result;
    }

    public function toDict() as Dictionary {
        var backupArray = new Array<Dictionary>[0];
        for (var i = 0; i < backup.size(); i += 1) {
            backupArray.add(backup[i].toDict());
        }

        return {
            "nearest" => nearest != null ? nearest.toDict() : null,
            "backup" => backupArray,
            "timestamp" => timestamp
        };
    }
}
