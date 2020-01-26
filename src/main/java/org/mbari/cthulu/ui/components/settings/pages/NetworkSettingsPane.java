package org.mbari.cthulu.ui.components.settings.pages;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.mbari.cthulu.settings.Settings;
import org.mbari.cthulu.ui.components.settings.SettingsPage;
import org.mbari.cthulu.ui.components.settings.SettingsValidationException;
import org.mbari.cthulu.ui.components.settings.controls.HelpTextLabel;
import org.mbari.cthulu.ui.components.settings.controls.SectionDivider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

import static java.lang.Integer.parseInt;

/**
 * Settings page for configuring the remote control network interface.
 */
final public class NetworkSettingsPane extends SettingsPage {

    private static final Logger log = LoggerFactory.getLogger(NetworkSettingsPane.class);

    private static final String HEADING_TEXT = "Network";
    private static final String PROMPT_TEXT = "Configure the network settings for the remote controller.";

    private final TextField portNumberTextField;

    public NetworkSettingsPane() {
        super(HEADING_TEXT, PROMPT_TEXT);

        portNumberTextField = new TextField();
        portNumberTextField.setPrefColumnCount(5);

        setContent(createContent());
    }

    private Pane createContent() {
        MigPane contentPane = new MigPane("ins 0, fill, wrap, gapy 12", "fill, grow");

        contentPane.add(new SectionDivider("Remote Control"));

        MigPane controlPane = new MigPane("ins 0 12 0 0, wrap 2", "[][]", "");
        controlPane.add(new Label("Port number:"), "width 100::");
        controlPane.add(portNumberTextField);
        controlPane.add(new HelpTextLabel("Changing the port requires an application restart"), "skip");
        contentPane.add(controlPane);

        return contentPane;
    }

    @Override
    protected void fromSettings(Settings settings) {
        log.debug("fromSettings()");
        portNumberTextField.setText(Integer.toString(settings.network().controlPort()));
    }

    @Override
    protected void toSettings(Settings settings) {
        log.debug("toSettings()");
        settings.network().controlPort(parseInt(portNumberTextField.getText().trim()));
    }

    @Override
    public void validateSettings() throws SettingsValidationException {
        // FIXME pending
    }
}
