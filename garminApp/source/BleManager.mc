using Toybox.Communications;
using Toybox.WatchUi;
using Toybox.System;

class BiciRadarBleManager {

    private var _messageCallback;
    private var _isRegistered = false;

    public function initialize() {
        registerForMessages();
    }

    private function registerForMessages() as Void {
        if (!(Communications has :registerForPhoneAppMessages)) {
            return;
        }

        try {
            _messageCallback = method(:onMessageReceived);
            Communications.registerForPhoneAppMessages(_messageCallback);
            _isRegistered = true;
        } catch (ex) {
            _isRegistered = false;
        }
    }

    public function onMessageReceived(msg as Communications.PhoneAppMessage) as Void {
        var data = msg.data;
        if (data == null) {
            return;
        }

        if (data instanceof Dictionary) {
            StorageManager.saveRawStationsData(data);
            WatchUi.requestUpdate();
        }
    }

    public function isConnected() {
        return _isRegistered;
    }

    public function requestRefresh() as Void {
    }
}
