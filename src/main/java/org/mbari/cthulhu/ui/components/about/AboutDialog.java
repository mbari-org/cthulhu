package org.mbari.cthulhu.ui.components.about;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.tbee.javafx.scene.layout.MigPane;

import static org.mbari.cthulhu.app.CthulhuApplication.application;

/**
 * A basic "about" dialog showing application information.
 */
public class AboutDialog extends Dialog<Integer> {

    private static final String LOGO_RESOURCE_NAME = "/org/mbari/cthulhu/images/app/logo-mbari.png";

    private static final String STYLESHEET_RESOURCE_NAME = "/org/mbari/cthulhu/css/about-dialog.css";

    private static final String JAVAFX_VERSION_PROPERTY_NAME = "javafx.runtime.version";

    private static final String JVM_PROPERTY_NAME = "java.vm.name";

    /**
     * Create a dialog.
     *
     * @param stage stage that is the parent for the dialog
     */
    public AboutDialog(Window stage) {
        super();

        initOwner(stage);
        initStyle(StageStyle.UNDECORATED);

        setTitle(String.format("About %s", application().applicationName()));

        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource(STYLESHEET_RESOURCE_NAME).toExternalForm());
        dialogPane.getStyleClass().add("about-dialog");

        MigPane root = new MigPane("width 500::, fill, wrap, ins 4", "[fill]", "[][]");

        ImageView imageView = new ImageView(this.getClass().getResource(LOGO_RESOURCE_NAME).toExternalForm());
        root.add(imageView);

        Label applicationNameLabel = new Label(application().applicationName());
        applicationNameLabel.getStyleClass().add("heading");
        Label buildInfoLabel = new Label(formatBuildInfo());
        Label javaInfoLabel = new Label(String.format("Java version: %s", Runtime.version().toString()));
        Label javafxInfoLabel = new Label(String.format("JavaFX version: %s", System.getProperty(JAVAFX_VERSION_PROPERTY_NAME)));
        Label jvmInfoLabel = new Label(String.format("JVM: %s", System.getProperty(JVM_PROPERTY_NAME)));
        Label networkInfoLabel = new Label(String.format("Control port: %d", application().settings().network().controlPort()));
        Label copyrightLabel = new Label(String.format("Copyright © MBARI %s", application().buildInfo().year()));

        MigPane contentPane = new MigPane("fill, wrap, ins 4", "[fill]", "[]4[]16[]4[]4[]16[]16[]");
        contentPane.getStyleClass().add("content-pane");
        contentPane.add(applicationNameLabel);
        contentPane.add(buildInfoLabel);
        contentPane.add(javaInfoLabel);
        contentPane.add(jvmInfoLabel);
        contentPane.add(javafxInfoLabel);
        contentPane.add(networkInfoLabel);
        contentPane.add(copyrightLabel);
        root.add(contentPane);
        getDialogPane().setContent(root);

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButton);
    }

    private String formatBuildInfo() {
        return String.format(
            "Version %s, built %s",
            application().buildInfo().version(),
            application().buildInfo().buildDate()
        );
    }
}
