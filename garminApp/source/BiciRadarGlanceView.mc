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

        var screenWidth = dc.getWidth();
        var screenHeight = dc.getHeight();

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            screenHeight / 2 - 16,
            Graphics.FONT_TINY,
            WatchUi.loadResource($.Rez.Strings.AppName),
            Graphics.TEXT_JUSTIFY_CENTER
        );

        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            screenHeight / 2 + 6,
            Graphics.FONT_TINY,
            WatchUi.loadResource($.Rez.Strings.OpenApp),
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );
    }
}
