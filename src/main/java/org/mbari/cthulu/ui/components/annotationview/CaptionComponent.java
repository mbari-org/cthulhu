package org.mbari.cthulu.ui.components.annotationview;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * A component used to render a caption.
 */
final class CaptionComponent extends StackPane {

    private static final String CONTAINER_STYLE_CLASS = "caption-component";

    private static final String TEXT_STYLE_CLASS = "text";

    private final Text text;

    /**
     * Create a caption component.
     */
    CaptionComponent() {
        getStyleClass().add(CONTAINER_STYLE_CLASS);

        text = new Text();
        text.getStyleClass().add(TEXT_STYLE_CLASS);

        applySettings();

        getChildren().add(text);
    }

    /**
     * Set the caption text.
     *
     * @param caption text
     */
    final void setCaption(String caption) {
        text.setText(caption);
    }

    /**
     * Get the caption text.
     *
     * @return text
     */
    final String getCaption() {
        return text.getText();
    }

    /**
     * Apply the current settings for this component.
     */
    public void applySettings() {
        setBackground(new Background(new BackgroundFill(
            Color.web(application().settings().annotations().captions().backgroundColour()),
            new CornerRadii(6),
            Insets.EMPTY
        )));
        text.setFont(new Font(application().settings().annotations().captions().fontSize()));
        text.setFill(Color.web(application().settings().annotations().captions().textColour()));
    }
}
