package org.mbari.cthulhu.settings;

import javafx.scene.paint.Color;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.mbari.cthulhu.ui.components.settings.Colours.colorToWebString;

/**
 * Settings for video annotation display.
 */
final public class Display {

    private int borderSize;

    private String borderColour;

    private int timeWindowMillis;

    private String decayBorderColour;

    /**
     * Create settings with default values.
     */
    public Display() {
        this.borderSize = 6;
        this.borderColour = colorToWebString(Color.ANTIQUEWHITE);
        this.timeWindowMillis = 2000;
        this.decayBorderColour = colorToWebString(Color.color(1.0d, 1.0d, 1.0d, 0.0d));
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Display(Display from) {
        this.borderSize = from.borderSize;
        this.borderColour = from.borderColour;
        this.timeWindowMillis = from.timeWindowMillis;
        this.decayBorderColour = from.decayBorderColour;
    }

    public int borderSize() {
        return borderSize;
    }

    public void borderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public String borderColour() {
        return borderColour;
    }

    public void borderColour(String borderColour) {
        this.borderColour = borderColour;
    }

    public int timeWindowMillis() {
        return timeWindowMillis;
    }

    public void timeWindowMillis(int timeWindowMillis) {
        this.timeWindowMillis = timeWindowMillis;
    }

    public String decayBorderColour() {
        return decayBorderColour;
    }

    public void decayBorderColour(String decayBorderColour) {
        this.decayBorderColour = decayBorderColour;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("borderSize", borderSize)
            .add("borderColour", borderColour)
            .add("timeWindowMillis", timeWindowMillis)
            .add("decayBorderColour", decayBorderColour)
            .toString();
    }
}
