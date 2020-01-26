package org.mbari.cthulu.ui.player;

import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.util.concurrent.TimeUnit;

import static org.mbari.cthulu.app.CthulhuApplication.application;

/**
 * Handler for "jog" events.
 * <p>
 * There are various challenges to be addressed to implement an optimal jogging strategy.
 * <p>
 * First, for repeated jog inputs a sample strategy is used otherwise the native media player can get overwhelmed with
 * requests to adjust the time and cause a loss of synchronisation with the video decoding.
 * <p>
 * Even with this sampling it is unfortunately still possible, although much less likely, to lose that synchronisation.
 * <p>
 * If that happens, the media must be played to resolve it.
 * <p>
 * Second, frame-perfect addressing (for the short frame skips) simply is not possible so an approximation is required.
 * <p>
 * The goal is to minimise instances where skipping forward or back one frame changes the time but does not cause a new
 * frame to be decoded.
 * <p>
 * Given the current time, the approximate single frame time (from the frame rate) is then applied as a delta to the
 * current time to move approximately one frame. This can not be perfect because there can be a fractional number of
 * milliseconds per frame.
 * <p>
 * In practice, this seems to work out better than trying to implement direct frame addressing and "snapping" to the
 * nearest frame.
 */
final class JogHandler {

    private static final Logger log = LoggerFactory.getLogger(JogHandler.class);

    /**
     * Link a source of observable jog events to a player component.
     * <p>
     * Jog events will be throttled and translated to various playback time adjustments.
     *
     * @param playerComponent player component
     * @param jogObservable source of jog events
     */
    static void installJogHandler(PlayerComponent playerComponent, Observable<Jog> jogObservable) {
        jogObservable
            .sample(application().settings().mediaPlayer().skipThrottle(), TimeUnit.MILLISECONDS)
            .subscribe(jog -> {
                log.trace("jog={}", jog);
                MediaPlayer mediaPlayer = playerComponent.mediaPlayer();
                long time = mediaPlayer.status().time();
                switch (jog) {
                    case START:
                        time = 0;
                        break;
                    case LONG_BACK:
                        time = Math.max(0, time - application().settings().mediaPlayer().longSkip());
                        break;
                    case BACK:
                        time = Math.max(0, time - application().settings().mediaPlayer().normalSkip());
                        break;
                    case SHORT_BACK:
                        time = Math.max(0, time - playerComponent.frameTime());
                        break;
                    case SHORT_SKIP:
                        time = Math.min(mediaPlayer.status().length(), time + playerComponent.frameTime());
                        break;
                    case SKIP:
                        time = Math.min(mediaPlayer.status().length(), time + application().settings().mediaPlayer().normalSkip());
                        break;
                    case LONG_SKIP:
                        time = Math.min(mediaPlayer.status().length(), time + application().settings().mediaPlayer().longSkip());
                        break;
                }
                playerComponent.setTime(time);
            });
    }

    private JogHandler() {
    }
}
