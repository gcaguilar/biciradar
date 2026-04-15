using Toybox.System;
using Toybox.Lang;
using Toybox.WatchUi;

(:background)
class BiciRadarServiceDelegate extends System.ServiceDelegate {

    public function initialize() {
        ServiceDelegate.initialize();
    }

    public function onTemporalEvent() as Void {
        System.println("BiciRadarService: Temporal event triggered");

        var bleManager = Application.getApp().getBleManager();
        bleManager.requestRefresh();
    }
}
