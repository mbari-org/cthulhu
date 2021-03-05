module org.mbari.cthulhu {
    exports org.mbari.cthulhu to javafx.graphics;
   exports org.mbari.cthulhu.settings to com.google.gson;

    requires com.google.gson;
    requires com.google.common;
    requires io.reactivex.rxjava3;
    requires javafx.controls;
    requires miglayout.javafx;
    requires miglayout.core;
    requires org.slf4j;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.filefilters;
    requires vcr4j.sharktopoda.client;

    opens org.mbari.cthulhu.app.config;
    opens org.mbari.cthulhu.settings;
}