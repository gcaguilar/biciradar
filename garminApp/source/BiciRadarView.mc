import Toybox.WatchUi;
import Toybox.Graphics;
import Toybox.System;
import Toybox.Time;
import Toybox.Lang;
import Toybox.Application;

class BiciRadarView extends WatchUi.View {

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
        WatchUi.requestUpdate();
    }

    public function onUpdate(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        var station = StorageManager.getNearestStation();

        if (station == null) {
            drawNoData(dc);
            return;
        }

        drawStation(dc, station);
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

    private function drawStation(dc as Dc, station as BikeStationModel) as Void {
        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();

        drawStationSummary(dc, station, false);

        var distanceStr = formatDistance(station.distance);
        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            screenHeight - 30,
            Graphics.FONT_TINY,
            distanceStr,
            Graphics.TEXT_JUSTIFY_CENTER
        );

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
                screenHeight - 15,
                Graphics.FONT_TINY,
                updateText,
                Graphics.TEXT_JUSTIFY_CENTER
            );
        }
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
}

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
