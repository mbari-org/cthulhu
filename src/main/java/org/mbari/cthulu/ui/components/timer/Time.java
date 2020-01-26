/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Caprica Software Limited.
 */

package org.mbari.cthulu.ui.components.timer;

/**
 * Utility class to format milliseconds time values into hours, minutes, seconds.
 */
final class Time {

    /**
     * Format string for hours, minutes, and seconds.
     */
    private static final String HMS_FORMAT = "%d:%02d:%02d";

    /**
     * Format string for minutes and seconds.
     */
    private static final String MS_FORMAT = "%02d:%02d";

    /**
     * Format a time value (in milliseconds) as hours, minutes, and seconds.
     *
     * @param value millisecond time value
     * @return formatted time string
     */
    static String formatHoursMinutesSeconds(long value) {
        value /= 1000;
        int hours = (int) value / 3600;
        int remainder = (int) value - (hours * 3600);
        int minutes = remainder / 60;
        remainder -= minutes * 60;
        int seconds = remainder;
        return String.format(HMS_FORMAT, hours, minutes, seconds);
    }

    /**
     * Format a time value (in milliseconds) as minutes, and seconds.
     *
     * @param value millisecond time value
     * @return formatted time string
     */
    static String formatHoursMinutes(long value) {
        value /= 1000;
        int minutes = (int) value / 60;
        int seconds = (int) value - (minutes * 60);
        return String.format(MS_FORMAT, minutes, seconds);
    }

    private Time() {
    }
}
