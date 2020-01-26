package org.mbari.cthulu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "disconnect",
    aliases = "dis",
    mixinStandardHelpOptions = true,
    description = "Stops the things",
    version = "1.0"
)
class Disconnect implements Callable<Void> {

    public Void call() throws IOException {
        TestController.disconnect();
        return null;
    }
}
