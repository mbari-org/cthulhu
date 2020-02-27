package org.mbari.cthulu.ui.player;

import com.google.common.base.Strings;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mbari.cthulu.settings.Settings;
import org.mbari.cthulu.ui.components.about.AboutDialog;
import org.mbari.cthulu.ui.components.settings.SettingsDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javafx.scene.input.KeyCombination.keyCombination;
import static org.mbari.cthulu.app.CthulhuApplication.application;
import static uk.co.caprica.vlcj.filefilters.filetypes.VideoFileTypes.videoFileTypes;

final class MainMenu extends MenuBar {

    private static final Logger log = LoggerFactory.getLogger(MainMenu.class);

    private final PlayerComponent playerComponent;

    private final Menu mediaMenu;
    private final MenuItem mediaOpenFileMenuItem;
    private final MenuItem mediaOpenNetworkStreamMenuItem;
    private final MenuItem mediaOpenLocationFromClipboard;
    private final MenuItem mediaOpenRecentMediaMenuItem;
    private final MenuItem mediaQuitMenuItem;

    private final Menu playbackMenu;

    private final MenuItem playbackJumpForwardMenuItem;
    private final MenuItem playbackJumpBackwardMenuItem;
    private final MenuItem playbackJumpToSpecificTimeMenuItem;
    private final MenuItem playbackPlayMenuItem;
    private final MenuItem playbackPauseMenuItem;
    private final MenuItem playbackStopMenuItem;

    private final Menu videoMenu;
    private final MenuItem videoTakeSnapshotMenuItem;

    private final Menu toolsMenu;
    private final MenuItem toolsPreferencesMenuItem;

    private final Menu helpMenu;
    private final MenuItem helpAboutMenuItem;

    private SettingsDialog settingsDialog;
    private AboutDialog aboutDialog;

    private FileChooser fileChooser;

    MainMenu(PlayerComponent playerComponent) {
        this.playerComponent = playerComponent;

        mediaMenu = new Menu("_Media");
        mediaMenu.setMnemonicParsing(true);
        mediaOpenFileMenuItem = new MenuItem("_Open File...");
        mediaOpenFileMenuItem.setMnemonicParsing(true);
        mediaOpenFileMenuItem.setAccelerator(keyCombination("ctrl+o"));
        mediaOpenNetworkStreamMenuItem = new MenuItem("Open _Network...");
        mediaOpenNetworkStreamMenuItem.setMnemonicParsing(true);
        mediaOpenLocationFromClipboard = new MenuItem("Open _Location from Clipboard");
        mediaOpenLocationFromClipboard.setMnemonicParsing(true);
        mediaOpenLocationFromClipboard.setAccelerator(keyCombination("ctrl+v"));
        mediaOpenRecentMediaMenuItem = new MenuItem("Open _Recent Media"); // FIXME submenu
        mediaOpenRecentMediaMenuItem.setMnemonicParsing(true);
        mediaQuitMenuItem = new MenuItem("_Quit");
        mediaQuitMenuItem.setMnemonicParsing(true);
        mediaQuitMenuItem.setAccelerator(keyCombination("ctrl+q"));

        playbackMenu = new Menu("P_layback");
        playbackMenu.setMnemonicParsing(true);
        playbackJumpForwardMenuItem = new MenuItem("_Jump Forward");
        playbackJumpForwardMenuItem.setMnemonicParsing(true);
        playbackJumpForwardMenuItem.setAccelerator(keyCombination("Ctrl+Right"));
        playbackJumpBackwardMenuItem = new MenuItem("Jump Bac_kward");
        playbackJumpBackwardMenuItem.setMnemonicParsing(true);
        playbackJumpBackwardMenuItem.setAccelerator(keyCombination("Ctrl+Left"));
        playbackJumpToSpecificTimeMenuItem = new MenuItem("Jump to Specific _Time...");
        playbackJumpToSpecificTimeMenuItem.setMnemonicParsing(true);
        playbackPlayMenuItem = new MenuItem("_Play");
        playbackPlayMenuItem.setMnemonicParsing(true);
        playbackPauseMenuItem = new MenuItem("Pa_use");
        playbackPauseMenuItem.setMnemonicParsing(true);
        playbackStopMenuItem = new MenuItem("_Stop");
        playbackStopMenuItem.setMnemonicParsing(true);

        videoMenu = new Menu("_Video");
        videoMenu.setMnemonicParsing(true);
        videoTakeSnapshotMenuItem = new MenuItem("Take _Snapshot");
        videoMenu.setMnemonicParsing(true);

        toolsMenu = new Menu("Tool_s");
        toolsMenu.setMnemonicParsing(true);
        toolsPreferencesMenuItem = new MenuItem("_Preferences...");
        toolsPreferencesMenuItem.setMnemonicParsing(true);
        toolsPreferencesMenuItem.setAccelerator(keyCombination("ctrl+p"));

        helpMenu = new Menu("_Help");
        helpMenu.setMnemonicParsing(true);
        helpAboutMenuItem = new MenuItem("_About...");
        helpAboutMenuItem.setMnemonicParsing(true);
        helpAboutMenuItem.setAccelerator(keyCombination("shift+f1"));

        addMenus();
        addHandlers();
    }

    private void addMenus() {
        mediaMenu.getItems().add(mediaOpenFileMenuItem);
        mediaMenu.getItems().add(mediaOpenNetworkStreamMenuItem);
        mediaMenu.getItems().add(mediaOpenLocationFromClipboard);
        mediaMenu.getItems().add(mediaOpenRecentMediaMenuItem);
        mediaMenu.getItems().add(new SeparatorMenuItem());
        mediaMenu.getItems().add(mediaQuitMenuItem);
        getMenus().add(mediaMenu);

        playbackMenu.getItems().add(playbackJumpForwardMenuItem);
        playbackMenu.getItems().add(playbackJumpBackwardMenuItem);
        playbackMenu.getItems().add(playbackJumpToSpecificTimeMenuItem);
        playbackMenu.getItems().add(new SeparatorMenuItem());
        playbackMenu.getItems().add(playbackPlayMenuItem);
        playbackMenu.getItems().add(playbackPauseMenuItem);
        playbackMenu.getItems().add(playbackStopMenuItem);
        getMenus().add(playbackMenu);

        videoMenu.getItems().add(videoTakeSnapshotMenuItem);
        getMenus().add(videoMenu);

        toolsMenu.getItems().add(toolsPreferencesMenuItem);
        getMenus().add(toolsMenu);

        helpMenu.getItems().add(helpAboutMenuItem);
        getMenus().add(helpMenu);
    }

    private void addHandlers() {
        mediaOpenFileMenuItem.setOnAction(this::openFile);
        mediaOpenLocationFromClipboard.setOnAction(this::openClipboard);
        mediaQuitMenuItem.setOnAction(this::quit);

        playbackJumpForwardMenuItem.setOnAction(this::jumpForward);
        playbackJumpBackwardMenuItem.setOnAction(this::jumpBackward);
        playbackJumpToSpecificTimeMenuItem.setOnAction(this::jumpTo);
        playbackPlayMenuItem.setOnAction(this::play);
        playbackPauseMenuItem.setOnAction(this::pause);
        playbackStopMenuItem.setOnAction(this::stop);

        videoTakeSnapshotMenuItem.setOnAction(this::takeSnapshot);

        toolsPreferencesMenuItem.setOnAction(this::showOptionsDialog);

        helpAboutMenuItem.setOnAction(this::showAboutDialog);
    }

    private void openFile(ActionEvent actionEvent) {
        log.debug("openFile()");
        if (fileChooser == null) {
            fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", videoFileExtensions()));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        }
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            fileChooser.setInitialDirectory(selectedFile.getParentFile());
            // FIXME this may need to go via an application() event in case we have some behaviours to init/reset when playing media
            playerComponent.mediaPlayer().media().play(selectedFile.getAbsolutePath());
        }
    }

    private List<String> videoFileExtensions() {
        return Stream.of(videoFileTypes()).map(ext -> String.format("*.%s", ext)).collect(toList());
    }

    private void openClipboard(ActionEvent actionEvent) {
        log.debug("openClipboard()");
        String clipboard = Clipboard.getSystemClipboard().getString();
        log.debug("clipboard={}", clipboard);
        if (!Strings.isNullOrEmpty(clipboard)) {
            playerComponent.mediaPlayer().media().play(clipboard);
        }
    }

    private void quit(ActionEvent actionEvent) {
        log.debug("quit()");
        application().close(playerComponent.uuid());
    }

    private void jumpForward(ActionEvent actionEvent) {
        log.debug("jumpForward()");
        setMediaPlayerTime(playerComponent.mediaPlayer().status().time() + 10000);
    }

    private void jumpBackward(ActionEvent actionEvent) {
        log.debug("jumpBackward()");
        setMediaPlayerTime(playerComponent.mediaPlayer().status().time() - 10000);
    }

    private void jumpTo(ActionEvent actionEvent) {
        log.debug("jumpTo()");
        // FIXME implement
    }

    private void play(ActionEvent actionEvent) {
        log.debug("play()");
        playerComponent.mediaPlayer().controls().play();
    }

    private void pause(ActionEvent actionEvent) {
        log.debug("pause()");
        playerComponent.mediaPlayer().controls().setPause(true);
    }

    private void stop(ActionEvent actionEvent) {
        log.debug("stop()");
        playerComponent.mediaPlayer().controls().stop();
    }

    private void takeSnapshot(ActionEvent actionEvent) {
        log.debug("takeSnapshot()");
        playerComponent.mediaPlayer().snapshots().save();
    }

    private void showOptionsDialog(ActionEvent actionEvent) {
        log.debug("showOptionsDialog()");
        if (settingsDialog == null) {
            settingsDialog = new SettingsDialog(getScene().getWindow());
        }
        Settings newSettings = new Settings(application().settings());
        settingsDialog.setSettings(newSettings);
        settingsDialog.showAndWait()
            .filter(response -> response == true)
            .ifPresent(response -> application().applySettings(newSettings));
    }

    private void showAboutDialog(ActionEvent actionEvent) {
        log.debug("showAboutDialog()");
        if (aboutDialog == null) {
            aboutDialog = new AboutDialog(getScene().getWindow());
        }
        aboutDialog.showAndWait();
    }

    private void setMediaPlayerTime(long time) {
        log.debug("setMediaPlayerTime(time={})", time);
        playerComponent.setTime(time);
    }
}
