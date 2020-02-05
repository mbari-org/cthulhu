package org.mbari.cthulu.ui.components.settings.pages;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import org.mbari.cthulu.settings.Settings;
import org.mbari.cthulu.ui.components.settings.SettingsPage;
import org.mbari.cthulu.ui.components.settings.SettingsValidationException;
import org.mbari.cthulu.ui.components.settings.controls.SectionDivider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Settings page for configuring user interface settings.
 */
final public class InterfaceSettingsPane extends SettingsPage {

    private static final Logger log = LoggerFactory.getLogger(InterfaceSettingsPane.class);

    private static final String HEADING_TEXT = "Interface";
    private static final String PROMPT_TEXT = "Configure various aspects of the application user interface.";

    private CheckBox rememberWindowPositionCheckBox;

    private CheckBox rememberWindowSizeCheckBox;

    public InterfaceSettingsPane() {
        super(HEADING_TEXT, PROMPT_TEXT);

        rememberWindowPositionCheckBox = new CheckBox("Remember window position");

        rememberWindowSizeCheckBox = new CheckBox("Remember window size");

        setContent(createContent());
    }

    private Pane createContent() {
        MigPane contentPane = new MigPane("ins 0, fill, wrap, gapy 12", "fill, grow");

        contentPane.add(new SectionDivider("Launcher"));

        MigPane launcherPane = new MigPane("ins 0 12 0 0, wrap 2", "[][]", "");
        launcherPane.add(rememberWindowPositionCheckBox);
        contentPane.add(launcherPane);

        contentPane.add(new SectionDivider("Media Player"));

        MigPane windowPane = new MigPane("ins 0 12 0 0, wrap 2", "[][]", "");
        windowPane.add(rememberWindowSizeCheckBox);
        contentPane.add(windowPane);

        return contentPane;
    }

    @Override
    protected void fromSettings(Settings settings) {
        log.debug("fromSettings()");
        rememberWindowPositionCheckBox.setSelected(settings.userInterface().rememberWindowPosition());
        rememberWindowSizeCheckBox.setSelected(settings.userInterface().rememberWindowSize());
    }

    @Override
    protected void toSettings(Settings settings) {
        log.debug("toSettings()");
        settings.userInterface().rememberWindowPosition(rememberWindowPositionCheckBox.isSelected());
        settings.userInterface().rememberWindowSize(rememberWindowSizeCheckBox.isSelected());
    }

    @Override
    public void validateSettings() throws SettingsValidationException {
        // FIXME pending
    }
}
