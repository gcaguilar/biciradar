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

    public function onMessageError(error as Communications.PhoneAppMessageError) as Void {
    }

    public function isConnected() {
        return _isRegistered;
    }

    public function requestRefresh() as Void {
        if (!(Communications has :transmit)) {
            return;
        }

        try {
            var refreshRequest = {
                "type" => "refresh_request",
                "requestedAt" => System.getTimer()
            };
            Communications.transmit(
                refreshRequest,
                null,
                new BiciRadarTransmitListener()
            );
        } catch (ex) {
        }
    }

    public function requestRouteToStation(stationId) {
        if (!(Communications has :transmit) || stationId.length() == 0) {
            return false;
        }

        try {
            var routeRequest = {
                "type" => "open_route",
                "stationId" => stationId,
                "requestedAt" => System.getTimer()
            };
            Communications.transmit(
                routeRequest,
                null,
                new BiciRadarTransmitListener()
            );
            return true;
        } catch (ex) {
            return false;
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
