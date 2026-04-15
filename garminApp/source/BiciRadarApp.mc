import Toybox.Application;
import Toybox.WatchUi;
import Toybox.Communications;
import Toybox.System;
import Toybox.Lang;

class BiciRadarApp extends Application.AppBase {

    private var _mainView as BiciRadarView?;
    private var _bleManager as BiciRadarBleManager?;

    public function initialize() {
        AppBase.initialize();
        _bleManager = new BiciRadarBleManager();
    }

    public function onStart(state as Dictionary?) as Void {
        StorageManager.loadCachedStations();
    }

    public function onStop(state as Dictionary?) as Void {
    }

    public function getInitialView() as [Views] or [Views, InputDelegates] {
        _mainView = new BiciRadarView();
        return [ _mainView ];
    }

    public function getBleManager() as BiciRadarBleManager {
        return _bleManager;
    }
}
