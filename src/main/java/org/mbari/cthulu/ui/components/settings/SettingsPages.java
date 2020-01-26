package org.mbari.cthulu.ui.components.settings;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.mbari.cthulu.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper component for a stack of settings pages in the settings dialog.
 */
final class SettingsPages extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(SettingsPages.class);

    private final Map<String, SettingsPage> settingsPages = new HashMap<>();

    private Settings settings;

    SettingsPages() {
    }

    /**
     * Add a page.
     *
     * @param settingsPage page to add
     */
    void add(SettingsPage settingsPage) {
        settingsPages.put(settingsPage.name(), settingsPage);
        getChildren().add(settingsPage);
    }

    /**
     * Show a particular settings page.
     *
     * @param nodeToShow page to show
     */
    void show(Node nodeToShow) {
        getChildren().forEach(node -> node.setVisible(false));
        nodeToShow.setVisible(true);
    }

    /**
     * Populate the user interface controls in each of the settings pages with values from the application settings.
     *
     * @param settings source of settings values
     */
    void fromSettings(Settings settings) {
        log.debug("fromSettings()");
        this.settings = settings;
        settingsPages.values().forEach(page -> page.fromSettings(settings));
    }

    /**
     * Apply new application settings from the controls in each of the settings page.
     *
     * @return new settings
     */
    void toSettings() {
        log.debug("toSettings()");
        settingsPages.values().forEach(page -> page.toSettings(settings));
    }

    /**
     * Validate the settings in each page.
     *
     * @throws SettingsValidationException if there is a validation error in any of the settings
     */
    void validateSettings() throws SettingsValidationException {
        log.debug("validateSettings()");
        for (SettingsPage settingsPage : settingsPages.values()) {
            settingsPage.validateSettings();
        }
    }
}
