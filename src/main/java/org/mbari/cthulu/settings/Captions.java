package org.mbari.cthulu.settings;

import javafx.scene.paint.Color;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.mbari.cthulu.ui.components.settings.Colours.colorToWebString;

/**
 * Settings for video annotation captions.
 */
final public class Captions {

    private double fontSize;

    private String textColour;

    private String backgroundColour;

    /**
     * Create settings with default values.
     */
    public Captions() {
        this.fontSize = 20.0d;
        this.textColour = colorToWebString(Color.WHITE);
        this.backgroundColour = colorToWebString(Color.color(0, 0, 0, 0.5));
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

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("fontSize", fontSize)
            .add("textColour", textColour)
            .add("backgroundColour", backgroundColour)
            .toString();
    }
}
