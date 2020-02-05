package org.mbari.cthulu.ui.components.application;

import javafx.stage.Stage;
import org.mbari.cthulu.ui.components.stream.StreamDialog;
import org.mbari.cthulu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mbari.cthulu.app.CthulhuApplication.application;

final public class OpenStreamApplicationButton extends ApplicationButton {

    private static final Logger log = LoggerFactory.getLogger(OpenStreamApplicationButton.class);

    private static final String IMAGE_RESOURCE = "/org/mbari/cthulu/icons/app/open-stream_48dp.png";

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
                playerComponent.mediaPlayer().media().play(streamDialog.getStreamUrl());
            });
    }
}
