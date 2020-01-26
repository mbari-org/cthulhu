package org.mbari.cthulu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "play",
    mixinStandardHelpOptions = true,
    description = "Plays media",
    version = "1.0"
)
class Play implements Callable<Void> {

    public Void call() throws IOException {
        TestController.play();
        return null;
    }
}
