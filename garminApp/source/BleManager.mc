using Toybox.Communications;
using Toybox.WatchUi;
using Toybox.System;
using Toybox.Lang;

class BiciRadarBleManager {

    private var _messageCallback as Method(:Communications.PhoneAppMessage) as Void or Null;
    private var _isRegistered as Boolean;
    private var _lastMessageTime as Number;

    public function initialize() {
        _isRegistered = false;
        _lastMessageTime = 0;
        registerForMessages();
    }

    private function registerForMessages() as Void {
        try {
            _messageCallback = method(:onMessageReceived);
            Communications.registerForPhoneAppMessages(_messageCallback);
            _isRegistered = true;
            System.println("BiciRadarBLE: Registered for phone messages");
        } catch (ex) {
            System.println("BiciRadarBLE: Failed to register for messages: " + ex.getMessage());
            _isRegistered = false;
        }
    }

    public function onMessageReceived(message as Communications.PhoneAppMessage) as Void {
        var data = message.data;

        if (data == null) {
            System.println("BiciRadarBLE: Received null data");
            return;
        }

        var now = System.getTimer();
        if (now - _lastMessageTime < 5000) {
            System.println("BiciRadarBLE: Ignoring duplicate message");
            return;
        }
        _lastMessageTime = now;

        if (!(data instanceof Dictionary)) {
            System.println("BiciRadarBLE: Expected Dictionary, got " + data.getType());
            return;
        }

        var stationsData = StationsData.fromDict(data);

        if (stationsData == null) {
            System.println("BiciRadarBLE: Failed to parse stations data");
            return;
        }

        System.println("BiciRadarBLE: Received " + stationsData.backup.size() + " backup stations");

        StorageManager.saveStationsData(stationsData);
        WatchUi.requestUpdate();
    }

    public function isConnected() as Boolean {
        return _isRegistered;
    }

    public function requestRefresh() as Void {
        System.println("BiciRadarBLE: Refresh requested");
    }
}

class BiciRadarMessageListener extends Communications.ConnectionListener {
    public function initialize() {
        ConnectionListener.initialize();
    }

    public function onComplete() as Void {
        System.println("BiciRadarBLE: Message sent successfully");
    }

    public function onError() as Void {
        System.println("BiciRadarBLE: Failed to send message");
    }
}
