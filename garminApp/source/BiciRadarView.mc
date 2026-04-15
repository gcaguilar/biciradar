import Toybox.WatchUi;
import Toybox.Graphics;
import Toybox.System;
import Toybox.Time;
import Toybox.Lang;

class BiciRadarView extends WatchUi.View {

    public function initialize() {
        WatchUi.View.initialize();
    }

    public function onLayout(dc as Dc) as Void {
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

    private function drawStation(dc as Dc, station as BikeStationModel) as Void {
        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);

        dc.drawText(
            screenWidth / 2,
            20,
            Graphics.FONT_TINY,
            "BiciRadar",
            Graphics.TEXT_JUSTIFY_CENTER
        );

        var shortName = truncateString(station.name, 18);
        dc.drawText(
            screenWidth / 2,
            screenHeight / 2 - 40,
            Graphics.FONT_TINY,
            shortName,
            Graphics.TEXT_JUSTIFY_CENTER
        );

        var bikeColor = getBikeColor(station.bikes);
        dc.setColor(bikeColor, Graphics.COLOR_BLACK);

        var bikesText = station.bikes.format("%d");
        var bikeFont = Graphics.FONT_NUMBER_MEDIUM;
        dc.drawText(
            screenWidth / 2,
            screenHeight / 2 - 10,
            bikeFont,
            bikesText,
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        var bikesLabel = WatchUi.loadResource($.Rez.Strings.Bikes) as String;
        dc.drawText(
            screenWidth / 2,
            screenHeight / 2 + 30,
            Graphics.FONT_TINY,
            bikesLabel,
            Graphics.TEXT_JUSTIFY_CENTER
        );

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
        if (bikes >= 5) {
            return Graphics.COLOR_GREEN;
        } else if (bikes >= 2) {
            return Graphics.COLOR_YELLOW;
        } else {
            return Graphics.COLOR_RED;
        }
    }

    private function truncateString(str as String, maxLen) as String {
        if (str.length() <= maxLen) {
            return str;
        }
        return str.substring(0, maxLen - 1) + "...";
    }

    private function formatDistance(meters) as String {
        if (meters < 1000) {
            return meters.format("%d") + "m";
        } else {
            var km = meters / 1000.0;
            return km.format("%.1f") + "km";
        }
    }
}
