package org.mbari.cthulhu.ui.player;

import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.mbari.cthulhu.ui.components.annotationview.AnnotationImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.media.MetaData;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.io.File;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javafx.scene.input.KeyCombination.keyCombination;
import static org.mbari.cthulhu.app.CthulhuApplication.application;
import static org.mbari.cthulhu.ui.player.JogHandler.installJogHandler;

/**
 * The user interface for a player component.
 */
class PlayerComponentStage extends Stage {

    private static final Logger log = LoggerFactory.getLogger(PlayerComponentStage.class);

    private static final String APPLICATION_ICON_RESOURCE_NAME = "/org/mbari/cthulhu/icons/app/app-logo.png";

    private static final String APPLICATION_STYLESHEET_RESOURCE_NAME = "/org/mbari/cthulhu/css/cthulhu.css";

    private static final String TITLE_FORMAT = "%s - %s";

    private static final int DEFAULT_WIDTH = 1200;

    private static final int DEFAULT_HEIGHT = 850;

    private static final int MINIMUM_WIDTH = 400;

    private static final int MINIMUM_HEIGHT = 200;

    /**
     * Source of jog/skip events.
     * <p>
     * Skip actions do not directly change the media player playback position, they are routed through this object so
     * that the actions may be consolidated and throttled.
     */
    private final PublishSubject<Jog> jog = PublishSubject.create();

    private final PlayerComponent playerComponent;

    private final AnnotationImageView videoView;

    private final PlayerDefaultView defaultView;

    PlayerComponentStage(PlayerComponent playerComponent, AnnotationImageView annotationImageView, MediaPlayerControls mediaPlayerControls) {
        this.playerComponent = playerComponent;
        this.videoView = annotationImageView;

        setTitle(application().applicationName());
        getIcons().add(new Image(getClass().getResourceAsStream(APPLICATION_ICON_RESOURCE_NAME)));

        defaultView = new PlayerDefaultView();
        videoView.setVisible(false);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(defaultView, annotationImageView);

        BorderPane root = new BorderPane();
        root.setCenter(stackPane);
        root.setBottom(mediaPlayerControls);

        Scene scene = new Scene(root, Color.BLACK);
        scene.getStylesheets().add(getClass().getResource(APPLICATION_STYLESHEET_RESOURCE_NAME).toExternalForm());
        setScene(scene);

        log.debug("rememberWindowSize={}", application().settings().userInterface().rememberWindowSize());
        if (application().settings().userInterface().rememberWindowSize()) {
            setWidth(application().settings().state().windowWidth());
            setHeight(application().settings().state().windowHeight());
        } else {
            setWidth(DEFAULT_WIDTH);
            setHeight(DEFAULT_HEIGHT);
        }

        setMinWidth(MINIMUM_WIDTH);
        setMinHeight(MINIMUM_HEIGHT);

        // Global key-bindings
        bind(scene, application().keyMap().shortSkip(), () -> jog.onNext(Jog.SHORT_SKIP));
        bind(scene, application().keyMap().normalSkip(), () -> jog.onNext(Jog.SKIP));
        bind(scene, application().keyMap().longSkip(), () -> jog.onNext(Jog.LONG_SKIP));
        bind(scene, application().keyMap().shortBack(), () -> jog.onNext(Jog.SHORT_BACK));
        bind(scene, application().keyMap().normalBack(), () -> jog.onNext(Jog.BACK));
        bind(scene, application().keyMap().longBack(), () -> jog.onNext(Jog.LONG_BACK));
        bind(scene, application().keyMap().playPause(), this::playPause);
        bind(scene, application().keyMap().nextFrame(), this::nextFrame);
        installJogHandler(playerComponent, jog);

        // Drag/drop from external sources
        scene.setOnDragOver(this::handleDragOver);
        scene.setOnDragDropped(this::handleDropped);

        // Track the focused state of the stage to maintain the currently active component
        focusedProperty().addListener((observableValue, oldValue, newValue) -> focusChanged(newValue));

        // Listen for media changed events to update the scene window title
        playerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void mediaChanged(MediaPlayer mediaPlayer, MediaRef mediaRef) {
                log.debug("mediaChanged(media={})", mediaPlayer.media().meta().get(Meta.TITLE));
                if (mediaPlayer.media().isValid()) {
                    MetaData metaData = mediaPlayer.media().meta().asMetaData();
                    log.debug("metaData={}", metaData);
                    // Even if the media has no specific meta data, the title will be populated from the filename/MRL
                    String title = metaData.get(Meta.TITLE);
                    log.debug("title={}", title);
                    if (!isNullOrEmpty(title)) {
                        setTitle(String.format(TITLE_FORMAT, title, application().applicationName()));
                    }
                }
            }
        });

        show();
    }

    /**
     * Create a key-binding for an accelerator key.
     *
     * @param scene scene for the global key-binding
     * @param bindings zero or more key-bindings
     * @param runnable action to run when the bound key is pressed
     */
    private void bind(Scene scene, String[] bindings, Runnable runnable) {
        Stream.of(bindings).forEach(binding -> scene.getAccelerators().put(keyCombination(binding), runnable));
    }

    /**
     * Handler for a drag-over event.
     * <p>
     * This is invoked when an item is dragged over the scene.
     *
     * @param dragEvent event
     */
    private void handleDragOver(DragEvent dragEvent) {
        log.trace("handleDragOver()");
        // Only accept drags of single files
        Dragboard dragboard = dragEvent.getDragboard();
        if (dragboard.hasFiles() && dragboard.getFiles().size() == 1) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }
        dragEvent.consume();
    }

    /**
     * Handler for a dropped event.
     * <p>
     * This is invoked when an external item (e.g. a file) is dropped onto a scene.
     *
     * @param dragEvent event
     */
    private void handleDropped(DragEvent dragEvent) {
        log.trace("handleDropped()");
        if (dragEvent.getDragboard().hasFiles()) {
            File droppedFile = dragEvent.getDragboard().getFiles().get(0);
            log.debug("droppedFile={}", droppedFile);
            dragEvent.setDropCompleted(true);
            playerComponent.playNewMedia(droppedFile.getAbsolutePath());
        } else {
            dragEvent.setDropCompleted(false);
        }
        dragEvent.consume();
    }

    /**
     * Handler invoked when the stage gains/loses the input focus.
     *
     * @param focussed <code>true</code> if this stage has the focus; <code>false</code> if it does not
     */
    private void focusChanged(boolean focussed) {
        log.debug("focusOwnerChanged()");
        // Only interested in focus-gained events: if focus lost to another player component that will be handled by its
        // own focus event; if focus lost to some other application we still need to know which player component was the
        // last focus owner
        if (focussed) {
            application().activePlayerComponent(playerComponent);
        }
    }

    private void playPause() {
        playerComponent.mediaPlayer().controls().pause();
    }

    /**
     * Advance to the next frame.
     * <p>
     * This is a native frame advance, and is likely smoother and more accurate than advancing by milliseconds.
     */
    private void nextFrame() {
        playerComponent.mediaPlayer().controls().nextFrame();
        playerComponent.eventSource().newTime(playerComponent.mediaPlayer().status().time());
        playerComponent.eventSource().newPosition(playerComponent.mediaPlayer().status().position());
    }

    /**
     * Show the default view, used when no video is playing.
     */
    void showDefaultView() {
        videoView.setVisible(false);
        defaultView.setManaged(true);
        defaultView.setVisible(true);
    }

    /**
     * Show the video view.
     */
    void showVideoView() {
        videoView.setVisible(true);
        defaultView.setVisible(false);
        defaultView.setManaged(false);
    }
}
