package org.mbari.cthulu.ui.components.settings.pages;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.mbari.cthulu.settings.Settings;
import org.mbari.cthulu.ui.components.settings.SettingsPage;
import org.mbari.cthulu.ui.components.settings.SettingsValidationException;
import org.mbari.cthulu.ui.components.settings.controls.HelpTextLabel;
import org.mbari.cthulu.ui.components.settings.controls.ItemLabel;
import org.mbari.cthulu.ui.components.settings.controls.SectionDivider;
import org.mbari.cthulu.ui.components.settings.controls.TimerModeChoiceBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

import static java.lang.Integer.parseInt;

/**
 * Settings page for configuring the media player.
 */
final public class MediaPlayerSettingsPane extends SettingsPage {

    private static final Logger log = LoggerFactory.getLogger(MediaPlayerSettingsPane.class);

    private static final String HEADING_TEXT = "Media Player";
    private static final String PROMPT_TEXT = "Configure the various settings for the media player.";

    private final TimerModeChoiceBox defaultTimeDisplayChoiceBox;

    private final TextField normalSkipTextField;

    private final TextField longSkipTextField;

    private final TextField skipThrottleTextField;

    private final TextField scrubThrottleTextField;

    public MediaPlayerSettingsPane() {
        super(HEADING_TEXT, PROMPT_TEXT);

        defaultTimeDisplayChoiceBox = new TimerModeChoiceBox();

        normalSkipTextField = new TextField();
        normalSkipTextField.setPrefColumnCount(5);

        longSkipTextField = new TextField();
        longSkipTextField.setPrefColumnCount(5);

        skipThrottleTextField = new TextField();
        skipThrottleTextField.setPrefColumnCount(5);

        scrubThrottleTextField = new TextField();
        scrubThrottleTextField.setPrefColumnCount(5);

        setContent(createContent());
    }

    private Pane createContent() {
        MigPane contentPane = new MigPane("ins 0, fill, wrap, gapy 12", "fill, grow");

        contentPane.add(new SectionDivider("Time Display"));

        MigPane timeDisplayPane = new MigPane("ins 0 12 0 0, wrap 2", "[][]", "");
        timeDisplayPane.add(new Label("Default:"), "width 100::");
        timeDisplayPane.add(defaultTimeDisplayChoiceBox);
        contentPane.add(timeDisplayPane);

        contentPane.add(new SectionDivider("Skip"));

        MigPane skipPane = new MigPane("ins 0 12 0 0, wrap 3, gapy 12", "[][]8[]", "");
        skipPane.add(new ItemLabel("Normal skip:"));
        skipPane.add(normalSkipTextField);
        skipPane.add(new HelpTextLabel("milliseconds"));
        skipPane.add(new ItemLabel("Long skip:"), "width 100::");
        skipPane.add(longSkipTextField);
        skipPane.add(new HelpTextLabel("milliseconds"));
        skipPane.add(new ItemLabel("Throttle:"), "width 100::");
        skipPane.add(skipThrottleTextField);
        skipPane.add(new HelpTextLabel("milliseconds"));
        skipPane.add(new HelpTextLabel("Skips are approximate any may not be frame-perfect"), "skip, span 2");
        contentPane.add(skipPane);

        contentPane.add(new SectionDivider("Scrub"));

        MigPane scrubPane = new MigPane("ins 0 12 0 0, wrap 3, gapy 12", "[][]8[]", "");
        scrubPane.add(new ItemLabel("Throttle:"), "width 100::");
        scrubPane.add(scrubThrottleTextField);
        scrubPane.add(new HelpTextLabel("milliseconds"), "grow");
        scrubPane.add(new HelpTextLabel("Too short a throttle can cause playback glitches"), "skip, span 2");

        contentPane.add(scrubPane);

        return contentPane;
    }

    @Override
    protected void fromSettings(Settings settings) {
        log.debug("fromSettings()");
        defaultTimeDisplayChoiceBox.setValue(settings.mediaPlayer().timeDisplay());
        normalSkipTextField.setText(Integer.toString(settings.mediaPlayer().normalSkip()));
        longSkipTextField.setText(Integer.toString(settings.mediaPlayer().longSkip()));
        skipThrottleTextField.setText(Integer.toString(settings.mediaPlayer().skipThrottle()));
        scrubThrottleTextField.setText(Integer.toString(settings.mediaPlayer().scrubThrottle()));
    }

    @Override
    protected void toSettings(Settings settings) {
        log.debug("toSettings()");
        settings.mediaPlayer().timeDisplay(defaultTimeDisplayChoiceBox.getValue());
        settings.mediaPlayer().normalSkip(parseInt(normalSkipTextField.getText()));
        settings.mediaPlayer().longSkip(parseInt(longSkipTextField.getText()));
        settings.mediaPlayer().skipThrottle(parseInt(skipThrottleTextField.getText()));
        settings.mediaPlayer().scrubThrottle(parseInt(scrubThrottleTextField.getText()));
    }

    @Override
    public void validateSettings() throws SettingsValidationException {
        // FIXME pending
    }
}
