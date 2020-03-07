package org.mbari.cthulhu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "close",
    mixinStandardHelpOptions = true,
    description = "Closes a player",
    version = "1.0"
)
class Close implements Callable<Void> {

    public Void call() throws IOException {
        TestController.close();
        return null;
    }
}
