package org.mbari.cthulu.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.log.LogEventListener;
import uk.co.caprica.vlcj.log.LogLevel;

/**
 * Handler for log messages from the native media player library.
 */
final class NativeLogHandler implements LogEventListener {

    private static final Logger log = LoggerFactory.getLogger(NativeLogHandler.class);

    NativeLogHandler() {
    }

    @Override
    public void log(LogLevel level, String module, String file, Integer line, String name, String header, Integer id, String message) {
        // Do not format the log message unless the log level is appropriate
        switch (level) {
            case DEBUG:
                if (log.isDebugEnabled()) {
                    log.debug(formatLogMessage(module, file, line, name, message));
                }
                break;
            case NOTICE:
                if (log.isInfoEnabled()) {
                    log.info(formatLogMessage(module, file, line, name, message));
                }
                break;
            case WARNING:
                if (log.isWarnEnabled()) {
                    log.warn(formatLogMessage(module, file, line, name, message));
                }
                break;
            case ERROR:
                if (log.isErrorEnabled()) {
                    log.error(formatLogMessage(module, file, line, name, message));
                }
                break;
        }
    }

    /**
     * Format a log message.
     *
     * @param module native library module that emitted the message
     * @param file source file where the message was emitted
     * @param line line number where the message was emitted
     * @param name name of the logger
     * @param message message text
     * @return formatted log statement
     */
    private static String formatLogMessage(String module, String file, Integer line, String name, String message) {
        return String.format("[%s:%s] %s (%s:%d)", module, name, message, file, line);
    }
}
