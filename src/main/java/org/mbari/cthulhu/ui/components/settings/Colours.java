package org.mbari.cthulhu.ui.components.settings;

import javafx.scene.paint.Color;

/**
 * Utility class dealing with colour value conversions.
 */
final public class Colours {

    /**
     * Convert a {@link Color} value to a CSS format.
     * <p>
     * This is used to serial configured colour options.
     *
     * @param color value to convert
     * @return equivlaent CSS value string
     */
    public static String colorToWebString(Color color) {
        return String.format("rgba(%d, %d, %d, %f)",
            (int) (255 * color.getRed()),
            (int) (255 * color.getGreen()),
            (int) (255 * color.getBlue()),
            color.getOpacity()
        );
    }

    private Colours() {
    }
}
