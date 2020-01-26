package org.mbari.cthulu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "video-infos",
    aliases = "vis",
    mixinStandardHelpOptions = true,
    description = "Requests information for all videos",
    version = "1.0"
)
class VideoInfos implements Callable<Void> {

    public Void call() throws IOException {
        TestController.videoInfos();
        return null;
    }
}
