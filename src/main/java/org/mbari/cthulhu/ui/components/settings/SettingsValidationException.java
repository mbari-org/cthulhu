package org.mbari.cthulhu.ui.components.settings;

import javafx.scene.Node;

/**
 * Exception that may be thrown when validating settings.
 */
final public class SettingsValidationException extends Exception {

    /**
     * Create a validation exception.
     *
     * @param message error message describing the validation exception
     * @param node user interface component that contains the invalid value
     */
    public SettingsValidationException(String message, Node node) {
        super(message);
    }
}
