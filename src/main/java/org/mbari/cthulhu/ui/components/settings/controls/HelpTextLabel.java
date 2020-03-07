package org.mbari.cthulhu.ui.components.settings.controls;

import javafx.scene.control.Label;

/**
 * A standard label for help-text within the settings dialog.
 */
final public class HelpTextLabel extends Label {

    private static final String STYLE_CLASS_NAME = "help-text";

    public HelpTextLabel(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS_NAME);
        setWrapText(true);
    }
}
