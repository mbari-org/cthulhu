module org.mbari.jfx {
    exports org.mbari.cthulu to javafx.graphics;

    requires com.google.gson;
    requires com.google.common;
    requires io.reactivex.rxjava3;
    requires javafx.controls;
    requires miglayout.javafx;
    requires miglayout.core;
    requires slf4j.api;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.filefilters;
    requires vcr4j.sharktopoda.client;
}