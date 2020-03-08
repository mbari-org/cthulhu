package org.mbari.cthulhu.ui.components.application;

import javafx.stage.Stage;
import org.mbari.cthulhu.ui.components.stream.StreamDialog;
import org.mbari.cthulhu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mbari.cthulhu.app.CthulhuApplication.application;

final public class OpenStreamApplicationButton extends ApplicationButton {

    private static final Logger log = LoggerFactory.getLogger(OpenStreamApplicationButton.class);

    private static final String IMAGE_RESOURCE = "/org/mbari/cthulhu/icons/app/open-stream_48dp.png";

    private StreamDialog streamDialog;

    public OpenStreamApplicationButton(Stage stage) {
        super(stage, IMAGE_RESOURCE);
    }

    @Override
    protected void onAction() {
        log.debug("onAction()");

        if (streamDialog == null) {
            streamDialog = new StreamDialog(stage);
        }

        streamDialog.setStreamUrl("");
        streamDialog.showAndWait()
            .filter(response -> response)
            .ifPresent(response -> {
                PlayerComponent playerComponent = application().open();
                playerComponent.playNewMedia(streamDialog.getStreamUrl());
            });
    }
}
