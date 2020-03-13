module org.mbari.cthulhu {
    exports org.mbari.cthulhu to javafx.graphics;

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
}