package org.mbari.cthulu.settings;

import javafx.scene.paint.Color;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.mbari.cthulu.ui.components.settings.Colours.colorToWebString;

/**
 * Settings for video annotation creation.
 */
final public class Creation {

    private boolean enableCursor;

    private int cursorSize;

    private String cursorColour;

    private int borderSize;

    private String borderColour;

    /**
     * Create settings with default values.
     */
    public Creation() {
        this.enableCursor = true;
        this.cursorSize = 6;
        this.cursorColour = colorToWebString(Color.WHITE);
        this.borderSize = 6;
        this.borderColour = colorToWebString(Color.WHITE);
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public Creation(Creation from) {
        this.enableCursor = from.enableCursor;
        this.cursorSize = from.cursorSize;
        this.cursorColour = from.cursorColour;
        this.borderSize = from.borderSize;
        this.borderColour = from.borderColour;
    }

    public boolean enableCursor() {
        return enableCursor;
    }

    public void enableCursor(boolean enableCursor) {
        this.enableCursor = enableCursor;
    }

    public int cursorSize() {
        return cursorSize;
    }

    public void cursorSize(int cursorSize) {
        this.cursorSize = cursorSize;
    }

    public String cursorColour() {
        return cursorColour;
    }

    public void cursorColour(String cursorColour) {
        this.cursorColour = cursorColour;
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
            .add("enableCursor", enableCursor)
            .add("cursorSize", cursorSize)
            .add("cursorColour", cursorColour)
            .add("borderSize", borderSize)
            .add("borderColour", borderColour)
            .toString();
    }
}
