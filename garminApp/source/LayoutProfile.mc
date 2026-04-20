import Toybox.Graphics;
import Toybox.Lang;
import Toybox.System;
import Toybox.WatchUi;

class LayoutProfile {
    public var family;
    public var screenWidth;
    public var screenHeight;
    public var titleMaxChars;
    public var summaryNameMaxChars;
    public var glanceNameMaxChars;
    public var detailNameMaxChars;
    public var detailHintMaxChars;
    public var showListEbikes;
    public var showDetailEbikes;
    public var showUpdatedInDetail;
    public var showSummaryTitle;
    public var showSummaryFooter;
    public var detailTitleFont;
    public var detailBodyFont;
    public var glanceTitleFont;
    public var glanceBodyFont;
    public var summaryLabelFont;
    public var summaryNumberFont;
    public var glanceTitleY;
    public var glanceValueY;
    public var glanceFooterY;
    public var summaryTitleY;
    public var summaryNameY;
    public var summaryValueY;
    public var summaryFooterY;
    public var detailTitleY;
    public var detailBodyStartY;
    public var detailLineSpacing;
    public var detailSectionGap;

    public function initialize(layoutFamily as String, width as Number, height as Number) {
        family = layoutFamily;
        screenWidth = width;
        screenHeight = height;

        titleMaxChars = 22;
        summaryNameMaxChars = 18;
        glanceNameMaxChars = 14;
        detailNameMaxChars = 22;
        detailHintMaxChars = 26;
        showListEbikes = true;
        showDetailEbikes = true;
        showUpdatedInDetail = true;
        showSummaryTitle = true;
        showSummaryFooter = true;
        detailTitleFont = Graphics.FONT_TINY;
        detailBodyFont = Graphics.FONT_TINY;
        glanceTitleFont = Graphics.FONT_TINY;
        glanceBodyFont = Graphics.FONT_TINY;
        summaryLabelFont = Graphics.FONT_TINY;
        summaryNumberFont = Graphics.FONT_NUMBER_MEDIUM;

        if (layoutFamily == "small_round") {
            titleMaxChars = 18;
            summaryNameMaxChars = 12;
            glanceNameMaxChars = 10;
            detailNameMaxChars = 16;
            detailHintMaxChars = 16;
            showListEbikes = false;
            showDetailEbikes = false;
            showUpdatedInDetail = false;
            showSummaryTitle = false;
            showSummaryFooter = false;
            detailTitleFont = Graphics.FONT_XTINY;
            detailBodyFont = Graphics.FONT_XTINY;
            glanceTitleFont = Graphics.FONT_XTINY;
            glanceBodyFont = Graphics.FONT_XTINY;
            summaryLabelFont = Graphics.FONT_XTINY;
            summaryNumberFont = Graphics.FONT_NUMBER_MILD;
        } else if (layoutFamily == "mid_round") {
            titleMaxChars = 20;
            summaryNameMaxChars = 15;
            glanceNameMaxChars = 12;
            detailNameMaxChars = 18;
            detailHintMaxChars = 20;
            showListEbikes = false;
            showDetailEbikes = false;
            summaryNumberFont = Graphics.FONT_NUMBER_MILD;
        } else if (layoutFamily == "mid_large_round") {
            titleMaxChars = 22;
            summaryNameMaxChars = 17;
            glanceNameMaxChars = 13;
            detailNameMaxChars = 22;
            detailHintMaxChars = 24;
        } else if (layoutFamily == "large_round_amoled") {
            titleMaxChars = 24;
            summaryNameMaxChars = 20;
            glanceNameMaxChars = 16;
            detailNameMaxChars = 24;
            detailHintMaxChars = 28;
        }

        glanceTitleY = centeredSlotY(1, 3, glanceTitleFont);
        glanceValueY = centeredSlotY(2, 3, summaryNumberFont);
        glanceFooterY = centeredSlotY(3, 3, glanceBodyFont);

        var summarySlotCount = showSummaryTitle ? 4 : 3;
        summaryTitleY = centeredSlotY(1, summarySlotCount, summaryLabelFont);
        summaryNameY = centeredSlotY(showSummaryTitle ? 2 : 1, summarySlotCount, summaryLabelFont);
        summaryValueY = centeredSlotY(showSummaryTitle ? 3 : 2, summarySlotCount, summaryNumberFont);
        summaryFooterY = centeredSlotY(showSummaryTitle ? 4 : 3, summarySlotCount, summaryLabelFont);

        var titleTopPadding = screenHeight / 14;
        if (layoutFamily == "small_round") {
            titleTopPadding = screenHeight / 18;
        }

        detailTitleY = titleTopPadding;
        detailBodyStartY = detailTitleY + Graphics.getFontHeight(detailTitleFont) + (screenHeight / 18);
        detailLineSpacing = Graphics.getFontHeight(detailBodyFont) + (screenHeight / 40);
        detailSectionGap = screenHeight / 30;
    }

    public static function current() as LayoutProfile {
        var settings = System.getDeviceSettings();
        return new LayoutProfile(loadFamilyKey(), settings.screenWidth, settings.screenHeight);
    }

    public static function forDc(dc as Dc) as LayoutProfile {
        return new LayoutProfile(loadFamilyKey(), dc.getWidth(), dc.getHeight());
    }

    private static function loadFamilyKey() as String {
        var rawValue = WatchUi.loadResource($.Rez.Strings.LayoutFamily);
        if (rawValue != null) {
            return rawValue as String;
        }

        return "mid_large_round";
    }

    private function centeredSlotY(slotIndex as Number, slotCount as Number, font) as Number {
        var slotHeight = screenHeight / slotCount;
        var slotCenterY = (slotHeight * (slotIndex - 1)) + (slotHeight / 2);
        return slotCenterY - (Graphics.getFontHeight(font) / 2);
    }
}
