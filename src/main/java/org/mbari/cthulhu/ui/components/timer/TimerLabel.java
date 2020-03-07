package org.mbari.cthulhu.ui.components.timer;

import com.google.common.base.Function;
import javafx.scene.control.Label;
import org.mbari.cthulhu.ui.player.TimerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * A label component used to display various types of timer.
 * <p>
 * The component accepts one or more {@link TimerMode} values to select the type of display. If more than one mode is
 * specified, clicking the label will cycle through each of those modes.
 */
public final class TimerLabel extends Label {

    private static final Logger log = LoggerFactory.getLogger(TimerLabel.class);

    private static final String STYLE_CLASS_NAME = "timer";

    /**
     * Default text to use for the time display when there is no current media.
     */
    private static final String DEFAULT_TIME_TEXT = "00:00";

    /**
     * Number of milliseconds in one hour.
     */
    private static final long ONE_HOUR = 1000 * 60 * 60;

    /**
     * Collection of configured timer display modes for this component.
     */
    private final List<TimerMode> timerModes;

    /**
     * Index of the current timer display mode.
     */
    private int currentMode = 0;

    /**
     * Previously applied time.
     */
    private long previousTime;

    /**
     * Previously applied duration.
     */
    private long previousLength;

    /**
     * Construct a timer display component.
     *
     * @param timerModes configured timer modes
     */
    public TimerLabel(TimerMode... timerModes) {
        this.timerModes = Arrays.asList(timerModes);
        getStyleClass().add(STYLE_CLASS_NAME);
        setText(DEFAULT_TIME_TEXT);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        if (timerModes.size() > 1) {
            setOnMouseClicked(mouseEvent -> cycleMode());
        }
    }

    /**
     * Set the timer display mode.
     *
     * @param timerMode new mode to set, it must be one of the modes set via the constructor
     */
    public void setMode(TimerMode timerMode) {
        log.debug("setMode(timerMode={})", timerMode);
        int requested = timerModes.indexOf(timerMode);
        if (requested != -1) {
            apply(requested);
        } else {
            throw new IllegalArgumentException(String.format("Requested mode %s not in allowed modes %s", timerMode, Arrays.toString(timerModes.toArray())));
        }
    }

    /**
     * Cycle through the allowed timer display modes.
     */
    public void cycleMode() {
        log.debug("cycleMode()");
        apply((currentMode + 1) % timerModes.size());
    }

    /**
     * Update with new time (and media length).
     *
     * @param time new time, in milliseconds
     * @param length length of the media, in milliseconds
     */
    public void tick(long time, long length) {
        log.trace("tick(time={}, length={})", time, length);

        // Remember the new values, they are used when changing timer mode so that the values can update immediately
        // rather than waiting for the next tick (which would perceptibly lag)
        this.previousTime = time;
        this.previousLength = length;

        // Determine which time format function to use based on the media length - if the length is over an hour then a
        // a format with hours, minutes and seconds is used, otherwise use a format with only minutes and seconds
        Function<Long, String> timeFormat = length > ONE_HOUR ?
            Time::formatHoursMinutesSeconds :
            Time::formatHoursMinutes;

        // Depending on the current timer mode, update the timer display
        switch (timerModes.get(currentMode)) {
            case ELAPSED:
                setText(timeFormat.apply(time));
                break;
            case DURATION:
                setText(timeFormat.apply(length));
                break;
            case REMAINING:
                setText(String.format("-%s", timeFormat.apply(length - time)));
                break;
            default:
                // This cannot happen
                throw new IllegalStateException("Unexpected timer mode");
        }
    }

    /**
     * Apply the new timer display mode.
     * <p>
     * This sets the mode and forces an immediate update.
     *
     * @param modeIndex index, counting from zero, of the timer display mode to set
     */
    private void apply(int modeIndex) {
        log.debug("apply(modeIndex={})", modeIndex);
        currentMode = modeIndex;
        tick(previousTime, previousLength);
    }
}
