using Toybox.WatchUi;
using Toybox.Graphics;
using Toybox.System;
using Toybox.Lang;

class BiciRadarWidget extends WatchUi.Widget {

    function initialize() {
        Widget.initialize();
    }

    function onUpdate(dc as Graphics.Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        var station = StorageManager.getNearestStation();

        if (station == null) {
            drawNoData(dc);
            return;
        }

        drawStationCompact(dc, station);
    }

    (:glance)
    function onGlance(dc as Graphics.Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        var station = StorageManager.getNearestStation();

        if (station != null) {
            dc.setColor(getBikeColor(station.bikes), Graphics.COLOR_BLACK);
            dc.drawText(
                dc.getWidth() / 2,
                dc.getHeight() / 2,
                Graphics.FONT_TINY,
                station.bikes.format("%d"),
                Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
            );
        }
    }

    private function drawNoData(dc as Graphics.Dc) as Void {
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.drawText(
            dc.getWidth() / 2,
            dc.getHeight() / 2,
            Graphics.FONT_TINY,
            "—",
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );
    }

    private function drawStationCompact(dc as Graphics.Dc, station as BikeStationModel) as Void {
        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        var shortName = station.name.substring(0, Math.min(station.name.length(), 16));
        dc.drawText(
            screenWidth / 2,
            5,
            Graphics.FONT_TINY,
            shortName,
            Graphics.TEXT_JUSTIFY_CENTER
        );

        var bikeColor = getBikeColor(station.bikes);
        dc.setColor(bikeColor, Graphics.COLOR_BLACK);

        var bikeFont = screenHeight > 240 ? Graphics.FONT_NUMBER_HOT : Graphics.FONT_NUMBER_MEDIUM;
        dc.drawText(
            screenWidth / 2,
            screenHeight / 2 - 5,
            bikeFont,
            station.bikes.format("%d"),
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );

        var distanceStr = formatDistance(station.distance);
        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            screenHeight - 15,
            Graphics.FONT_TINY,
            distanceStr,
            Graphics.TEXT_JUSTIFY_CENTER
        );
    }

    private function getBikeColor(bikes as Number) as Number {
        if (bikes >= 5) {
            return Graphics.COLOR_GREEN;
        } else if (bikes >= 2) {
            return Graphics.COLOR_YELLOW;
        } else {
            return Graphics.COLOR_RED;
        }
    }

    private function formatDistance(meters as Number) as String {
        if (meters < 1000) {
            return meters.format("%d") + "m";
        } else {
            var km = meters / 1000.0;
            return km.format("%.1f") + "km";
        }
    }
}
