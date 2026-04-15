using Toybox.Application;
using Toybox.WatchUi;
using Toybox.Communications;
using Toybox.System;
using Toybox.Lang;

class BiciRadarApp extends WatchUi.Application.AppBase {

    private var _mainView as BiciRadarView?;
    private var _bleManager as BiciRadarBleManager?;

    function initialize() {
        AppBase.initialize();
        _bleManager = new BiciRadarBleManager();
    }

    function onStart(state as Dictionary?) as Void {
        StorageManager.loadCachedStations();
    }

    function onStop() as Void {
    }

    function getInitialView() as Array<Views or InputDelegates>? {
        _mainView = new BiciRadarView();
        return [ _mainView ] as Array<Views or InputDelegates>;
    }

    function getWidget() as WatchUi.Widget {
        return new BiciRadarWidget();
    }

    function getBleManager() as BiciRadarBleManager {
        return _bleManager!;
    }

    (:background)
    function getServiceDelegate() as Array<System.ServiceDelegate>? {
        return [ new BiciRadarServiceDelegate() ] as Array<System.ServiceDelegate>;
    }
}
