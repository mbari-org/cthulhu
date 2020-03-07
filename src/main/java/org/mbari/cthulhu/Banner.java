package org.mbari.cthulhu;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;

/**
 * Application banner.
 */
final class Banner {

    /**
     * Name of the resource that contains the banner.
     */
    private static final String BANNER_RESOURCE_NAME = "/org/mbari/cthulhu/banner/banner.txt";

    /**
     * Get the banner text.
     *
     * @return banner
     */
    static String banner() {
        try {
            return Resources.toString(CthulhuLauncher.class.getResource(BANNER_RESOURCE_NAME), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application banner resource", e);
        }
    }

    private Banner() {
    }
}
