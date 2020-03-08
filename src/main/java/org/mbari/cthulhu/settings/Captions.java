package org.mbari.cthulhu.settings;

import javafx.scene.paint.Color;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.mbari.cthulhu.ui.components.settings.Colours.colorToWebString;

/**
 * Settings for video annotation captions.
 */
final public class Captions {

    private double fontSize;

    private String textColour;

    private String backgroundColour;

    private String defaultValue;

    /**
     * Create settings with default values.
     */
    public Captions() {
        this.fontSize = 20.0d;
        this.textColour = colorToWebString(Color.WHITE);
        this.backgroundColour = colorToWebString(Color.color(0, 0, 0, 0.5));
        this.defaultValue = "Concept";
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Captions(Captions from) {
        this.fontSize = from.fontSize;
        this.textColour = from.textColour;
        this.backgroundColour = from.backgroundColour;
        this.defaultValue = from.defaultValue;
    }

    public double fontSize() {
        return fontSize;
    }

    public void fontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public String textColour() {
        return textColour;
    }

    public void textColour(String textColour) {
        this.textColour = textColour;
    }

    public String backgroundColour() {
        return backgroundColour;
    }

    public void backgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public void defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("fontSize", fontSize)
            .add("textColour", textColour)
            .add("backgroundColour", backgroundColour)
            .add("defaultValue", defaultValue)
            .toString();
    }
}
