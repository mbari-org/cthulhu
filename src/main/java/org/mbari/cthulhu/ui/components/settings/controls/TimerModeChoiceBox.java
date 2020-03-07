package org.mbari.cthulhu.ui.components.settings.controls;

import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;
import org.mbari.cthulhu.ui.player.TimerMode;

/**
 * A custom choice-box for selecting a timer display mode (media duration or time remaining).
 */
final public class TimerModeChoiceBox extends ChoiceBox<TimerMode> {

    public TimerModeChoiceBox() {
        setConverter(new TimerModeStringConverter());

        getItems().add(TimerMode.DURATION);
        getItems().add(TimerMode.REMAINING);
    }

    private static final class TimerModeStringConverter extends StringConverter<TimerMode> {
        @Override
        public String toString(TimerMode timerMode) {
            return timerMode.name().toLowerCase();
        }

        @Override
        public TimerMode fromString(String s) {
            return TimerMode.valueOf(s.toUpperCase());
        }
    }
}
