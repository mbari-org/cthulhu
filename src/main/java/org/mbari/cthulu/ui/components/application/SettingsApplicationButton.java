package org.mbari.cthulu.ui.components.application;

import javafx.stage.Stage;
import org.mbari.cthulu.settings.Settings;
import org.mbari.cthulu.ui.components.settings.SettingsDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mbari.cthulu.app.CthulhuApplication.application;

final public class SettingsApplicationButton extends ApplicationButton {

    private static final Logger log = LoggerFactory.getLogger(SettingsApplicationButton.class);

    private static final String IMAGE_RESOURCE = "/org/mbari/cthulu/icons/app/settings_48dp.png";

    private SettingsDialog settingsDialog;

    public SettingsApplicationButton(Stage stage) {
        super(stage, IMAGE_RESOURCE);
    }

    @Override
    protected void onAction() {
        log.debug("onAction()");

        if (settingsDialog == null) {
            settingsDialog = new SettingsDialog(stage);
        }

        Settings newSettings = new Settings(application().settings());
        settingsDialog.setSettings(newSettings);
        settingsDialog.showAndWait()
            .filter(response -> response)
            .ifPresent(response -> application().applySettings(newSettings));
    }
}
