import Toybox.WatchUi;
import Toybox.Graphics;
import Toybox.System;
import Toybox.Time;
import Toybox.Lang;
import Toybox.Application;

class BiciRadarView extends WatchUi.Menu2 {
    private var _focusedStationId = null;

    public function initialize() {
        Menu2.initialize({ :title => WatchUi.loadResource($.Rez.Strings.NearbyStationsTitle) });
    }

    public function onShow() as Void {
        StorageManager.loadCachedStations();
        if (BiciRadarApp.isDemoMode() && StorageManager.getNearbyStations().size() == 0) {
            StorageManager.loadDemoStations();
        }

        var app = Application.getApp() as BiciRadarApp;
        if (app != null && !BiciRadarApp.isDemoMode()) {
            app.getBleManager().requestRefresh();
        }
        refreshMenuItems();
        WatchUi.requestUpdate();
    }

    public function onUpdate(dc as Dc) as Void {
        refreshMenuItems();
        Menu2.onUpdate(dc);
    }

    public function reloadStations() as Void {
        refreshMenuItems();
        WatchUi.requestUpdate();
    }

    public function getStationForMenuItem(item as WatchUi.MenuItem) {
        var stationId = item.getId();
        if (stationId == null) {
            return null;
        }

        _focusedStationId = stationId.toString();

        var stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        for (var i = 0; i < stations.size(); i += 1) {
            if (stations[i].id == _focusedStationId) {
                return stations[i];
            }
        }

        return null;
    }

    private function refreshMenuItems() as Void {
        clearMenuItems();

        var stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        if (stations.size() == 0 && BiciRadarApp.isDemoMode()) {
            StorageManager.loadDemoStations();
            stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        }

        if (stations.size() == 0) {
            setTitle(WatchUi.loadResource($.Rez.Strings.AppName));
            addItem(new WatchUi.MenuItem(
                WatchUi.loadResource($.Rez.Strings.NoData) as String,
                WatchUi.loadResource($.Rez.Strings.OpenApp) as String,
                "empty",
                null
            ));
            return;
        }

        setTitle(WatchUi.loadResource($.Rez.Strings.NearbyStationsTitle));

        var focusIndex = 0;
        for (var i = 0; i < stations.size(); i += 1) {
            var station = stations[i];
            addItem(new WatchUi.MenuItem(
                truncateStatic(station.name, 22),
                buildStationSubLabel(station),
                station.id,
                null
            ));

            if (_focusedStationId != null && station.id == _focusedStationId) {
                focusIndex = i;
            }
        }

        if (stations.size() > 0) {
            setFocus(focusIndex);
        }
    }

    private function clearMenuItems() as Void {
        while (getItem(0) != null) {
            deleteItem(0);
        }
    }

    private function buildStationSubLabel(station as BikeStationModel) as String {
        var label = formatDistance(station.distance) + "  " + station.bikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyBikes) as String);
        if (station.ebikes > 0) {
            label += "  " + station.ebikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyEbikes) as String);
        }

        return label;
    }

    public static function drawStationSummary(dc as Dc, station as BikeStationModel, isGlance as Boolean) as Void {
        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);

        if (!isGlance) {
            dc.drawText(
                screenWidth / 2,
                20,
                Graphics.FONT_TINY,
                WatchUi.loadResource($.Rez.Strings.AppName),
                Graphics.TEXT_JUSTIFY_CENTER
            );
        }

        var nameY = isGlance ? (screenHeight / 2 - 16) : (screenHeight / 2 - 40);
        var valueY = isGlance ? (screenHeight / 2 + 4) : (screenHeight / 2 - 10);
        var labelY = isGlance ? (screenHeight - 14) : (screenHeight / 2 + 30);
        var shortName = truncateStatic(station.name, isGlance ? 14 : 18);
        dc.drawText(
            screenWidth / 2,
            nameY,
            Graphics.FONT_TINY,
            shortName,
            Graphics.TEXT_JUSTIFY_CENTER
        );

        var bikeColor = getBikeColorStatic(station.bikes);
        dc.setColor(bikeColor, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            valueY,
            isGlance ? Graphics.FONT_NUMBER_MEDIUM : Graphics.FONT_NUMBER_MEDIUM,
            station.bikes.format("%d"),
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        var bikesLabel = WatchUi.loadResource($.Rez.Strings.Bikes) as String;
        dc.drawText(
            screenWidth / 2,
            labelY,
            Graphics.FONT_TINY,
            bikesLabel,
            Graphics.TEXT_JUSTIFY_CENTER
        );
    }

    public function reloadDemoStations() as Void {
        StorageManager.loadDemoStations();
        reloadStations();
    }

    private function getBikeColor(bikes) {
        return getBikeColorStatic(bikes);
    }

    private function truncateString(str as String, maxLen) as String {
        return truncateStatic(str, maxLen);
    }

    private function formatDistance(meters) as String {
        if (meters < 1000) {
            return meters.format("%d") + (WatchUi.loadResource($.Rez.Strings.MetersUnit) as String);
        } else {
            var km = meters / 1000.0;
            return km.format("%.1f") + (WatchUi.loadResource($.Rez.Strings.KilometersUnit) as String);
        }
    }

    private static function getBikeColorStatic(bikes) {
        if (bikes >= 5) {
            return Graphics.COLOR_GREEN;
        } else if (bikes >= 2) {
            return Graphics.COLOR_YELLOW;
        } else {
            return Graphics.COLOR_RED;
        }
    }

    private static function truncateStatic(str as String, maxLen) as String {
        if (str.length() <= maxLen) {
            return str;
        }
        return str.substring(0, maxLen - 1) + "...";
    }

    public static function bikeColorFor(bikes) {
        return getBikeColorStatic(bikes);
    }

    public static function truncateStationName(str as String, maxLen) as String {
        return truncateStatic(str, maxLen);
    }

    public static function formatStationDistance(meters) as String {
        if (meters < 1000) {
            return meters.format("%d") + (WatchUi.loadResource($.Rez.Strings.MetersUnit) as String);
        }

        var km = meters / 1000.0;
        return km.format("%.1f") + (WatchUi.loadResource($.Rez.Strings.KilometersUnit) as String);
    }
}

class BiciRadarDetailView extends WatchUi.View {
    private var _station;

    public function initialize(station) {
        WatchUi.View.initialize();
        _station = station;
    }

    public function onLayout(dc as Dc) as Void {
        setLayout($.Rez.Layouts.DetailLayout(dc));
    }

    public function onUpdate(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        setText("TitleLabel", BiciRadarView.truncateStationName(_station.name, 22));
        setText("DetailText", buildBodyText());

        View.onUpdate(dc);
    }

    private function statusText() as String {
        if (_station.bikes >= 5) {
            return WatchUi.loadResource($.Rez.Strings.AvailabilityHigh);
        } else if (_station.bikes >= 2) {
            return WatchUi.loadResource($.Rez.Strings.AvailabilityMedium);
        }

        return WatchUi.loadResource($.Rez.Strings.AvailabilityLow);
    }

    private function availabilityText() as String {
        if (_station.bikes == 1) {
            return WatchUi.loadResource($.Rez.Strings.OneBike);
        }

        return _station.bikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyBikes) as String);
    }

    private function ebikeText() as String {
        if (_station.ebikes == 1) {
            return WatchUi.loadResource($.Rez.Strings.OneEbike);
        }

        return _station.ebikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyEbikes) as String);
    }

    private function setText(id as String, text as String) as Void {
        var drawable = findDrawableById(id);
        if (drawable != null) {
            if (drawable instanceof WatchUi.TextArea) {
                (drawable as WatchUi.TextArea).setText(text);
                return;
            }

            if (drawable instanceof WatchUi.Text) {
                (drawable as WatchUi.Text).setText(text);
            }
        }
    }

    private function buildBodyText() as String {
        return (WatchUi.loadResource($.Rez.Strings.DetailBikes) as String) + ": " + availabilityText() + "\n"
            + (WatchUi.loadResource($.Rez.Strings.DetailEbikes) as String) + ": " + ebikeText() + "\n"
            + (WatchUi.loadResource($.Rez.Strings.DetailDistance) as String) + ": " + BiciRadarView.formatStationDistance(_station.distance) + "\n"
            + (WatchUi.loadResource($.Rez.Strings.DetailStatus) as String) + ": " + statusText() + "\n\n"
            + (WatchUi.loadResource($.Rez.Strings.DetailSelectRoute) as String) + "\n"
            + updatedText();
    }

    public function getStationId() as String {
        return _station.id;
    }

    private function updatedText() as String {
        var lastUpdate = StorageManager.getLastUpdateTime();
        if (lastUpdate == null) {
            return WatchUi.loadResource($.Rez.Strings.UpdatedNoData);
        }

        var ageMinutes = ((Time.now().value() - lastUpdate) / 60);
        if (ageMinutes < 1) {
            return WatchUi.loadResource($.Rez.Strings.UpdatedNow);
        } else if (ageMinutes == 1) {
            return WatchUi.loadResource($.Rez.Strings.UpdatedOneMinute);
        } else if (ageMinutes < 60) {
            return (WatchUi.loadResource($.Rez.Strings.UpdatedManyMinutesPrefix) as String) + " " + ageMinutes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.UpdatedManyMinutesSuffix) as String);
        }

        return WatchUi.loadResource($.Rez.Strings.UpdatedOld);
    }
}

class BiciRadarViewDelegate extends WatchUi.Menu2InputDelegate {
    private var _view as BiciRadarView;

    public function initialize(view as BiciRadarView) {
        Menu2InputDelegate.initialize();
        _view = view;
    }

    public function onSelect(item as WatchUi.MenuItem) as Void {
        var station = _view.getStationForMenuItem(item);
        if (station != null) {
            var detailView = new BiciRadarDetailView(station);
            WatchUi.pushView(detailView, new BiciRadarDetailDelegate(detailView), WatchUi.SLIDE_UP);
        }
    }

    public function onBack() as Void {
        _view.reloadStations();
    }

    public function onNextPage() as Boolean {
        _view.reloadStations();
        return false;
    }

    public function onPreviousPage() as Boolean {
        _view.reloadStations();
        return false;
    }

    public function onWrap(key as WatchUi.Key) as Boolean {
        _view.reloadStations();
        return true;
    }

    public function onMenu() as Boolean {
        _view.reloadDemoStations();
        return true;
    }
}

class BiciRadarDetailDelegate extends WatchUi.BehaviorDelegate {
    private var _view as BiciRadarDetailView;

    public function initialize(view as BiciRadarDetailView) {
        BehaviorDelegate.initialize();
        _view = view;
    }

    public function onBack() as Boolean {
        WatchUi.popView(WatchUi.SLIDE_DOWN);
        return true;
    }

    public function onSelect() as Boolean {
        if (BiciRadarApp.isDemoMode()) {
            WatchUi.showToast(WatchUi.loadResource($.Rez.Strings.ToastDemoNoPhone), null);
            return true;
        }

        var app = Application.getApp() as BiciRadarApp;
        if (app.getBleManager().requestRouteToStation(_view.getStationId())) {
            WatchUi.showToast(WatchUi.loadResource($.Rez.Strings.ToastRouteSent), null);
            return true;
        }

        WatchUi.showToast(WatchUi.loadResource($.Rez.Strings.ToastNoPhoneConnection), null);
        return true;
    }
}
