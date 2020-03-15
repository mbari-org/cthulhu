[![MBARI logo](images/logo-mbari-3b.png)](https://www.mbari.org)

# Cthulhu

## Overview

A video playback application with remote UDP control and annotation overlay. 

This player will also allow localized annotation as well as rendering existing localized annotations directly on video. Integration with external applications will be supported via UDP.

A java implementation of a remote control is available in the [vcr4j-sharktopoda](https://github.com/mbari-media-management/vcr4j/tree/master/vcr4j-sharktopoda) module of [vcr4j](https://github.com/mbari-media-management/vcr4j)

## Current Implementation

This project is in the discovery and investigation phase. Recent updates to [JavaFX 13](https://bugs.openjdk.java.net/browse/JDK-8226947) and [vlcj](https://github.com/caprica/vlcj/issues/883) may allow very good video decoding and rendering via JavaFX. The authors of the M3 stack are familiar with JavaFX and would prefer that implementation language over C++ or Swift.

## Usage

### Prerequisites

- __Java 12+__:  Required for compile and build. 
- __[VLC](https://www.videolan.org/vlc/index.html)__: Cthulhu is run as it uses _libvlc_ for video decoding. 
- __[jpackage](https://jdk.java.net/jpackage/)__: Required to build the packaged application.

### Run

```bash
gradle run
```

### Package

```bash
export JPACKAGE_HOME=/Path/to/jdk14-with-jpackage
gradle jpackage
```
