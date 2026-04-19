import Toybox.WatchUi;
import Toybox.Graphics;
import Toybox.Lang;

(:glance)
class BiciRadarGlanceView extends WatchUi.GlanceView {
    public function initialize() {
        GlanceView.initialize();
    }

    public function onUpdate(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();

        StorageManager.loadCachedStations();
        if (BiciRadarApp.isDemoMode() && StorageManager.getNearbyStations().size() == 0) {
            StorageManager.loadDemoStations();
        }

        var stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        if (stations.size() > 0) {
            BiciRadarView.drawStationSummary(dc, stations[0], true);
            return;
        }

        var profile = LayoutProfile.forDc(dc);
        var screenWidth = dc.getWidth();

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            profile.glanceTitleY,
            profile.glanceTitleFont,
            WatchUi.loadResource($.Rez.Strings.AppName),
            Graphics.TEXT_JUSTIFY_CENTER
        );

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            profile.glanceFooterY,
            profile.glanceBodyFont,
            WatchUi.loadResource($.Rez.Strings.OpenApp),
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );
    }
}
