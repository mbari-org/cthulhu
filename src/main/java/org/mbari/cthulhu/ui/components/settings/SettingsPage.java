package org.mbari.cthulhu.ui.components.settings;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.mbari.cthulhu.settings.Settings;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * A base component for a settings page.
 */
abstract public class SettingsPage extends MigPane {

    /**
     * Name of the settings page.
     */
    private final String name;

    private final Label headingLabel;
    private final Label descriptionLabel;

    /**
     * Create a settings page.
     *
     * @param heading heading text
     * @param description description text
     */
    protected SettingsPage(String heading, String description) {
        super("wrap, ins 6", "[fill, grow]", "[]12[]24[]");

        this.name = heading;

        headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("heading");
        add(headingLabel);

        descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("description");
        add(descriptionLabel);

        setVisible(false);
    }

    /**
     * Get the settings page name.
     *
     * @return name
     */
    final String name() {
        return name;
    }

    /**
     * Set the page-specific content for this settings page.
     * <p>
     * Used by sub-classes.
     *
     * @param content page content
     */
    protected final void setContent(Pane content) {
        add(content);
    }

    /**
     * Template method used by sub-classes to populate the settings page user interface controls from application
     * settings.
     *
     * @param settings source of settings values
     */
    protected abstract void fromSettings(Settings settings);

    /**
     * Template method used by sub-classes to apply application settings from the settings page user interface controls.
     *
     * @param settings settings to populate
     */
    protected abstract void toSettings(Settings settings);

    /**
     * Template method used by sub-classes to implement validation for the values in the settings page.
     *
     * @throws SettingsValidationException if invalid settings were detected
     */
    public abstract void validateSettings() throws SettingsValidationException;
}
