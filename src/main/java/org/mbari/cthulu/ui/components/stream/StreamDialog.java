package org.mbari.cthulu.ui.components.stream;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mbari.cthulu.ui.components.settings.controls.ItemLabel;
import org.tbee.javafx.scene.layout.MigPane;

import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * A basic "about" dialog showing application information.
 */
public class StreamDialog extends Dialog<Boolean> {

    private static final String STYLESHEET_RESOURCE_NAME = "/org/mbari/cthulu/css/stream-dialog.css";

    private final TextField mrlTextField;

    /**
     * Create a dialog.
     *
     * @param stage stage that is the parent for the dialog
     */
    public StreamDialog(Stage stage) {
        super();

        initOwner(stage);

        setTitle(String.format("Open Stream", application().applicationName()));

        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(STYLESHEET_RESOURCE_NAME).toExternalForm());
        dialogPane.getStyleClass().add("stream-dialog");

        MigPane root = new MigPane("width 500::, fill, wrap, ins 4", "[][fill, grow]", "[][]");
        root.getStyleClass().add("content");

        mrlTextField = new TextField();
        mrlTextField.setPrefColumnCount(40);

        root.add(new ItemLabel("Stream URL:"), "grow");
        root.add(mrlTextField);

        getDialogPane().setContent(root);

        ButtonType okButton = ButtonType.OK;
        ButtonType cancelButton = ButtonType.CANCEL;
        dialogPane.getButtonTypes().addAll(okButton, cancelButton);

        setResultConverter(buttonType -> buttonType == ButtonType.OK);
    }

    public void setStreamUrl(String streamUrl) {
        mrlTextField.setText(streamUrl);
    }

    public String getStreamUrl() {
        return mrlTextField.getText().trim();
    }
}
