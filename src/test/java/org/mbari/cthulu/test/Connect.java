package org.mbari.cthulu.test;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "connect",
    aliases = "con",
    mixinStandardHelpOptions = true,
    description = "Starts the things",
    version = "1.0"
)
class Connect implements Callable<Void> {

    @Parameters(index = "0")
    private Integer port;

    @Parameters(index = "1")
    private Integer framecapturePort;

    public Void call() throws IOException {
        TestController.connect(port, framecapturePort);
        return null;
    }
}
