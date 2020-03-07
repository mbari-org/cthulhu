package org.mbari.cthulhu.settings;

import org.mbari.cthulhu.ui.player.TimerMode;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Settings for the media player.
 */
final public class MediaPlayer {

    private TimerMode timeDisplay;

    private int normalSkip;

    private int longSkip;

    private int skipThrottle;

    private int scrubThrottle;

    /**
     * Create settings with default values.
     */
    public MediaPlayer() {
        this.timeDisplay = TimerMode.REMAINING;
        this.normalSkip = 1000;
        this.longSkip = 5000;
        this.skipThrottle = 100;
        this.scrubThrottle = 200;
    }

    /**
     * Copy settings.
     *
     * @param from settings to copy
     */
    public MediaPlayer(MediaPlayer from) {
        this.timeDisplay = from.timeDisplay;
        this.normalSkip = from.normalSkip;
        this.longSkip = from.longSkip;
        this.skipThrottle = from.skipThrottle;
        this.scrubThrottle = from.scrubThrottle;
    }

    public TimerMode timeDisplay() {
        return timeDisplay;
    }

    public void timeDisplay(TimerMode timeDisplay) {
        this.timeDisplay = timeDisplay;
    }

    public int normalSkip() {
        return normalSkip;
    }

    public void normalSkip(int normalSkip) {
        this.normalSkip = normalSkip;
    }

    public int longSkip() {
        return longSkip;
    }

    public void longSkip(int longSkip) {
        this.longSkip = longSkip;
    }

    public int skipThrottle() {
        return skipThrottle;
    }

    public void skipThrottle(int skipThrottle) {
        this.skipThrottle = skipThrottle;
    }

    public int scrubThrottle() {
        return scrubThrottle;
    }

    public void scrubThrottle(int scrubThrottle) {
        this.scrubThrottle = scrubThrottle;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("timeDisplay", timeDisplay)
            .add("normalSkip", normalSkip)
            .add("longSkip", longSkip)
            .add("skipThrottle", skipThrottle)
            .add("scrubThrottle", scrubThrottle)
            .toString();
    }
}
