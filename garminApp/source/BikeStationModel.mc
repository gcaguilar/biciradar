class BikeStationModel {
    public var id;
    public var name;
    public var bikes;
    public var distance;
    public var ebikes;

    public function initialize(id, name, bikes, distance, ebikes) {
        self.id = id;
        self.name = name;
        self.bikes = bikes;
        self.distance = distance;
        self.ebikes = ebikes;
    }

    public static function fromDict(data) {
        if (data == null) {
            return null;
        }

        var name = data["name"];
        var bikes = data["bikes"];
        var distance = data["distance"];
        if (name == null || bikes == null || distance == null) {
            return null;
        }

        var id = data["id"];
        var ebikes = data["ebikes"];
        if (ebikes == null) {
            ebikes = 0;
        }
        if (id == null) {
            id = "";
        }

        return new BikeStationModel(id.toString(), name.toString(), bikes, distance, ebikes);
    }

    public function toDict() {
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
    public var nearest = null;
    public var backup = [];
    public var timestamp = 0;

    public function initialize() {
    }

    public static function fromDict(data) {
        if (data == null) {
            return null;
        }

        var result = new StationsData();

        if (data["nearest"] != null) {
            result.nearest = BikeStationModel.fromDict(data["nearest"]);
        }

        var backupData = data["backup"];
        if (backupData != null) {
            for (var i = 0; i < backupData.size(); i += 1) {
                var station = BikeStationModel.fromDict(backupData[i]);
                if (station != null) {
                    result.backup.add(station);
                }
            }
        }

        if (data["timestamp"] != null) {
            result.timestamp = data["timestamp"];
        }

        return result;
    }

    public function toDict() {
        var backupArray = [];
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
