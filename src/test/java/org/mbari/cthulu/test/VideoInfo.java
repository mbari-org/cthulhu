package org.mbari.cthulu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "video-info",
    aliases = "vi",
    mixinStandardHelpOptions = true,
    description = "Requests information for the current video",
    version = "1.0"
)
class VideoInfo implements Callable<Void> {

    public Void call() throws IOException {
        TestController.videoInfo();
        return null;
    }
}
