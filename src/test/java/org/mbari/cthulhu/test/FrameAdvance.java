package org.mbari.cthulhu.test;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
    name = "frame-advance",
    aliases = "fa",
    mixinStandardHelpOptions = true,
    description = "Request a single frame advance",
    version = "1.0"
)
class FrameAdvance implements Callable<Void> {

    @Parameters(index = "0")
    private Integer frames;

    @Parameters(index = "1", defaultValue = "0")
    private Long delay;

    public Void call() throws IOException {
        TestController.frameAdvance(frames, delay);
        return null;
    }
}
