package org.mbari.cthulhu.app;

import javafx.application.Platform;
import org.mbari.cthulhu.ui.player.PlayerComponent;
import org.mbari.vcr4j.remote.control.commands.FrameCapture;
import org.mbari.vcr4j.remote.control.commands.VideoInfo;
import org.mbari.vcr4j.remote.control.commands.VideoInfoBean;
import org.mbari.vcr4j.remote.player.VideoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.media.InfoApi;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.waiter.mediaplayer.SnapshotTakenWaiter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import static org.mbari.cthulhu.app.CthulhuApplication.application;

final class CthulhuVideoController implements VideoController {

    private static final Logger log = LoggerFactory.getLogger(CthulhuClientController.class);

    private static final String FILE_PROTOCOL = "file";

    private static final String FILE_PREFIX = "file:";

    @Override
    public boolean open(UUID videoUuid, URL url) {
        log.debug("open(uuid={}, url={})", videoUuid, url);
        return platformExecute(() -> application().playerComponents().open(videoUuid).playNewMedia(convertMrl(url)));
    }

    @Override
    public boolean close(UUID videoUuid) {
        log.debug("close(videoUuid={})", videoUuid);
        return platformExecute(() -> application().playerComponents().close(videoUuid));
    }

    @Override
    public boolean show(UUID videoUuid) {
        log.debug("show(videoUuid={})", videoUuid);
        return platformExecute(() -> application().playerComponents().show(videoUuid));
    }

    @Override
    public Optional<VideoInfo> requestVideoInfo() {
        log.debug("requestVideoInfo()");
        return application().playerComponents().active().map(CthulhuVideoController::videoInfo);
    }

    @Override
    public List<VideoInfo> requestAllVideoInfos() {
        log.debug("requestAllVideoInfos()");
        return application().playerComponents().playerComponents().values().stream().map(CthulhuVideoController::videoInfo).toList();
    }

    @Override
    public boolean play(UUID videoUuid, double rate) {
        log.debug("play(videoUuid={}, rate)", rate);
        return application().playerComponents().get(videoUuid)
                .map(playerComponent -> {
                    playerComponent.mediaPlayer().controls().setRate((float) rate);
                    playerComponent.mediaPlayer().controls().play();
                    return true;
                }).orElse(false);
    }

    @Override
    public boolean pause(UUID videoUuid) {
        log.debug("pause(videoUuid={})", videoUuid);
        return application().playerComponents().get(videoUuid)
                .map(playerComponent -> {
                    if (playerComponent.playing()) {
                        playerComponent.mediaPlayer().controls().pause();
                    }
                    return true;
                }).orElse(false);
    }

    @Override
    public Optional<Double> requestRate(UUID videoUuid) {
        log.debug("requestRate(videoUuid={})", videoUuid);
        return application().playerComponents().get(videoUuid)
                .map(playerComponent -> playerComponent.playing() ? (double) playerComponent.mediaPlayer().status().rate() : 0d);
    }

    @Override
    public Optional<Duration> requestElapsedTime(UUID videoUuid) {
        log.debug("requestElapsedTime(videoUuid={})", videoUuid);
        return application().playerComponents().get(videoUuid)
                .map(playerComponent -> Duration.ofMillis(playerComponent.mediaPlayer().status().time()));
    }

    @Override
    public boolean seekElapsedTime(UUID videoUuid, Duration elapsedTime) {
        log.debug("seekElapsedTime(videoUuid={}, elapsedTime={})", videoUuid, elapsedTime);
        return application().playerComponents().get(videoUuid)
                .map(playerComponent ->
                        platformExecute(() -> playerComponent.setTime(elapsedTime.toMillis()))
                ).orElse(false);
    }

    @Override
    public boolean frameAdvance(UUID videoUuid) {
        log.debug("frameAdvance(videoUuid={})", videoUuid);
        return application().playerComponents().get(videoUuid)
                .map(playerComponent -> {
                    playerComponent.mediaPlayer().controls().nextFrame();
                    return true;
                }).orElse(false);
    }

    @Override
    public CompletableFuture<FrameCapture> framecapture(UUID videoUuid, UUID imageReferenceUuid, Path saveLocation) {
        log.debug("framecapture(videoUuid={}, saveLocation={})", videoUuid, saveLocation);
        return null;  // TODO: Implement frame capture. Current takeSnapshot() returns a future vcr4j.sharktopoda.client.model.FrameCapture but we need a vcr4j.remote.control.commands.FrameCapture
    }

    /**
     * Execute a task on the JavaFX application thread, wait for it to complete and return the result.
     *
     * @param callable task to execute
     * @return return value from task, or false on error
     */
    private static Boolean platformExecute(Callable<Boolean> callable) {
        FutureTask<Boolean> task = new FutureTask<>(callable);
        Platform.runLater(task);
        try {
            log.debug("waiting for task result");
            return task.get();
        } catch (Exception e) {
            log.error("Failed to execute task", e);
            return false;
        }
    }

    /**
     * Get the video info for the current media (if there is one) for a media player component.
     *
     * @param playerComponent media player component
     * @return Video info, may be <code>null</code> if there is no current media
     */
    private static VideoInfo videoInfo(PlayerComponent playerComponent) {
        return new VideoInfoBean(
                playerComponent.uuid(),
                url(playerComponent),
                durationMillis(playerComponent),
                frameRate(playerComponent),
                isKey(playerComponent));
    }

    /**
     * Get a URL for the current media (if there is one) for a media player component.
     *
     * @param playerComponent media player component
     * @return URL, may be <code>null</code> if there is no current media
     */
    private static URL url(PlayerComponent playerComponent) {
        try {
            InfoApi info = playerComponent.mediaPlayer().media().info();
            return info != null ? new URL(info.mrl()) : null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a duration for the current media (if there is one) for a media player component.
     *
     * @param playerComponent media player component
     * @return Duration in milliseconds, may be <code>null</code> if there is no current media
     */
    private static Long durationMillis(PlayerComponent playerComponent) {
        InfoApi info = playerComponent.mediaPlayer().media().info();
        return info != null ? info.duration() : null;
    }

    /**
     * Get a frame rate for the first video track of the current media (if there is one) for a media player component.
     *
     * @param playerComponent media player component
     * @return Frame rate in frames per second, may be <code>null</code> if there is no current media or the media has no video tracks
     */
    private static Double frameRate(PlayerComponent playerComponent) {
        InfoApi info = playerComponent.mediaPlayer().media().info();
        if (info == null) return null;

        Optional<VideoTrackInfo> firstTrackInfo = info.videoTracks().stream().findFirst();
        if (firstTrackInfo.isEmpty()) return null;

        return (double) firstTrackInfo.get().frameRateBase();
    }

    /**
     * Check if the current media is key (if there is one) for a media player component.
     *
     * @param playerComponent media player component
     * @return isKey, may be <code>null</code> if there is no current media
     */
    private static Boolean isKey(PlayerComponent playerComponent) {
        return false; // TODO: What to put here?
    }

    /**
     * Create a future for taking a media player snapshot.
     *
     * @param playerComponent media player component
     * @param path file path for the saved snapshot
     * @return future
     */
    private static CompletableFuture<org.mbari.vcr4j.sharktopoda.client.model.FrameCapture> takeSnapshot(PlayerComponent playerComponent, Path path) {
        log.debug("takeSnapshot(playerComponent={}, path={})", playerComponent, path);
        File snapshotFile = convertSnapshotPath(path);
        return CompletableFuture.supplyAsync(() -> {
            MediaPlayer mediaPlayer = playerComponent.mediaPlayer();
            long snapshotTime = mediaPlayer.status().time();
            log.debug("snapshotTime={}", snapshotTime);
            SnapshotTakenWaiter snapshotTakenWaiter = new SnapshotTakenWaiter(mediaPlayer) {
                @Override
                protected boolean onBefore(MediaPlayer mediaPlayer) {
                    log.debug("onBefore()");
                    return mediaPlayer.snapshots().save(snapshotFile);
                }
            };
            try {
                log.debug("awaiting snapshot taken event...");
                snapshotTakenWaiter.await();
                log.debug("got snapshot taken event");
                return new org.mbari.vcr4j.sharktopoda.client.model.FrameCapture(snapshotFile.toPath(), Duration.ofMillis(snapshotTime));
            } catch (InterruptedException e) {
                log.error("Interrupted waiting for snapshot taken event", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Heuristic to deal with local file URLs.
     * <p>
     * Converting a local file URL like "file:///home/video/test.mp4" to a {@link URL} instance will lose the proper
     * "file:///" prefix when the URL value is later retrieved - it would return e.g. "file:/home/video/test.mp4"
     * instead.
     * <p>
     * If a local file URL is passed, this method will restore the proper prefix.
     *
     * @param url URL
     * @return mrl
     */
    private static String convertMrl(URL url) {
        log.debug("convertMrl(url={})", url);
        String mrl = url.toExternalForm();
        if (FILE_PROTOCOL.equals(url.getProtocol())) {
            if (!mrl.startsWith("file:///")) {
                mrl = mrl.replace("file:/", "file:///");
            }
        } else {
            return mrl;
        }
        log.debug("mrl={}", mrl);
        return mrl;
    }

    /**
     * Heuristic to deal with local files mangled by a URL transformation.
     * <p>
     * The remote control API converts the simple path to a URL and then serialises the external form when it delivers
     * this message. This casuses the path to be deserialised to a path with a literal "file:" prefix - this prefix
     * would actually become part of the file name, it is not a URL protocol identifier.
     * <p>
     * This method therefore strips any "file:" prefix from the path.
     *
     * @param path file path as a mangled URL
     * @return cleaned local file path
     */
    private static File convertSnapshotPath(Path path) {
        log.debug("convertSnapshotPath(path={})",path);
        String snapshotPath = path.toString();
//        if (snapshotPath.startsWith(FILE_PREFIX)) {
//            snapshotPath = snapshotPath.substring(FILE_PREFIX.length());
//        }
//        log.debug("snapshotPath={}", snapshotPath);
//        return new File(snapshotPath);
        try {
            URL url = new URL(snapshotPath);
            return new File(url.toURI());
        }
        catch (MalformedURLException | URISyntaxException e){
            log.error("Failed to resolve  = {}", path);
            if (snapshotPath.startsWith(FILE_PREFIX)) {
                snapshotPath = snapshotPath.substring(FILE_PREFIX.length());
            }
            log.debug("snapshotPath={}", snapshotPath);
            return new File(snapshotPath);
        }
    }
}
