using Toybox.Communications;
using Toybox.WatchUi;
using Toybox.System;
using Toybox.Lang;

class BiciRadarBleManager {

    private var _messageCallback;
    private var _messageErrorCallback;
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
            if (Communications has :registerForPhoneAppMessageErrors) {
                _messageErrorCallback = method(:onMessageError);
                Communications.registerForPhoneAppMessageErrors(_messageErrorCallback);
            }
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

    public function onMessageError(error) as Void {
    }

    public function isConnected() {
        return _isRegistered;
    }

    public function requestRefresh() as Void {
        if (!(Communications has :transmit)) {
            return;
        }

        try {
            Communications.transmit(
                {
                    :type => "refresh_request",
                    :requestedAt => System.getTimer()
                },
                null,
                new BiciRadarTransmitListener()
            );
        } catch (ex) {
        }
    }
}

class BiciRadarTransmitListener extends Communications.ConnectionListener {
    public function initialize() {
        ConnectionListener.initialize();
    }

    public function onComplete() as Void {
    }

    public function onError() as Void {
    }
}
