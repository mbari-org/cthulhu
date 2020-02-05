package org.mbari.cthulu.ui.components.application;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mbari.cthulu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.mbari.cthulu.app.CthulhuApplication.application;
import static uk.co.caprica.vlcj.filefilters.filetypes.VideoFileTypes.videoFileTypes;

final public class OpenLocalApplicationButton extends ApplicationButton {

    private static final Logger log = LoggerFactory.getLogger(OpenLocalApplicationButton.class);

    private static final String IMAGE_RESOURCE = "/org/mbari/cthulu/icons/app/open-local_48dp.png";

    private FileChooser fileChooser;

    public OpenLocalApplicationButton(Stage stage) {
        super(stage, IMAGE_RESOURCE);
    }

    @Override
    protected void onAction() {
        log.debug("onAction()");

        if (fileChooser == null) {
            fileChooser = new FileChooser();
            fileChooser.setTitle("Open Files");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", videoFileExtensions()));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        }

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            selectedFiles.forEach(OpenLocalApplicationButton::openFile);
            fileChooser.setInitialDirectory(selectedFiles.get(0).getParentFile());
        }
    }

    private static List<String> videoFileExtensions() {
        return Stream.of(videoFileTypes()).map(ext -> String.format("*.%s", ext)).collect(toList());
    }

    private static void openFile(File file) {
        log.debug("openFile(file={})", file);
        PlayerComponent playerComponent = application().open();
        playerComponent.mediaPlayer().media().play(file.getAbsolutePath());
    }
}
