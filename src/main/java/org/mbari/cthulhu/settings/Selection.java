package org.mbari.cthulhu.settings;

import javafx.scene.paint.Color;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.mbari.cthulhu.ui.components.settings.Colours.colorToWebString;

/**
 * Settings for video annotation display.
 */
final public class Selection {

    private int borderSize;

    private String borderColour;

    /**
     * Create settings with default values.
     */
    public Selection() {
        this.borderSize = 6;
        this.borderColour = colorToWebString(Color.YELLOW);
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Selection(Selection from) {
        this.borderSize = from.borderSize;
        this.borderColour = from.borderColour;
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

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("borderSize", borderSize)
            .add("borderColour", borderColour)
            .toString();
    }
}
