package org.mbari.cthulhu.ui.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * A more stable source of time events for a media player.
 * <p>
 * The native media player sends somewhat irregular time changes which if strictly adhered to will provide a less than
 * optimal user experience, this component attempts to provide a more stable timer aiming to give smooth
 * updates for timer displays.
 * <p>
 * The accuracy and other characteristics of the time events from the native media player depend on the decoder that is
 * being used to play the media, therefore it is not trivial to come up with a perfect one-size-fits-all implementation
 * for this timer. At least the approximation implemented here as better than the default.
 */
final class MediaPlayerTimer {

    private static final Logger log = LoggerFactory.getLogger(MediaPlayerTimer.class);

    //  - While playing, this timer calls the given handler at every tick
    //  - The handler is called with the following time value depending on whether
    //    mediaPlayer.status().time() has reported a new time or not:
    //      - if the reported time is new, then that's the one used for the call
    //      - otherwise, the time is the previously reported time plus the difference in
    //        system time wrt when that last time was reported.
    //        This also takes into account the "direction of time" based on reported
    //        mediaPlayer.status().time() values, which should help better reflect the
    //        changes when the user drags the time control back and forth during playing.

    /**
     * A short period for this timer. At each tick, the given onTick consumer is called.
     */
    private static final long PERIOD = 10;

    /**
     * Timer implementation.
     */
    private Timer timer = new Timer(true);

    /**
     * Captures previously reported mediaPlayer.status().time()
     */
    private long previousMpMillis = -1;

    /**
     * Captures system time when previousMpMillis is updated.
     */
    private long previousSysMillisForMp = 0;

    /**
     * This is either +1 or -1 indicating time direction according to sign of
     * reported mediaPlayer time difference.
     */
    private long timeDirection;

    /**
     * Was media playing in previous call to the timer?
     */
    private boolean wasPlaying = false;

    // Note: underscore-prefixed variables below are for debugging purposes.
    // For performance reasons, keep associated sections commented out
    // (except of course locally as needed, and probably also in working
    // branches to facilitate dev/testing)

    private long _previousSysMillis = 0;
    private long _previousMpMillis = 0;

    /**
     * Create a timer.
     *
     * @param mediaPlayer media player that generates time-changed events
     * @param onTick function to execute on each tick of the timer
     */
    MediaPlayerTimer(MediaPlayer mediaPlayer, Consumer<Long> onTick) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final boolean isPlaying = mediaPlayer.status().isPlaying();
                final long newMpMillis = mediaPlayer.status().time();
                final long currSysMillis = System.currentTimeMillis();

                if (log.isTraceEnabled()) {
                  final long _sysDiffMillis = currSysMillis - _previousSysMillis;
                  final long _mpDiffMillis = newMpMillis - _previousMpMillis;
                  log.trace("{} _sysDiffMillis=({}) _mpDiffMillis=({}) newMpMillis={} ",
                    isPlaying ? "PLAYING" : "       ",
                    String.format("%3d", _sysDiffMillis),
                    String.format("%3d", _mpDiffMillis),
                    String.format("%6d", newMpMillis)
                  );
                  _previousSysMillis = currSysMillis;
                  _previousMpMillis = newMpMillis;
                }

                // The value for the eventual call to onTick.accept:
                long toCallTickMillis;
                if (newMpMillis > 0 && newMpMillis != previousMpMillis) {
                    // mediaPlayer has reported a new (non-zero) value.
                    // first, some updates for possible handling in subsequent tick:
                    timeDirection = newMpMillis > previousMpMillis ? +1 : -1;
                    previousSysMillisForMp = currSysMillis;

                    toCallTickMillis = previousMpMillis = newMpMillis;
                }
                else {
                    if (timeDirection < 0) {
                      // don't do the adjustment below in this case as it actually can result in a short
                      // but noticeable weird behavior: boxes moving back in time for a little bit while
                      // video resuming going forward. In any case, not doing anything here is ok given
                      // the subsequent update in next calls.
                      return;
                    }
                    // mediaPlayer has reported zero or the same value as before.
                    // Use last known time direction and system time difference:
                    toCallTickMillis = previousMpMillis +
                        timeDirection * (currSysMillis - previousSysMillisForMp);
                }

                if (isPlaying) {
                    onTick.accept(toCallTickMillis);
                }
                else {
                    previousMpMillis = -1;
                    if (wasPlaying) {
                        // Media just paused. Let's do one more call:
                        wasPlaying = false;
                        onTick.accept(toCallTickMillis);
                    }
                }
            }
        }, 0, PERIOD);
    }

    /**
     * Cancel the timer.
     */
    void cancel() {
        timer.cancel();
        timer = null;
    }
}
