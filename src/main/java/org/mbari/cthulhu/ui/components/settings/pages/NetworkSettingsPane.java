package org.mbari.cthulhu.ui.components.settings.pages;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.mbari.cthulhu.settings.Settings;
import org.mbari.cthulhu.ui.components.settings.SettingsPage;
import org.mbari.cthulhu.ui.components.settings.SettingsValidationException;
import org.mbari.cthulhu.ui.components.settings.controls.HelpTextLabel;
import org.mbari.cthulhu.ui.components.settings.controls.SectionDivider;
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

    private final TextField incomingPortTextField;
    private final TextField incomingTopicTextField;
    private final TextField outgoingPortTextField;
    private final TextField outgoingTopicTextField;

    public NetworkSettingsPane() {
        super(HEADING_TEXT, PROMPT_TEXT);

        portNumberTextField = new TextField();
        portNumberTextField.setPrefColumnCount(5);

        incomingPortTextField = new TextField();
        incomingPortTextField.setPrefColumnCount(5);

        incomingTopicTextField = new TextField();
        incomingTopicTextField.setPrefColumnCount(15);

        outgoingPortTextField = new TextField();
        outgoingPortTextField.setPrefColumnCount(5);

        outgoingTopicTextField = new TextField();
        outgoingTopicTextField.setPrefColumnCount(15);

        setContent(createContent());
    }

    private Pane createContent() {
        MigPane contentPane = new MigPane("ins 0, fill, wrap, gapy 12", "fill, grow");

        contentPane.add(new SectionDivider("Remote Control"));

        MigPane controlPane = new MigPane("ins 0 12 0 0, wrap 2", "[][]", "");
        controlPane.add(new Label("Control Port:"), "width 100::");
        controlPane.add(portNumberTextField);
        controlPane.add(new HelpTextLabel("Changing the port requires an application restart"), "skip");
        contentPane.add(controlPane);

        contentPane.add(new SectionDivider("Localization"));

        MigPane localizationPane = new MigPane("ins 0 12 0 0, wrap 4", "[][]32[][]", "");
        localizationPane.add(new Label("Incoming port:"), "width 100::");
        localizationPane.add(incomingPortTextField);
        localizationPane.add(new Label("Topic:"), "width 40::");
        localizationPane.add(incomingTopicTextField);
        localizationPane.add(new Label("Outgoing port:"), "width 100::");
        localizationPane.add(outgoingPortTextField);
        localizationPane.add(new Label("Topic:"), "width 40::");
        localizationPane.add(outgoingTopicTextField);
        contentPane.add(localizationPane);

        return contentPane;
    }

    @Override
    protected void fromSettings(Settings settings) {
        log.debug("fromSettings()");
        portNumberTextField.setText(Integer.toString(settings.network().controlPort()));
        incomingPortTextField.setText(Integer.toString(settings.network().localization().incomingPort()));
        incomingTopicTextField.setText(settings.network().localization().incomingTopic());
        outgoingPortTextField.setText(Integer.toString(settings.network().localization().outgoingPort()));
        outgoingTopicTextField.setText(settings.network().localization().outgoingTopic());
    }

    @Override
    protected void toSettings(Settings settings) {
        log.debug("toSettings()");
        settings.network().controlPort(parseInt(portNumberTextField.getText().trim()));
        settings.network().localization().incomingPort(parseInt(incomingPortTextField.getText().trim()));
        settings.network().localization().incomingTopic(incomingTopicTextField.getText().trim());
        settings.network().localization().outgoingPort(parseInt(outgoingPortTextField.getText().trim()));
        settings.network().localization().outgoingTopic(outgoingTopicTextField.getText().trim());
    }

    @Override
    public void validateSettings() throws SettingsValidationException {
        validateRequired(portNumberTextField, "Port number is required.");
        validateInteger(portNumberTextField, "Invalid port number: %s.");

        validateRequired(incomingPortTextField, "Incoming localization port is required.");
        validateInteger(incomingPortTextField, "Invalid incoming localization port number: %s.");

        validateRequired(incomingTopicTextField, "Incoming localization topic is required.");

        validateRequired(outgoingPortTextField, "Outgoing localization port is required.");
        validateInteger(outgoingPortTextField, "Invalid outgoing localization port number: %s.");

        validateRequired(outgoingTopicTextField, "Outgoing localization topic is required.");
    }
}
