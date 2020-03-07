package org.mbari.cthulhu.test;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "seek",
    mixinStandardHelpOptions = true,
    description = "Seeks the playback position to the specified time",
    version = "1.0"
)
class Seek implements Callable<Void> {

    @Parameters(index = "0")
    private Long time;

    public Void call() throws IOException {
        TestController.seek(time);
        return null;
    }
}
