package org.mbari.cthulu.ui.main;

import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.mbari.cthulu.ui.components.application.OpenLocalApplicationButton;
import org.mbari.cthulu.ui.components.application.OpenStreamApplicationButton;
import org.mbari.cthulu.ui.components.application.QuitApplicationButton;
import org.mbari.cthulu.ui.components.application.SettingsApplicationButton;

final class ApplicationButtons extends GridPane {

    private static final String STYLE_CLASS = "app-buttons";

    ApplicationButtons(Stage stage) {
        getStyleClass().add(STYLE_CLASS);

        int column = 0;

        add(new QuitApplicationButton(stage), column++, 0);
        add(new SettingsApplicationButton(stage), column++, 0);
        add(new OpenLocalApplicationButton(stage), column++, 0);
        add(new OpenStreamApplicationButton(stage), column++, 0);
    }
}
