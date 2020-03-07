package org.mbari.cthulhu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "elapsed-time",
    aliases = "et",
    mixinStandardHelpOptions = true,
    description = "Requests the current playback elapsed time",
    version = "1.0"
)
class ElapsedTime implements Callable<Void> {

    public Void call() throws IOException {
        TestController.elapsedTime();
        return null;
    }
}
