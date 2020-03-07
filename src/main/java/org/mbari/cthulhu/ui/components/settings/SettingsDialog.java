package org.mbari.cthulhu.ui.components.settings;

import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.stage.Window;
import org.mbari.cthulhu.settings.Settings;
import org.mbari.cthulhu.ui.components.settings.pages.AnnotationSettingsPane;
import org.mbari.cthulhu.ui.components.settings.pages.InterfaceSettingsPane;
import org.mbari.cthulhu.ui.components.settings.pages.MediaPlayerSettingsPane;
import org.mbari.cthulhu.ui.components.settings.pages.NetworkSettingsPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mbari.cthulhu.app.CthulhuApplication.application;

/**
 * An application options/settings dialog.
 */
final public class SettingsDialog extends Dialog<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(SettingsDialog.class);

    private static final String STYLESHEET_RESOURCE_NAME = "/org/mbari/cthulhu/css/settings-dialog.css";

    private static final int DIALOG_HEIGHT = 550;

    /**
     * Component used to show the list of available settings pages, and to select which one to show.
     */
    private ListView<SettingsPage> settingsList;

    /**
     * Component used to manage the stack of individual settings pages.
     */
    private SettingsPages settingsPages;

    /**
     * Create a dialog.
     *
     * @param stage stage that is the parent for the dialog
     */
    public SettingsDialog(Window stage) {
        super();

        initOwner(stage);

        setTitle("Settings");

        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(STYLESHEET_RESOURCE_NAME).toExternalForm());
        dialogPane.getStyleClass().add("settings-dialog");

        settingsPages = new SettingsPages();
        settingsPages.getStyleClass().add("settings-content");

        settingsList = new ListView<>();
        settingsList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(SettingsPage page, boolean empty) {
                super.updateItem(page, empty);
                if (empty || page == null) {
                    setText(null);
                } else {
                    setText(page.name());
                }
            }
        });

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(settingsList, settingsPages);
        splitPane.setDividerPosition(0, 0);

        Label applicationNameLabel = new Label(application().applicationName());
        applicationNameLabel.getStyleClass().add("heading");

        getDialogPane().setContent(splitPane);

        ButtonType okButton = ButtonType.OK;
        ButtonType cancelButton = ButtonType.CANCEL;
        dialogPane.getButtonTypes().addAll(okButton, cancelButton);

        addPage(new AnnotationSettingsPane());
        addPage(new MediaPlayerSettingsPane());
        addPage(new InterfaceSettingsPane());
        addPage(new NetworkSettingsPane());

        settingsList.getSelectionModel().selectFirst();
        settingsPages.show(settingsList.getItems().get(0));

        setResultConverter(buttonType -> buttonType == ButtonType.OK);

        getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, event -> {
            try {
                settingsPages.validateSettings();
                settingsPages.toSettings();
            } catch (SettingsValidationException e) {
                log.debug("Invalid settings", e);
                event.consume();
            }
        });

        setHeight(DIALOG_HEIGHT);

        registerEventHandlers();
    }

    private void addPage(SettingsPage page) {
        settingsList.getItems().add(page);
        settingsPages.add(page);
    }

    private void registerEventHandlers() {
        settingsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            settingsPages.show(newValue);
        });
    }

    /**
     * Set the settings values for editing in each page.
     *
     * @param settings settings to edit
     */
    public void setSettings(Settings settings) {
        log.debug("setSettings(settings={})", settings);
        settingsPages.fromSettings(settings);
    }
}
