package org.mbari.cthulu.ui.components.settings.controls;

import javafx.scene.control.Label;

/**
 * A standard label for item-text within the settings dialog.
 */
final public class ItemLabel extends Label {

    private static final String STYLE_CLASS_NAME = "item-text";

    public ItemLabel(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS_NAME);
    }
}
