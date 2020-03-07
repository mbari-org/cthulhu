package org.mbari.cthulhu.test;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "show",
    mixinStandardHelpOptions = true,
    description = "Shows the player",
    version = "1.0"
)
class Show implements Callable<Void> {

    public Void call() throws IOException {
        TestController.show();
        return null;
    }
}
