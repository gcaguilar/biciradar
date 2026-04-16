import Toybox.WatchUi;
import Toybox.Graphics;
import Toybox.System;
import Toybox.Time;
import Toybox.Lang;
import Toybox.Application;

class BiciRadarView extends WatchUi.View {
    private var _selectedStationIndex = 0;
    private var _listStartIndex = 0;
    private var _visibleRowCount = 0;

    public function initialize() {
        WatchUi.View.initialize();
    }

    public function onLayout(dc as Dc) as Void {
    }

    public function onShow() as Void {
        var app = Application.getApp() as BiciRadarApp;
        if (app != null) {
            app.getBleManager().requestRefresh();
        }
        clampSelection();
        WatchUi.requestUpdate();
    }

    public function onUpdate(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        var stations = StorageManager.getNearbyStations();
        clampSelection();

        if (stations.size() == 0) {
            drawNoData(dc);
            return;
        }

        drawStations(dc, stations);
    }

    private function drawNoData(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        var noData = WatchUi.loadResource($.Rez.Strings.NoData) as String;
        var openApp = WatchUi.loadResource($.Rez.Strings.OpenApp) as String;
        dc.drawText(
            dc.getWidth() / 2,
            dc.getHeight() / 2 - 20,
            Graphics.FONT_TINY,
            noData,
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );
        dc.drawText(
            dc.getWidth() / 2,
            dc.getHeight() / 2 + 20,
            Graphics.FONT_TINY,
            openApp,
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );
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
                "BiciRadar",
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

    private function drawStations(dc as Dc, stations as Array<BikeStationModel>) as Void {
        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();
        var headerY = 16;
        var listTop = 32;
        var rowHeight = 24;
        var footerY = screenHeight - 10;
        var availableHeight = footerY - listTop - 4;
        var visibleRows = availableHeight / rowHeight;

        if (visibleRows < 1) {
            visibleRows = 1;
        }

        _visibleRowCount = visibleRows;
        updateListWindow(stations, visibleRows);

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            headerY,
            Graphics.FONT_TINY,
            "Estaciones cercanas",
            Graphics.TEXT_JUSTIFY_CENTER
        );

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawLine(12, 24, screenWidth - 12, 24);

        for (var i = 0; i < visibleRows; i += 1) {
            var stationIndex = _listStartIndex + i;
            if (stationIndex >= stations.size()) {
                break;
            }

            drawStationRow(dc, stations[stationIndex], stationIndex, listTop + (i * rowHeight), rowHeight);
        }

        drawFooter(dc);
    }

    private function drawStationRow(dc as Dc, station as BikeStationModel, stationIndex as Number, y as Number, rowHeight as Number) as Void {
        var screenWidth = dc.getWidth();
        var name = truncateStatic(station.name, 18);
        var isSelected = stationIndex == _selectedStationIndex;
        var indicatorColor = getBikeColorStatic(station.bikes);

        if (isSelected) {
            dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_DK_GRAY);
            dc.fillRectangle(8, y - 1, screenWidth - 16, rowHeight - 4);
            dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_DK_GRAY);
        } else {
            dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        }

        dc.setColor(isSelected ? indicatorColor : indicatorColor, isSelected ? Graphics.COLOR_DK_GRAY : Graphics.COLOR_BLACK);
        dc.fillRectangle(12, y + 4, 6, 6);

        dc.setColor(isSelected ? Graphics.COLOR_BLACK : Graphics.COLOR_WHITE, isSelected ? Graphics.COLOR_DK_GRAY : Graphics.COLOR_BLACK);
        dc.drawText(24, y + 2, Graphics.FONT_XTINY, name, Graphics.TEXT_JUSTIFY_LEFT);

        dc.setColor(isSelected ? Graphics.COLOR_BLACK : Graphics.COLOR_DK_GRAY, isSelected ? Graphics.COLOR_DK_GRAY : Graphics.COLOR_BLACK);
        dc.drawText(screenWidth - 10, y + 2, Graphics.FONT_XTINY, formatDistance(station.distance), Graphics.TEXT_JUSTIFY_RIGHT);

        dc.setColor(isSelected ? Graphics.COLOR_BLACK : getBikeColorStatic(station.bikes), isSelected ? Graphics.COLOR_DK_GRAY : Graphics.COLOR_BLACK);
        dc.drawText(24, y + 13, Graphics.FONT_XTINY, availabilityText(station), Graphics.TEXT_JUSTIFY_LEFT);

        dc.setColor(isSelected ? Graphics.COLOR_BLACK : Graphics.COLOR_WHITE, isSelected ? Graphics.COLOR_DK_GRAY : Graphics.COLOR_BLACK);
        dc.drawText(screenWidth - 10, y + 13, Graphics.FONT_XTINY, station.bikes.format("%d") + " bicis", Graphics.TEXT_JUSTIFY_RIGHT);
    }

    private function drawFooter(dc as Dc) as Void {
        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();

        var lastUpdate = StorageManager.getLastUpdateTime();
        if (lastUpdate != null) {
            var ageMinutes = ((Time.now().value() - lastUpdate) / 60);
            var updateText;
            if (ageMinutes < 1) {
                updateText = WatchUi.loadResource($.Rez.Strings.JustNow) as String;
            } else if (ageMinutes == 1) {
                updateText = "1 " + (WatchUi.loadResource($.Rez.Strings.MinuteAgo) as String);
            } else if (ageMinutes < 60) {
                updateText = ageMinutes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.MinutesAgo) as String);
            } else {
                updateText = WatchUi.loadResource($.Rez.Strings.Outdated) as String;
            }
            dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
            dc.drawText(
                screenWidth / 2,
                screenHeight - 10,
                Graphics.FONT_XTINY,
                updateText,
                Graphics.TEXT_JUSTIFY_CENTER
            );
        }
    }

    public function selectNextStation() as Void {
        var stations = StorageManager.getNearbyStations();
        if (stations.size() == 0) {
            return;
        }

        _selectedStationIndex = (_selectedStationIndex + 1) % stations.size();
        WatchUi.requestUpdate();
    }

    public function selectPreviousStation() as Void {
        var stations = StorageManager.getNearbyStations();
        if (stations.size() == 0) {
            return;
        }

        _selectedStationIndex = (_selectedStationIndex + stations.size() - 1) % stations.size();
        WatchUi.requestUpdate();
    }

    public function getSelectedStation() {
        var stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        if (stations.size() == 0) {
            return null;
        }

        clampSelection();
        return stations[_selectedStationIndex];
    }

    public function handleTap(y as Number) as Boolean {
        var stations = StorageManager.getNearbyStations();
        if (stations.size() == 0) {
            return false;
        }

        var rowIndex = getTappedRowIndex(y);
        if (rowIndex == null) {
            return false;
        }

        _selectedStationIndex = rowIndex;
        WatchUi.requestUpdate();
        return true;
    }

    private function clampSelection() as Void {
        var stations = StorageManager.getNearbyStations();
        if (stations.size() == 0) {
            _selectedStationIndex = 0;
            _listStartIndex = 0;
            return;
        }

        if (_selectedStationIndex >= stations.size()) {
            _selectedStationIndex = stations.size() - 1;
        }

        if (_selectedStationIndex < 0) {
            _selectedStationIndex = 0;
        }
    }

    private function updateListWindow(stations as Array<BikeStationModel>, visibleRows as Number) as Void {
        if (visibleRows <= 0) {
            _listStartIndex = 0;
            return;
        }

        var maxStart = stations.size() - visibleRows;
        if (maxStart < 0) {
            maxStart = 0;
        }

        if (_listStartIndex < 0) {
            _listStartIndex = 0;
        }
        if (_listStartIndex > maxStart) {
            _listStartIndex = maxStart;
        }

        if (_selectedStationIndex < _listStartIndex) {
            _listStartIndex = _selectedStationIndex;
        }

        var lastVisibleIndex = _listStartIndex + visibleRows - 1;
        if (_selectedStationIndex > lastVisibleIndex) {
            _listStartIndex = _selectedStationIndex - visibleRows + 1;
        }
    }

    private function getTappedRowIndex(y as Number) {
        if (_visibleRowCount <= 0) {
            return null;
        }

        var listTop = 32;
        var rowHeight = 24;
        for (var i = 0; i < _visibleRowCount; i += 1) {
            var rowTop = listTop + (i * rowHeight) - 2;
            var rowBottom = rowTop + rowHeight - 4;
            if (y >= rowTop && y <= rowBottom) {
                return _listStartIndex + i;
            }
        }

        return null;
    }

    private function availabilityText(station as BikeStationModel) as String {
        if (station.ebikes > 0) {
            return station.ebikes.format("%d") + " ebikes";
        }

        return "sin ebikes";
    }

    private function getBikeColor(bikes) {
        return getBikeColorStatic(bikes);
    }

    private function truncateString(str as String, maxLen) as String {
        return truncateStatic(str, maxLen);
    }

    private function formatDistance(meters) as String {
        if (meters < 1000) {
            return meters.format("%d") + "m";
        } else {
            var km = meters / 1000.0;
            return km.format("%.1f") + "km";
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
            return meters.format("%d") + "m";
        }

        var km = meters / 1000.0;
        return km.format("%.1f") + "km";
    }
}

(:glance)
class BiciRadarGlanceView extends WatchUi.GlanceView {
    public function initialize() {
        GlanceView.initialize();
    }

    public function onUpdate(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        var station = StorageManager.getNearestStation();
        if (station == null) {
            dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
            dc.drawText(
                dc.getWidth() / 2,
                dc.getHeight() / 2,
                Graphics.FONT_TINY,
                WatchUi.loadResource($.Rez.Strings.NoData) as String,
                Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
            );
            return;
        }

        BiciRadarView.drawStationSummary(dc, station, true);
    }
}

class BiciRadarDetailView extends WatchUi.View {
    private var _station;

    public function initialize(station) {
        WatchUi.View.initialize();
        _station = station;
    }

    public function onUpdate(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            14,
            Graphics.FONT_XTINY,
            "Detalle estacion",
            Graphics.TEXT_JUSTIFY_CENTER
        );
        dc.drawText(
            screenWidth / 2,
            30,
            Graphics.FONT_TINY,
            BiciRadarView.truncateStationName(_station.name, 20),
            Graphics.TEXT_JUSTIFY_CENTER
        );

        dc.setColor(BiciRadarView.bikeColorFor(_station.bikes), Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            62,
            Graphics.FONT_NUMBER_MEDIUM,
            _station.bikes.format("%d"),
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawText(screenWidth / 2, 84, Graphics.FONT_TINY, "bicis disponibles", Graphics.TEXT_JUSTIFY_CENTER);

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawLine(14, 98, screenWidth - 14, 98);

        drawMetricRow(dc, 18, 114, "ebikes", _station.ebikes.format("%d"));
        drawMetricRow(dc, 18, 134, "distancia", BiciRadarView.formatStationDistance(_station.distance));
        drawMetricRow(dc, 18, 154, "estado", statusText());
        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawText(screenWidth / 2, screenHeight - 24, Graphics.FONT_XTINY, updatedText(), Graphics.TEXT_JUSTIFY_CENTER);
        dc.drawText(screenWidth / 2, screenHeight - 10, Graphics.FONT_XTINY, "tap o atras para volver", Graphics.TEXT_JUSTIFY_CENTER);
    }

    private function drawMetricRow(dc as Dc, x as Number, y as Number, label as String, value as String) as Void {
        var screenWidth = dc.getWidth();
        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawText(x, y, Graphics.FONT_XTINY, label, Graphics.TEXT_JUSTIFY_LEFT);
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.drawText(screenWidth - 18, y, Graphics.FONT_XTINY, value, Graphics.TEXT_JUSTIFY_RIGHT);
    }

    private function statusText() as String {
        if (_station.bikes >= 5) {
            return "alta";
        } else if (_station.bikes >= 2) {
            return "media";
        }

        return "baja";
    }

    private function updatedText() as String {
        var lastUpdate = StorageManager.getLastUpdateTime();
        if (lastUpdate == null) {
            return "sin actualizacion";
        }

        var ageMinutes = ((Time.now().value() - lastUpdate) / 60);
        if (ageMinutes < 1) {
            return "actualizado ahora";
        } else if (ageMinutes == 1) {
            return "actualizado hace 1 min";
        } else if (ageMinutes < 60) {
            return "actualizado hace " + ageMinutes.format("%d") + " min";
        }

        return "actualizacion antigua";
    }
}

class BiciRadarViewDelegate extends WatchUi.BehaviorDelegate {
    private var _view as BiciRadarView;

    public function initialize(view as BiciRadarView) {
        BehaviorDelegate.initialize();
        _view = view;
    }

    public function onNextPage() as Boolean {
        _view.selectNextStation();
        return true;
    }

    public function onPreviousPage() as Boolean {
        _view.selectPreviousStation();
        return true;
    }

    public function onSelect() as Boolean {
        var station = _view.getSelectedStation();
        if (station == null) {
            return true;
        }

        WatchUi.pushView(new BiciRadarDetailView(station), new BiciRadarDetailDelegate(), WatchUi.SLIDE_UP);
        return true;
    }

    public function onTap(evt as ClickEvent) as Boolean {
        if (WatchUi.CLICK_TYPE_TAP != evt.getType()) {
            return false;
        }

        var stationBeforeTap = _view.getSelectedStation();
        var coords = evt.getCoordinates();
        if (!_view.handleTap(coords[1])) {
            return false;
        }

        var stationAfterTap = _view.getSelectedStation();
        if (stationBeforeTap != null && stationAfterTap != null && stationBeforeTap.id == stationAfterTap.id) {
            WatchUi.pushView(new BiciRadarDetailView(stationAfterTap), new BiciRadarDetailDelegate(), WatchUi.SLIDE_UP);
        }

        return true;
    }
}

class BiciRadarDetailDelegate extends WatchUi.BehaviorDelegate {
    public function initialize() {
        BehaviorDelegate.initialize();
    }

    public function onBack() as Boolean {
        WatchUi.popView(WatchUi.SLIDE_DOWN);
        return true;
    }

    public function onSelect() as Boolean {
        WatchUi.popView(WatchUi.SLIDE_DOWN);
        return true;
    }
}
