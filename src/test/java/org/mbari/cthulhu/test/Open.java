package org.mbari.cthulhu.test;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "open",
    mixinStandardHelpOptions = true,
    description = "Opens a new player",
    version = "1.0"
)
class Open implements Callable<Void> {

    @Parameters(index = "0")
    private String url;

    public Void call() throws IOException {
        TestController.open(url);
        return null;
    }
}
