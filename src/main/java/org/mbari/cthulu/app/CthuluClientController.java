package org.mbari.cthulu.app;

import javafx.application.Platform;
import org.mbari.cthulu.ui.player.PlayerComponent;
import org.mbari.vcr4j.sharktopoda.client.ClientController;
import org.mbari.vcr4j.sharktopoda.client.model.FrameCapture;
import org.mbari.vcr4j.sharktopoda.client.model.Video;
import org.mbari.vcr4j.sharktopoda.client.udp.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.media.InfoApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.waiter.mediaplayer.SnapshotTakenWaiter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import static java.util.stream.Collectors.toList;
import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * Component that translates incoming remote controller requests to application actions.
 *
 * @see IO
 */
final class CthuluClientController implements ClientController {

    private static final Logger log = LoggerFactory.getLogger(CthuluClientController.class);

    private static final String FILE_PROTOCOL = "file";

    private static final String FILE_PREFIX = "file:";

    CthuluClientController() {
    }

    @Override
    public boolean open(UUID uuid, URL url) {
        log.debug("open(uuid={}, url={})", uuid, url);
        return platformExecute(() -> application().playerComponents().open(uuid).mediaPlayer().media().play(convertMrl(url)));
    }

    @Override
    public boolean close(UUID uuid) {
        log.debug("close(uuid={})", uuid);
        return platformExecute(() -> application().playerComponents().close(uuid));
    }

    @Override
    public boolean show(UUID uuid) {
        log.debug("show(uuid={})", uuid);
        return platformExecute(() -> application().playerComponents().show(uuid));
    }

    @Override
    public Optional<Video> requestVideoInfo() {
        log.debug("requestVideoInfo()");
        // FIXME need to keep track of what's "current" somehow, currently this returns junk
        Video result = null;
        try {
            result = new Video(UUID.randomUUID(), new URL("file:///dummy.mp4"));
            return Optional.of(result); // it seems to not like empty optionsl
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<Video> requestAllVideoInfos() {
        log.debug("requestAllVideoInfos()");
        return application().playerComponents().playerComponents().entrySet().stream()
            .map(entry -> new Video(entry.getKey(), url(entry.getValue())))
            .collect(toList());
    }

    @Override
    public boolean play(UUID uuid, double rate) {
        log.debug("play(uuid={}, rate)", rate);
        return application().playerComponents().get(uuid)
            .map(playerComponent -> {
                playerComponent.mediaPlayer().controls().setRate((float) rate);
                playerComponent.mediaPlayer().controls().play();
                return true;
            }).orElse(false);
    }

    @Override
    public boolean pause(UUID uuid) {
        log.debug("pause(uuid={})", uuid);
        return application().playerComponents().get(uuid)
            .map(playerComponent -> {
                playerComponent.mediaPlayer().controls().pause();
                return true;
            }).orElse(false);
    }

    @Override
    public Optional<Double> requestRate(UUID uuid) {
        log.debug("requestRate(uuid={})", uuid);
        return application().playerComponents().get(uuid)
            .map(playerComponent -> playerComponent.playing() ? (double) playerComponent.mediaPlayer().status().rate() : 0d);
    }

    @Override
    public Optional<Duration> requestElapsedTime(UUID uuid) {
        log.debug("requestElapsedTime(uuid={})", uuid);
        return application().playerComponents().get(uuid)
            .map(playerComponent -> Duration.ofMillis(playerComponent.mediaPlayer().status().time()));
    }

    @Override
    public boolean seekElapsedTime(UUID uuid, Duration duration) {
        log.debug("seekElapsedTime(uuid={}, duration={})", uuid, duration);
        return application().playerComponents().get(uuid)
            .map(playerComponent ->
                platformExecute(() -> playerComponent.setTime(duration.toMillis()))
            ).orElse(false);
    }

    @Override
    public boolean frameAdvance(UUID uuid) {
        log.debug("frameAdvance(uuid={})", uuid);
        return application().playerComponents().get(uuid)
            .map(playerComponent -> {
                playerComponent.mediaPlayer().controls().nextFrame();
                return true;
            }).orElse(false);
    }

    @Override
    public CompletableFuture<FrameCapture> framecapture(UUID uuid, Path path) {
        log.debug("framecapture(uuid={}, path={})", uuid, path);
        return application().playerComponents().get(uuid)
            .map(playerComponent -> takeSnapshot(playerComponent, path))
            .orElseThrow();
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
     * Create a future for taking a media player snapshot.
     *
     * @param playerComponent media player component
     * @param path file path for the saved snapshot
     * @return future
     */
    private static CompletableFuture<FrameCapture> takeSnapshot(PlayerComponent playerComponent, Path path) {
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
                return new FrameCapture(snapshotFile.toPath(), Duration.ofMillis(snapshotTime));
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
        if (snapshotPath.startsWith(FILE_PREFIX)) {
            snapshotPath = snapshotPath.substring(FILE_PREFIX.length());
        }
        log.debug("snapshotPath={}", snapshotPath);
        return new File(snapshotPath);
    }
}
