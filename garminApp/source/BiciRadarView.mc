import Toybox.WatchUi;
import Toybox.Graphics;
import Toybox.Time;
import Toybox.Lang;
import Toybox.Application;

class BiciRadarView extends WatchUi.Menu2 {
    private var _focusedStationId = null;
    private var _layoutProfile as LayoutProfile;

    public function initialize() {
        Menu2.initialize({ :title => WatchUi.loadResource($.Rez.Strings.NearbyStationsTitle) });
        _layoutProfile = LayoutProfile.current();
    }

    public function onShow() as Void {
        StorageManager.loadCachedStations();
        if (BiciRadarApp.isDemoMode() && StorageManager.getNearbyStations().size() == 0) {
            StorageManager.loadDemoStations();
        }

        var app = Application.getApp() as BiciRadarApp;
        if (app != null && !BiciRadarApp.isDemoMode()) {
            app.getBleManager().requestRefresh();
        }
        refreshMenuItems();
        WatchUi.requestUpdate();
    }

    public function onUpdate(dc as Dc) as Void {
        _layoutProfile = LayoutProfile.forDc(dc);
        refreshMenuItems();
        Menu2.onUpdate(dc);
    }

    public function reloadStations() as Void {
        refreshMenuItems();
        WatchUi.requestUpdate();
    }

    public function getStationForMenuItem(item as WatchUi.MenuItem) {
        var stationId = item.getId();
        if (stationId == null) {
            return null;
        }

        _focusedStationId = stationId.toString();

        var stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        for (var i = 0; i < stations.size(); i += 1) {
            if (stations[i].id == _focusedStationId) {
                return stations[i];
            }
        }

        return null;
    }

    private function refreshMenuItems() as Void {
        clearMenuItems();

        var stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        if (stations.size() == 0 && BiciRadarApp.isDemoMode()) {
            StorageManager.loadDemoStations();
            stations = StorageManager.getNearbyStations() as Array<BikeStationModel>;
        }

        if (stations.size() == 0) {
            setTitle(WatchUi.loadResource($.Rez.Strings.AppName));
            addItem(new WatchUi.MenuItem(
                WatchUi.loadResource($.Rez.Strings.NoData) as String,
                WatchUi.loadResource($.Rez.Strings.OpenApp) as String,
                "empty",
                null
            ));
            return;
        }

        setTitle(WatchUi.loadResource($.Rez.Strings.NearbyStationsTitle));

        var focusIndex = 0;
        for (var i = 0; i < stations.size(); i += 1) {
            var station = stations[i];
            addItem(new WatchUi.MenuItem(
                truncateStatic(station.name, _layoutProfile.titleMaxChars),
                buildStationSubLabel(station, _layoutProfile),
                station.id,
                null
            ));

            if (_focusedStationId != null && station.id == _focusedStationId) {
                focusIndex = i;
            }
        }

        if (stations.size() > 0) {
            setFocus(focusIndex);
        }
    }

    private function clearMenuItems() as Void {
        while (getItem(0) != null) {
            deleteItem(0);
        }
    }

    private function buildStationSubLabel(station as BikeStationModel, profile as LayoutProfile) as String {
        var label = formatDistance(station.distance) + "  " + station.bikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyBikes) as String);
        if (profile.showListEbikes && station.ebikes > 0) {
            label += "  " + station.ebikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyEbikes) as String);
        }

        return label;
    }

    public static function drawStationSummary(dc as Dc, station as BikeStationModel, isGlance as Boolean) as Void {
        var profile = LayoutProfile.forDc(dc);
        var screenWidth = dc.getWidth();

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);

        if (!isGlance && profile.showSummaryTitle) {
            dc.drawText(
                screenWidth / 2,
                profile.summaryTitleY,
                profile.summaryLabelFont,
                WatchUi.loadResource($.Rez.Strings.AppName),
                Graphics.TEXT_JUSTIFY_CENTER
            );
        }

        var nameY = isGlance ? profile.glanceTitleY : profile.summaryNameY;
        var valueY = isGlance ? profile.glanceValueY : profile.summaryValueY;
        var labelY = isGlance ? profile.glanceFooterY : profile.summaryFooterY;
        var shortName = truncateStatic(station.name, isGlance ? profile.glanceNameMaxChars : profile.summaryNameMaxChars);
        dc.drawText(
            screenWidth / 2,
            nameY,
            isGlance ? profile.glanceTitleFont : profile.summaryLabelFont,
            shortName,
            Graphics.TEXT_JUSTIFY_CENTER
        );

        var bikeColor = getBikeColorStatic(station.bikes);
        dc.setColor(bikeColor, Graphics.COLOR_BLACK);
        dc.drawText(
            screenWidth / 2,
            valueY,
            profile.summaryNumberFont,
            station.bikes.format("%d"),
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        );

        if (isGlance || profile.showSummaryFooter) {
            dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
            var bikesLabel = WatchUi.loadResource($.Rez.Strings.Bikes) as String;
            dc.drawText(
                screenWidth / 2,
                labelY,
                isGlance ? profile.glanceBodyFont : profile.summaryLabelFont,
                bikesLabel,
                Graphics.TEXT_JUSTIFY_CENTER
            );
        }
    }

    public function reloadDemoStations() as Void {
        StorageManager.loadDemoStations();
        reloadStations();
    }

    private function getBikeColor(bikes) {
        return getBikeColorStatic(bikes);
    }

    private function truncateString(str as String, maxLen) as String {
        return truncateStatic(str, maxLen);
    }

    private function formatDistance(meters) as String {
        if (meters < 1000) {
            return meters.format("%d") + (WatchUi.loadResource($.Rez.Strings.MetersUnit) as String);
        } else {
            var km = meters / 1000.0;
            return km.format("%.1f") + (WatchUi.loadResource($.Rez.Strings.KilometersUnit) as String);
        }
    }

    private static function getBikeColorStatic(bikes) {
        if (bikes >= 5) {
            return Graphics.COLOR_GREEN;
        } else if (bikes >= 2) {
            return Graphics.COLOR_YELLOW;
        } else {
            return Graphics.COLOR_RED;
        }
    }

    private static function truncateStatic(str as String, maxLen) as String {
        if (str.length() <= maxLen) {
            return str;
        }
        return str.substring(0, maxLen - 1) + "...";
    }

    public static function bikeColorFor(bikes) {
        return getBikeColorStatic(bikes);
    }

    public static function truncateStationName(str as String, maxLen) as String {
        return truncateStatic(str, maxLen);
    }

    public static function truncateText(str as String, maxLen) as String {
        return truncateStatic(str, maxLen);
    }

    public static function formatStationDistance(meters) as String {
        if (meters < 1000) {
            return meters.format("%d") + (WatchUi.loadResource($.Rez.Strings.MetersUnit) as String);
        }

        var km = meters / 1000.0;
        return km.format("%.1f") + (WatchUi.loadResource($.Rez.Strings.KilometersUnit) as String);
    }
}

class BiciRadarDetailView extends WatchUi.View {
    private var _station;
    private var _layoutProfile as LayoutProfile;
    private var _buttonX = 0;
    private var _buttonY = 0;
    private var _buttonWidth = 0;
    private var _buttonHeight = 0;
    private var _detailScrollOffset = 0;

    public function initialize(station) {
        WatchUi.View.initialize();
        _station = station;
        _layoutProfile = LayoutProfile.current();
    }

    public function onLayout(dc as Dc) as Void {
        _layoutProfile = LayoutProfile.forDc(dc);
        setLayout(buildLayout(dc, _layoutProfile));
    }

    public function onShow() as Void {
    }

    public function onHide() as Void {
    }

    public function onUpdate(dc as Dc) as Void {
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.clear();
        refreshLayoutContent();
        View.onUpdate(dc);
    }

    public function getStationId() as String {
        return _station.id;
    }

    public function scrollDetails(step as Number) as Boolean {
        var lines = buildDetailLines(_layoutProfile);
        var maxOffset = lines.size() - visibleMetricLineCount(_layoutProfile);
        if (maxOffset < 0) {
            maxOffset = 0;
        }

        var newOffset = _detailScrollOffset + step;
        if (newOffset < 0) {
            newOffset = 0;
        } else if (newOffset > maxOffset) {
            newOffset = maxOffset;
        }

        if (newOffset == _detailScrollOffset) {
            return false;
        }

        _detailScrollOffset = newOffset;
        WatchUi.requestUpdate();
        return true;
    }

    public function isTapInsideOpenRoute(clickEvent as WatchUi.ClickEvent) as Boolean {
        var coordinates = clickEvent.getCoordinates();
        if (coordinates == null || coordinates.size() < 2) {
            return false;
        }

        var x = coordinates[0];
        var y = coordinates[1];
        return x >= _buttonX && x <= (_buttonX + _buttonWidth) && y >= _buttonY && y <= (_buttonY + _buttonHeight);
    }

    private function buildDetailLines(profile as LayoutProfile) as Array<String> {
        var lines = [] as Array<String>;
        lines.add(buildMetricLine(WatchUi.loadResource($.Rez.Strings.DetailStatus) as String, statusText()));

        if (profile.showDetailEbikes) {
            lines.add(buildMetricLine(WatchUi.loadResource($.Rez.Strings.DetailEbikes) as String, ebikeText()));
        }

        lines.add(buildMetricLine(WatchUi.loadResource($.Rez.Strings.DetailDistance) as String, BiciRadarView.formatStationDistance(_station.distance)));

        if (profile.showUpdatedInDetail) {
            lines.add(updatedText());
        }

        return lines;
    }

    private function buildMetricLine(label as String, value as String) as String {
        return label + ": " + value;
    }

    private function visibleMetricLineCount(profile as LayoutProfile) as Number {
        if (profile.family == "small_round") {
            return 2;
        } else if (profile.family == "mid_round") {
            return 3;
        }

        return 4;
    }

    private function statusText() as String {
        if (_station.bikes >= 5) {
            return WatchUi.loadResource($.Rez.Strings.AvailabilityHigh);
        } else if (_station.bikes >= 2) {
            return WatchUi.loadResource($.Rez.Strings.AvailabilityMedium);
        }

        return WatchUi.loadResource($.Rez.Strings.AvailabilityLow);
    }

    private function availabilityText() as String {
        if (_station.bikes == 1) {
            return WatchUi.loadResource($.Rez.Strings.OneBike);
        }

        return _station.bikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyBikes) as String);
    }

    private function ebikeText() as String {
        if (_station.ebikes == 1) {
            return WatchUi.loadResource($.Rez.Strings.OneEbike);
        }

        return _station.ebikes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.ManyEbikes) as String);
    }

    private function updatedText() as String {
        var lastUpdate = StorageManager.getLastUpdateTime();
        if (lastUpdate == null) {
            return WatchUi.loadResource($.Rez.Strings.UpdatedNoData);
        }

        var ageMinutes = ((Time.now().value() - lastUpdate) / 60);
        if (ageMinutes < 1) {
            return WatchUi.loadResource($.Rez.Strings.UpdatedNow);
        } else if (ageMinutes == 1) {
            return WatchUi.loadResource($.Rez.Strings.UpdatedOneMinute);
        } else if (ageMinutes < 60) {
            return (WatchUi.loadResource($.Rez.Strings.UpdatedManyMinutesPrefix) as String) + " " + ageMinutes.format("%d") + " " + (WatchUi.loadResource($.Rez.Strings.UpdatedManyMinutesSuffix) as String);
        }

        return WatchUi.loadResource($.Rez.Strings.UpdatedOld);
    }

    private function buildLayout(dc as Dc, profile as LayoutProfile) as Array<WatchUi.Drawable> {
        var buttonFont = profile.detailBodyFont;
        var buttonLabel = BiciRadarView.truncateText(
            WatchUi.loadResource($.Rez.Strings.DetailOpenRouteButton) as String,
            profile.detailHintMaxChars
        );
        var buttonHeight = Graphics.getFontHeight(buttonFont) + 10;
        if (profile.family == "small_round") {
            buttonHeight = Graphics.getFontHeight(buttonFont) + 6;
        }
        var buttonWidth = dc.getTextWidthInPixels(buttonLabel, buttonFont) + 20;
        var maxButtonWidth = dc.getWidth() - 24;
        if (buttonWidth > maxButtonWidth) {
            buttonWidth = maxButtonWidth;
        }

        var buttonX = (dc.getWidth() - buttonWidth) / 2;
        var bottomPadding = dc.getHeight() / 8;
        if (bottomPadding < 16) {
            bottomPadding = 16;
        }
        var buttonY = dc.getHeight() - buttonHeight - bottomPadding;
        var numberFont = profile.summaryNumberFont;
        var numberHeight = Graphics.getFontHeight(numberFont);
        var bodyFontHeight = Graphics.getFontHeight(profile.detailBodyFont);
        var secondaryFont = Graphics.FONT_XTINY;
        var secondaryFontHeight = Graphics.getFontHeight(secondaryFont);
        var metricBaseY = profile.detailBodyStartY + numberHeight + secondaryFontHeight + 10;
        var metricLineStep = bodyFontHeight + 3;
        var visibleMetricCount = visibleMetricLineCount(profile);
        var minimumButtonY = metricBaseY + (metricLineStep * visibleMetricCount) + 6;
        if (buttonY < minimumButtonY) {
            buttonY = minimumButtonY;
        }

        _buttonX = buttonX;
        _buttonY = buttonY;
        _buttonWidth = buttonWidth;
        _buttonHeight = buttonHeight;

        var bikesLabelFont = Graphics.FONT_XTINY;
        var bikesValueY = profile.detailBodyStartY;
        var bikesLabelY = profile.detailBodyStartY + numberHeight - 2;
        var metricLine1Y = metricBaseY;
        var metricLine2Y = metricBaseY + metricLineStep;
        var metricLine3Y = metricBaseY + (metricLineStep * 2);
        var metricLine4Y = metricBaseY + (metricLineStep * 3);

        if (profile.family == "small_round") {
            bikesLabelY = profile.detailBodyStartY + numberHeight - 4;
            metricLine1Y = bikesLabelY + secondaryFontHeight + 2;
            metricLine2Y = metricLine1Y + bodyFontHeight;
            metricLine3Y = metricLine2Y + bodyFontHeight;
            metricLine4Y = metricLine3Y + bodyFontHeight;
        }

        var titleText = new WatchUi.Text({
            :identifier => "TitleLabel",
            :text => "",
            :color => Graphics.COLOR_WHITE,
            :font => profile.detailTitleFont,
            :locX => dc.getWidth() / 2,
            :locY => profile.detailTitleY,
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        var bikesValue = new WatchUi.Text({
            :identifier => "BikesValue",
            :text => "",
            :color => BiciRadarView.bikeColorFor(_station.bikes),
            :font => numberFont,
            :locX => dc.getWidth() / 2,
            :locY => bikesValueY,
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        var bikesLabel = new WatchUi.Text({
            :identifier => "BikesLabel",
            :text => "",
            :color => Graphics.COLOR_DK_GRAY,
            :font => bikesLabelFont,
            :locX => dc.getWidth() / 2,
            :locY => bikesLabelY,
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        var metricLine1 = new WatchUi.Text({
            :identifier => "MetricLine1",
            :text => "",
            :color => Graphics.COLOR_WHITE,
            :font => profile.detailBodyFont,
            :locX => dc.getWidth() / 2,
            :locY => metricLine1Y,
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        var metricLine2 = new WatchUi.Text({
            :identifier => "MetricLine2",
            :text => "",
            :color => Graphics.COLOR_WHITE,
            :font => profile.detailBodyFont,
            :locX => dc.getWidth() / 2,
            :locY => metricLine2Y,
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        var metricLine3 = new WatchUi.Text({
            :identifier => "MetricLine3",
            :text => "",
            :color => Graphics.COLOR_WHITE,
            :font => profile.detailBodyFont,
            :locX => dc.getWidth() / 2,
            :locY => metricLine3Y,
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        var metricLine4 = new WatchUi.Text({
            :identifier => "MetricLine4",
            :text => "",
            :color => Graphics.COLOR_WHITE,
            :font => profile.detailBodyFont,
            :locX => dc.getWidth() / 2,
            :locY => metricLine4Y,
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        var button = new WatchUi.Button({
            :identifier => "OpenRouteButton",
            :behavior => :onOpenRoute,
            :locX => buttonX,
            :locY => buttonY,
            :width => buttonWidth,
            :height => buttonHeight,
            :background => Graphics.COLOR_BLACK,
            :stateDefault => Graphics.COLOR_WHITE,
            :stateHighlighted => Graphics.COLOR_LT_GRAY,
            :stateSelected => Graphics.COLOR_LT_GRAY,
            :stateDisabled => Graphics.COLOR_DK_GRAY
        });

        var buttonText = new WatchUi.Text({
            :identifier => "OpenRouteButtonLabel",
            :text => "",
            :color => Graphics.COLOR_BLACK,
            :font => buttonFont,
            :locX => dc.getWidth() / 2,
            :locY => buttonY + ((buttonHeight - Graphics.getFontHeight(buttonFont)) / 2),
            :justification => Graphics.TEXT_JUSTIFY_CENTER
        });

        return [titleText, bikesValue, bikesLabel, metricLine1, metricLine2, metricLine3, metricLine4, button, buttonText];
    }

    private function refreshLayoutContent() as Void {
        setTextDrawable(
            "TitleLabel",
            BiciRadarView.truncateStationName(_station.name, _layoutProfile.detailNameMaxChars)
        );
        setTextDrawable("BikesValue", _station.bikes.format("%d"));
        setTextDrawable("BikesLabel", availabilityText());
        var detailLines = buildDetailLines(_layoutProfile);
        var visibleCount = visibleMetricLineCount(_layoutProfile);
        setTextDrawable("MetricLine1", detailLineAt(detailLines, 0, visibleCount));
        setTextDrawable("MetricLine2", detailLineAt(detailLines, 1, visibleCount));
        setTextDrawable("MetricLine3", detailLineAt(detailLines, 2, visibleCount));
        setTextDrawable("MetricLine4", detailLineAt(detailLines, 3, visibleCount));
        setTextDrawable(
            "OpenRouteButtonLabel",
            BiciRadarView.truncateText(
                WatchUi.loadResource($.Rez.Strings.DetailOpenRouteButton) as String,
                _layoutProfile.detailHintMaxChars
            )
        );
    }

    private function detailLineAt(lines as Array<String>, visibleIndex as Number, visibleCount as Number) as String {
        if (visibleIndex >= visibleCount) {
            return "";
        }

        var lineIndex = _detailScrollOffset + visibleIndex;
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            return "";
        }

        return lines[lineIndex];
    }

    private function setTextDrawable(id as String, text as String) as Void {
        var drawable = findDrawableById(id);
        if (drawable == null) {
            return;
        }

        if (drawable instanceof WatchUi.TextArea) {
            (drawable as WatchUi.TextArea).setText(text);
            return;
        }

        if (drawable instanceof WatchUi.Text) {
            (drawable as WatchUi.Text).setText(text);
        }
    }
}

class BiciRadarViewDelegate extends WatchUi.Menu2InputDelegate {
    private var _view as BiciRadarView;

    public function initialize(view as BiciRadarView) {
        Menu2InputDelegate.initialize();
        _view = view;
    }

    public function onSelect(item as WatchUi.MenuItem) as Void {
        var station = _view.getStationForMenuItem(item);
        if (station != null) {
            var detailView = new BiciRadarDetailView(station);
            WatchUi.pushView(detailView, new BiciRadarDetailDelegate(detailView), WatchUi.SLIDE_UP);
        }
    }

    public function onBack() as Void {
        _view.reloadStations();
    }

    public function onNextPage() as Boolean {
        _view.reloadStations();
        return false;
    }

    public function onPreviousPage() as Boolean {
        _view.reloadStations();
        return false;
    }

    public function onWrap(key as WatchUi.Key) as Boolean {
        _view.reloadStations();
        return true;
    }

    public function onMenu() as Boolean {
        _view.reloadDemoStations();
        return true;
    }
}

class BiciRadarDetailDelegate extends WatchUi.BehaviorDelegate {
    private var _view as BiciRadarDetailView;

    public function initialize(view as BiciRadarDetailView) {
        BehaviorDelegate.initialize();
        _view = view;
    }

    public function onBack() as Boolean {
        WatchUi.popView(WatchUi.SLIDE_DOWN);
        return true;
    }

    public function onOpenRoute() as Boolean {
        return triggerOpenRoute();
    }

    public function onTap(clickEvent as WatchUi.ClickEvent) as Boolean {
        if (_view.isTapInsideOpenRoute(clickEvent)) {
            return triggerOpenRoute();
        }

        return false;
    }

    public function onKey(keyEvent as WatchUi.KeyEvent) as Boolean {
        var key = keyEvent.getKey();
        if (key == WatchUi.KEY_DOWN) {
            return _view.scrollDetails(1);
        } else if (key == WatchUi.KEY_UP) {
            return _view.scrollDetails(-1);
        }

        return false;
    }

    public function onSwipe(swipeEvent as WatchUi.SwipeEvent) as Boolean {
        var direction = swipeEvent.getDirection();
        if (direction == WatchUi.SWIPE_UP) {
            return _view.scrollDetails(1);
        } else if (direction == WatchUi.SWIPE_DOWN) {
            return _view.scrollDetails(-1);
        }

        return false;
    }

    public function onNextPage() as Boolean {
        return _view.scrollDetails(1);
    }

    public function onPreviousPage() as Boolean {
        return _view.scrollDetails(-1);
    }

    public function onSelect() as Boolean {
        return triggerOpenRoute();
    }

    private function triggerOpenRoute() as Boolean {
        if (BiciRadarApp.isDemoMode()) {
            WatchUi.showToast(WatchUi.loadResource($.Rez.Strings.ToastDemoNoPhone), null);
            return true;
        }

        var app = Application.getApp() as BiciRadarApp;
        if (app.getBleManager().requestRouteToStation(_view.getStationId())) {
            WatchUi.showToast(WatchUi.loadResource($.Rez.Strings.ToastRouteSent), null);
            return true;
        }

        WatchUi.showToast(WatchUi.loadResource($.Rez.Strings.ToastNoPhoneConnection), null);
        return true;
    }
}
