package org.mbari.cthulhu.app.config;

import com.google.gson.GsonBuilder;

import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.text.DateFormat.LONG;
import static java.text.DateFormat.getDateInstance;

/**
 * Build information.
 * <p>
 * Information is read from a file on the application classpath that is prepared at build-time.
 */
final public class BuildInfo {

    /**
     * Name of the classpath resource containing the configuration.
     */
    private static final String CONFIG_RESOURCE = "/org/mbari/cthulhu/config/build-info.json";

    /**
     * Timestamp format pattern, matches the "maven.build.timestamp.format" property value in the project POM file.
     */
    private static final String TIMESTAMP_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * Date format pattern used for only the year.
     */
    private static final String YEAR_FORMAT_PATTERN = "yyyy";

    /**
     * Build artifact identifier.
     */
    private String artifactId;

    /**
     * Build version number.
     */
    private String version;

    /**
     * Build timestamp.
     */
    private Date timestamp;

    /**
     * Read media player configuration.
     *
     * @return media player configuration
     */
    public static BuildInfo readBuildInfo() {
        return new GsonBuilder()
            .setDateFormat(TIMESTAMP_FORMAT_PATTERN)
            .create()
            .fromJson(
                new InputStreamReader(BuildInfo.class.getResourceAsStream(CONFIG_RESOURCE)),
                BuildInfo.class
            );
    }

    /**
     * Get the build artifact identifier.
     *
     * @return artifact id
     */
    public String artifactId() {
        return artifactId;
    }

    /**
     * Get the build version.
     *
     * @return version
     */
    public String version() {
        return version;
    }

    /**
     * Get the build timestamp.
     *
     * @return timestamp
     */
    public Date timestamp() {
        return timestamp;
    }

    /**
     * Get the build date.
     *
     * @return date
     */
    public String buildDate() {
        return getDateInstance(LONG).format(timestamp);
    }

    /**
     * Get the year.
     *
     * @return year
     */
    public String year() {
        return new SimpleDateFormat(YEAR_FORMAT_PATTERN).format(timestamp);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("artifactId", artifactId)
            .add("version", version)
            .add("timestamp", timestamp)
            .add("buildDate", buildDate())
            .add("year", year())
            .toString();
    }

    private BuildInfo() {
    }
}
