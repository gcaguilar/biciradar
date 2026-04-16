import Toybox.Application;
import Toybox.Lang;
import Toybox.WatchUi;

const ENABLE_DEMO_DATA = true;

class BiciRadarApp extends Application.AppBase {

    public static function isDemoMode() as Boolean {
        return ENABLE_DEMO_DATA;
    }

    private var _mainView;
    private var _glanceView;
    private var _bleManager;

    public function initialize() {
        AppBase.initialize();
    }

    public function onStart(state as Dictionary?) as Void {
    }

    public function onStop(state as Dictionary?) as Void {
    }

    public function getInitialView() as [Views] or [Views, InputDelegates] {
        _mainView = new BiciRadarView();
        return [ _mainView, new BiciRadarViewDelegate(_mainView) ];
    }

    public function getGlanceView() as [WatchUi.GlanceView] or [WatchUi.GlanceView, WatchUi.GlanceViewDelegate] or Null {
        _glanceView = new BiciRadarGlanceView();
        return [ _glanceView ];
    }

    public function getBleManager() {
        if (_bleManager == null) {
            _bleManager = new BiciRadarBleManager();
        }

        return _bleManager;
    }
}
