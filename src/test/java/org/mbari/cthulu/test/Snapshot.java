package org.mbari.cthulu.test;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "snapshot",
    aliases = "ss",
    mixinStandardHelpOptions = true,
    description = "Requests a video frame snapshot",
    version = "1.0"
)
class Snapshot implements Callable<Void> {

    @Parameters(index = "0")
    private String path;

    public Void call() throws IOException {
        TestController.snapshot(path);
        return null;
    }
}
