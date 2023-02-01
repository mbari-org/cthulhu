module org.mbari.cthulhu {
    exports org.mbari.cthulhu to javafx.graphics;
    exports org.mbari.cthulhu.settings to com.google.gson;
    exports org.mbari.cthulhu.ui.player to com.google.gson;

    requires com.google.gson;
    requires com.google.common;
    requires io.reactivex.rxjava3;
    requires javafx.controls;
    requires miglayout.javafx;
    requires miglayout.core;
    requires org.slf4j;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.filefilters;
    requires vcr4j.core;
    requires vcr4j.sharktopoda;
    requires vcr4j.sharktopoda.client;
    requires vcr4j.remote;
    requires java.xml;
    requires info.picocli;

    opens org.mbari.cthulhu.app.config to com.google.gson;
    opens org.mbari.cthulhu.settings to com.google.gson;
}