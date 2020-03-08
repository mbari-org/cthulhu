package org.mbari.cthulhu.ui.components.settings.pages;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.mbari.cthulhu.settings.Settings;
import org.mbari.cthulhu.ui.components.settings.SettingsPage;
import org.mbari.cthulhu.ui.components.settings.SettingsValidationException;
import org.mbari.cthulhu.ui.components.settings.controls.HelpTextLabel;
import org.mbari.cthulhu.ui.components.settings.controls.ItemLabel;
import org.mbari.cthulhu.ui.components.settings.controls.SectionDivider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.mbari.cthulhu.ui.components.settings.Colours.colorToWebString;

/**
 * Settings page for configuring video annotations.
 */
final public class AnnotationSettingsPane extends SettingsPage {

    private static final Logger log = LoggerFactory.getLogger(AnnotationSettingsPane.class);

    private static final String HEADING_TEXT = "Annotations";
    private static final String PROMPT_TEXT = "Configure various aspects of video annotations.";

    private final CheckBox enableCursorCheckbox;
    private final TextField cursorSizeTextField;
    private final ColorPicker cursorColourPicker;
    private final TextField dragBorderSize;
    private final ColorPicker dragBorderColourPicker;
    private final TextField displayBorderSize;
    private final ColorPicker displayBorderColourPicker;
    private final TextField timeWindowTextField;
    private final ColorPicker decayBorderColourPicker;
    private final TextField captionFontSizeTextField;
    private final ColorPicker captionTextColourPicker;
    private final ColorPicker captionBackgroundColourPicker;
    private final TextField defaultCaptionTextField;

    public AnnotationSettingsPane() {
        super(HEADING_TEXT, PROMPT_TEXT);

        enableCursorCheckbox = new CheckBox("Enable cursor");

        cursorSizeTextField = new TextField();
        cursorSizeTextField.setPrefColumnCount(3);
        cursorColourPicker = new ColorPicker();

        dragBorderSize = new TextField();
        dragBorderSize.setPrefColumnCount(3);
        dragBorderColourPicker = new ColorPicker();

        displayBorderSize = new TextField();
        displayBorderSize.setPrefColumnCount(3);
        displayBorderColourPicker = new ColorPicker();

        timeWindowTextField = new TextField();
        timeWindowTextField.setPrefColumnCount(3);

        decayBorderColourPicker = new ColorPicker();

        captionFontSizeTextField = new TextField();
        captionFontSizeTextField.setPrefColumnCount(3);
        captionTextColourPicker = new ColorPicker();
        captionBackgroundColourPicker = new ColorPicker();

        defaultCaptionTextField = new TextField();
        defaultCaptionTextField.setPrefColumnCount(15);

        setContent(createContent());
    }

    private Pane createContent() {
        MigPane contentPane = new MigPane("ins 0, fill, wrap, gapy 12", "fill, grow");

        contentPane.add(new SectionDivider("Annotation Creation"));

        MigPane creationPane = new MigPane("ins 0 12 0 0, wrap 4, gapy 12", "[][]32[][]", "");

        creationPane.add(enableCursorCheckbox, "wrap");

        creationPane.add(new ItemLabel("Cursor Size:"), "width 120::");
        creationPane.add(cursorSizeTextField);
        creationPane.add(new ItemLabel("Cursor Colour:"), "width 100::");
        creationPane.add(cursorColourPicker);
        creationPane.add(new ItemLabel("Border Size:"), "width 120::");
        creationPane.add(dragBorderSize);
        creationPane.add(new ItemLabel("Border Colour:"), "width 100::");
        creationPane.add(dragBorderColourPicker);

        contentPane.add(creationPane);

        contentPane.add(new SectionDivider("Annotation Display"));

        MigPane displayPane = new MigPane("ins 0 12 0 0, wrap 4, gapy 12", "[][]32[][]", "");
        displayPane.add(new ItemLabel("Border Size:"), "width 120::");
        displayPane.add(displayBorderSize);
        displayPane.add(new ItemLabel("Border Colour:"), "width 100::");
        displayPane.add(displayBorderColourPicker);
        displayPane.add(new ItemLabel("Time Window:"), "width 120::");
        displayPane.add(timeWindowTextField);
        displayPane.add(new ItemLabel("Decay Colour:"), "width 100::");
        displayPane.add(decayBorderColourPicker);
        displayPane.add(new HelpTextLabel("seconds"), "skip, span 3");

        contentPane.add(displayPane);

        contentPane.add(new SectionDivider("Captions"));

        MigPane captionsPane = new MigPane("ins 0 12 0 0, wrap 4, gapy 12", "[][]32[][]", "");
        captionsPane.add(new ItemLabel("Font Size:"), "width 120::");
        captionsPane.add(captionFontSizeTextField);
        captionsPane.add(new ItemLabel("Text Colour:"), "width 100::");
        captionsPane.add(captionTextColourPicker);
        captionsPane.add(new ItemLabel("Background:"), "width 100::, skip 2");
        captionsPane.add(captionBackgroundColourPicker);
        captionsPane.add(new ItemLabel("Default value:"), "width 120::");
        captionsPane.add(defaultCaptionTextField, "span 3, grow");
        contentPane.add(captionsPane);

        return contentPane;
    }

    @Override
    protected void fromSettings(Settings settings) {
        log.debug("fromSettings()");
        enableCursorCheckbox.setSelected(settings.annotations().creation().enableCursor());
        cursorSizeTextField.setText(Integer.toString(settings.annotations().creation().cursorSize()));
        cursorColourPicker.setValue(Color.web(settings.annotations().creation().cursorColour()));
        dragBorderSize.setText(Integer.toString(settings.annotations().creation().borderSize()));
        dragBorderColourPicker.setValue(Color.web(settings.annotations().creation().borderColour()));
        displayBorderSize.setText(Integer.toString(settings.annotations().display().borderSize()));
        displayBorderColourPicker.setValue(Color.web(settings.annotations().display().borderColour()));
        timeWindowTextField.setText(Integer.toString(settings.annotations().display().timeWindow()));
        decayBorderColourPicker.setValue(Color.web(settings.annotations().display().decayBorderColour()));
        captionFontSizeTextField.setText(Double.toString(settings.annotations().captions().fontSize()));
        captionTextColourPicker.setValue(Color.web(settings.annotations().captions().textColour()));
        captionBackgroundColourPicker.setValue(Color.web(settings.annotations().captions().backgroundColour()));
        defaultCaptionTextField.setText(settings.annotations().captions().defaultValue());
    }

    @Override
    protected void toSettings(Settings settings) {
        log.debug("toSettings()");
        settings.annotations().creation().enableCursor(enableCursorCheckbox.isSelected());
        settings.annotations().creation().cursorSize(parseInt(cursorSizeTextField.getText()));
        settings.annotations().creation().cursorColour(colorToWebString(cursorColourPicker.getValue()));
        settings.annotations().creation().borderSize(parseInt(dragBorderSize.getText()));
        settings.annotations().creation().borderColour(colorToWebString(dragBorderColourPicker.getValue()));
        settings.annotations().display().borderSize(parseInt(displayBorderSize.getText()));
        settings.annotations().display().borderColour(colorToWebString(displayBorderColourPicker.getValue()));
        settings.annotations().display().timeWindow(parseInt(timeWindowTextField.getText()));
        settings.annotations().display().decayBorderColour(colorToWebString(decayBorderColourPicker.getValue()));
        settings.annotations().captions().fontSize(parseDouble(captionFontSizeTextField.getText()));
        settings.annotations().captions().textColour(colorToWebString(captionTextColourPicker.getValue()));
        settings.annotations().captions().backgroundColour(colorToWebString(captionBackgroundColourPicker.getValue()));
        settings.annotations().captions().defaultValue(defaultCaptionTextField.getText());
    }

    @Override
    public void validateSettings() throws SettingsValidationException {
        validateRequired(cursorSizeTextField, "Cursor size is required.");
        validateInteger(cursorSizeTextField, "Invalid cursor size: %s");

        validateRequired(cursorColourPicker, "Cursor colour is required.");

        validateRequired(dragBorderSize, "Drag rectangle border size is required.");
        validateInteger(dragBorderSize, "Invalid drag rectangle border size: %s");

        validateRequired(dragBorderColourPicker, "Drag border colour is required.");

        validateRequired(displayBorderSize, "Display rectangle border size is required.");
        validateInteger(displayBorderSize, "Invalid display rectangle border size: %s");

        validateRequired(displayBorderColourPicker, "Display border colour is required.");

        validateRequired(timeWindowTextField, "Time window is required.");
        validateInteger(timeWindowTextField, "Invalid time window: %s");

        validateRequired(captionFontSizeTextField, "Caption font size is required.");
        validateDouble(captionFontSizeTextField, "Invalid caption font size: %s");

        validateRequired(captionTextColourPicker, "Caption text colour is required.");

        validateRequired(captionBackgroundColourPicker, "Caption background colour is required.");

        // Caption text can be optional
    }
}
