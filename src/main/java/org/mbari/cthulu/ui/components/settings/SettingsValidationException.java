package org.mbari.cthulu.ui.components.settings;

/**
 * Exception that may be thrown when validating settings.
 */
final public class SettingsValidationException extends Exception {

    /**
     * Create a validation exception.
     *
     * @param message error message describing the validation exception
     */
    public SettingsValidationException(String message) {
        super(message);
    }
}
