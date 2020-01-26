package org.mbari.cthulu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "pause",
    mixinStandardHelpOptions = true,
    description = "Toggles play/pause for media playback",
    version = "1.0"
)
class Pause implements Callable<Void> {

    public Void call() throws IOException {
        TestController.pause();
        return null;
    }
}
