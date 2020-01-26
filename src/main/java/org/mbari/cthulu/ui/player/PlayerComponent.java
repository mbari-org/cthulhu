package org.mbari.cthulu.ui.player;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;
import org.mbari.cthulu.annotations.AnnotationsController;
import org.mbari.cthulu.ui.components.annotationview.AnnotationImageView;
import org.mbari.cthulu.ui.videosurface.ImageViewVideoSurfaceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.util.UUID;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * A player component.
 */
public final class PlayerComponent {

    private static final Logger log = LoggerFactory.getLogger(PlayerComponent.class);

    /**
     * Unique identifier of this player component.
     */
    private final UUID uuid;

    /**
     * Component that manages the one source of truth for current time and position.
     */
    private final MediaPlayerEventSource mediaPlayerEventSource = new MediaPlayerEventSource();

    private final ImageView videoImageView;

    private final EmbeddedMediaPlayer mediaPlayer;

    private final AnnotationImageView annotationImageView;

    private final AnnotationsController annotationsController;

    private final MediaPlayerTimer mediaPlayerTimer;

    private final MediaPlayerControls mediaPlayerControls;

    private final PlayerComponentStage stage;

    /**
     * Reported video frame rate.
     * <p>
     * This is set by a native media player event handler thread.
     */
    private volatile FrameRate frameRate;

    /**
     * Report media duration, in milliseconds.
     * <p>
     * This is set by a native media player event handler thread.
     */
    private volatile long length;

    public PlayerComponent(UUID uuid) {
        this.uuid = uuid;

        videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);

        mediaPlayer = application().mediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.videoSurface().set(ImageViewVideoSurfaceFactory.getVideoSurface(videoImageView));

        annotationImageView = new AnnotationImageView(this);

        annotationsController = new AnnotationsController(annotationImageView);

        mediaPlayerTimer = new MediaPlayerTimer(mediaPlayer, mediaPlayerEventSource::newTime);

        mediaPlayerControls = new MediaPlayerControls(this);

        stage = new PlayerComponentStage(this, annotationImageView, mediaPlayerControls);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        registerApplicationEventHandlers();
        registerAnnotationEventHandlers();
        registerMediaPlayerEventHandlers();
    }

    private void registerApplicationEventHandlers() {
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindow);
    }

    private void registerAnnotationEventHandlers() {
        annotationImageView.setOnNewAnnotation(annotationsController::annotationCreated);
    }

    private void registerMediaPlayerEventHandlers() {
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void opening(MediaPlayer mediaPlayer) {
                log.debug("opening()");
                mediaPlayerEventSource.newTime(0);
                mediaPlayerEventSource.newPosition(0);
            }

            @Override
            public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType type, int id) {
                log.debug("elementaryStreamAdded(type={}, id={})", type, id);
                if (type == TrackType.VIDEO && id != -1) {
                    Platform.runLater(() -> stage.showVideoView());
                }
            }

            @Override
            public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType type, int id) {
                log.debug("elementaryStreamSelected(type={}, id={})", type, id);
                if (type == TrackType.VIDEO && id != -1) {
                    // This is the earliest point in the media/media-player lifecycle that the track info is available
                    VideoTrackInfo videoTrackInfo = mediaPlayer.media().info().videoTracks().get(0);
                    log.debug("videoTrackInfo={}", videoTrackInfo);
                    setFrameRate(new FrameRate(videoTrackInfo.frameRate(), videoTrackInfo.frameRateBase()));
                }
            }

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                log.trace("positionChanged(newPosition={})", newPosition);
                mediaPlayerEventSource.newPosition(newPosition);
            }

            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
                log.debug("lengthChanged(newLength={})", newLength);
                setLength(length);
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                log.debug("stopped()");
                Platform.runLater(() -> stage.showDefaultView());
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                log.debug("finished()");
                // Can not seek when stopped, and in fact the media is "spent" so it makes sense to reset timers here
                mediaPlayerEventSource.newTime(0);
                mediaPlayerEventSource.newPosition(0);
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                log.error("error()");
                Platform.runLater(() -> stage.showDefaultView());
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
                log.trace("volumeChanged(volume={})", volume);
                // Native media player will report -1 if the media not playing, that value is not useful for us
                if (volume != -1f) {
                    mediaPlayerEventSource.newVolume(volume);
                }
            }
        });
    }

    private void setLength(long length) {
        log.debug("setLength(length={})", length);
        this.length = length;
    }

    private void setFrameRate(FrameRate frameRate) {
        log.debug("setFrameRate(frameRate={})", frameRate);
        this.frameRate = frameRate;
    }

    private void closeWindow(WindowEvent windowEvent) {
        log.debug("closeWindow(widowEvent={})", windowEvent);
        application().close(uuid);
    }

    /**
     * Get the unique identifier for this player component.
     *
     * @return component unique identifier
     */
    public UUID uuid() {
        return uuid;
    }

    /**
     * Get the associated native media player.
     *
     * @return media player
     */
    public EmbeddedMediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    /**
     * Get the image view used for the video surface.
     *
     * @return view
     */
    public ImageView videoImageView() {
        return videoImageView;
    }

    /**
     * Show this component (bring it to front).
     */
    public void show() {
        log.debug("show()");
        stage.toFront();
    }

    /**
     * Close this player component.
     */
    public void close() {
        log.debug("close()");

        application().settings().state().window(
            (int) Math.round(stage.getWidth()),
            (int) Math.round(stage.getHeight())
        );

        mediaPlayer.controls().stop();
        mediaPlayer.release();

        stage.close();
    }

    /**
     * Get the mediated event source for this component.
     *
     * @return event source
     */
    MediaPlayerEventSource eventSource() {
        return mediaPlayerEventSource;
    }

    /**
     * Get the current approximate frame time.
     *
     * @return frame time, to the nearest whole millisecond
     */
    long frameTime() {
        return frameRate != null ? frameRate.frameTime() : 0;
    }

    /**
     * Change the media player time.
     *
     * @param newTime new time, milliseconds from the start of the media
     */
    void setTime(long newTime) {
        mediaPlayer.controls().setTime(newTime);

        mediaPlayerEventSource.newTime(newTime);
        if (!mediaPlayer.status().isPlaying()) {
            mediaPlayerEventSource.newPosition(mediaPlayer.status().position());
        }
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("uuid", uuid)
            .toString();
    }
}
