package org.mbari.cthulu.ui.components.application;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mbari.cthulu.app.CthulhuApplication.application;

final public class QuitApplicationButton extends ApplicationButton {

    private static final Logger log = LoggerFactory.getLogger(QuitApplicationButton.class);

    private static final String IMAGE_RESOURCE = "/org/mbari/cthulu/icons/app/quit_48dp.png";

    public QuitApplicationButton(Stage stage) {
        super(stage, IMAGE_RESOURCE);
    }

    @Override
    protected void onAction() {
        log.debug("onAction()");
        application().quit();
    }
}
