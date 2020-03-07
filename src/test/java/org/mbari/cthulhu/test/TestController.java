package org.mbari.cthulhu.test;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.mbari.vcr4j.commands.SeekElapsedTimeCmd;
import org.mbari.vcr4j.commands.VideoCommands;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.commands.FramecaptureCmd;
import org.mbari.vcr4j.sharktopoda.commands.OpenCmd;
import org.mbari.vcr4j.sharktopoda.commands.SharkCommands;
import org.mbari.vcr4j.sharktopoda.decorators.FramecaptureDecorator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.shell.jline3.PicocliJLineCompleter;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

/**
 * A basic interactive shell used to send remote commands to the Cthulu application via UDP.
 * <p>
 * Error handling is minimal, take care.
 */
public class TestController {

    @Command(
        name = "cthulhu",
        description = "Remote control test harness for Cthulu",
        footer = {"", "Press ctrl+d to exit."},
        subcommands = {
            Close.class,
            Connect.class,
            Disconnect.class,
            ElapsedTime.class,
            FrameAdvance.class,
            Open.class,
            Pause.class,
            Play.class,
            Seek.class,
            Show.class,
            Snapshot.class,
            VideoInfo.class,
            VideoInfos.class
        }
    )
    static class CthuluCommands implements Runnable {

        LineReaderImpl reader;
        PrintWriter out;

        public void setReader(LineReader reader){
            this.reader = (LineReaderImpl)reader;
            this.out = reader.getTerminal().writer();
        }

        public void run() {
            out.println(new CommandLine(this).getUsageMessage());
        }
    }

    private static SharktopodaVideoIO videoIO;

    private static FramecaptureDecorator framecaptureDecorator;

    static void connect(int port, int framecapturePort) {
        disconnect();
        try {
            videoIO = new SharktopodaVideoIO(UUID.randomUUID(), "localhost", port);
            framecaptureDecorator = new FramecaptureDecorator(videoIO, framecapturePort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void disconnect() {
        if (videoIO != null) {
            videoIO.close();
            videoIO = null;
        }
    }

    static void open(String url) {
        try {
            videoIO.send(new OpenCmd(new URL(url)));
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    static void show() {
        videoIO.send(SharkCommands.SHOW);
    }

    static void close() {
        videoIO.send(SharkCommands.CLOSE);
    }

    static void play() {
        videoIO.send(VideoCommands.PLAY);
    }

    static void pause() {
        videoIO.send(VideoCommands.PAUSE);
    }

    public static void frameAdvance(Integer frames, Long delay) {
        for (int i = 0; i < frames; i++) {
            videoIO.send(SharkCommands.FRAMEADVANCE);
            if (i + 1 < frames) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void snapshot(String path) {
        videoIO.send(new FramecaptureCmd(UUID.randomUUID(), new File(path)));
    }

    static void elapsedTime() {
        videoIO.send(VideoCommands.REQUEST_ELAPSED_TIME);
    }

    static void seek(Long time) {
        videoIO.send(new SeekElapsedTimeCmd(Duration.ofMillis(time)));
    }

    static void videoInfo() {
        videoIO.send(SharkCommands.REQUEST_VIDEO_INFO);
    }

    static void videoInfos() {
        videoIO.send(SharkCommands.REQUEST_ALL_VIDEO_INFOS);
    }

    public static void main(String[] args) throws Exception {
        try {
            CthuluCommands commands = new CthuluCommands();
            CommandLine cmd = new CommandLine(commands);
            Terminal terminal = TerminalBuilder.builder().build();
            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new PicocliJLineCompleter(cmd.getCommandSpec()))
                .parser(new DefaultParser())
                .build();
            commands.setReader(reader);
            String prompt = "cthulhu> ";
            String rightPrompt = null;
            while (true) {
                try {
                    String line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                    ParsedLine pl = reader.getParser().parse(line, 0);
                    String[] arguments = pl.words().toArray(new String[0]);
                    CommandLine.run(commands, arguments);
                } catch (UserInterruptException e) {
                } catch (EndOfFileException e) {
                    return;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
