# MBARI Cthulu

Cthulu is a video playback and annotation application developed by the
[Monteray Bay Aquarium Research Institute](https://mbari.org).


## Status

This is an in-development snapshot of an unfinished application. Significant functionality is present and working, but
some features are still in progress.


## Requirements

This application requires a [Java 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) run-time
environment. 


## Building Cthulu

Getting the source:
```
git clone git@github.com:caprica/cthulu.git
```

You should _not_ rely on the stability of this repository and you are on your own if you decide to fork it at the
present time - force pushes may be used during this phase of development until the first formal delivery of code is
made.

Use Maven to build the application:
```
$mvn clean install
```


## Running Cthulu

Using Maven:
```
$mvn javafx:run
```

### Considerations if running on Linux

An application that embeds LibVLC and uses a JavaFX/Swing file-open dialog can sometimes cause hard crashes, this can be
mitigated by specifying the following on the command-line when starting the application:
```
-DVLCJ_INITX=no
```

## Using Cthulu

Some minimal instructions for using Cthulu...

The application can function as a regular media player - you can open local files, paste clipboard contents to open 
files (either a local filename or a URL), or drag a local file over the application to play it.

The default network port for remote control is 5005 - commands sent to this port are _mostly_ implemented. 

Jogging/skipping is implemented using global key-bindings, these are configurable in a configuration file at build
time, but the defaults are:

| Function             | Key Binding |
| ---------------------| ------------|
| Long skip back       | d           |
| Normal skip back     | f           |
| Short skip back      | g           |
| Play/pause           | space or h  |
| Short skip ahead     | j           |
| Normal skip ahead    | k           |
| Long skip ahead      | l           |
| Native frame advance | n           |

Native frame advance is smooth, there is no native frame skip back.

Some other basic media player functionality is provided by application menus.

While the video is playing or paused, annotations can be created directly on top of the video by using the mouse to
drag rectangles around areas of interest. 

Annotation positions and sizes are always recorded in the coordinate system of the video being played - they scale
properly and maintain proper position as the window is resized.

Currently video annotations are implemented only so far as creating new annotations - time-based display and decay is
not yet implemented, nor is any selection or adjustment of annotations.

Editable user preferences are available for annotation display, skip durations and so on.

## Remote Control Testing

The network interface is mostly working.

A simple command-line driver test application is provided (see the project "test" sources) that can be used to test the
interface.


## Technical Limitations

Skips are not perfectly accurate - frame-specific accuracy is simply not possible with LibVLC and VLC's video decoders.

Smooth reverse playback is not supported by LibVLC/VLC and must be emulated by a sequence of short backward skips.

Video playback can sometimes glitch if there are many drastic back and foward scrub actions performed - these actions
are throttled to reduce the risk of this happening, but it still can happen. A frame skip or a pause/play cycle clears
the glitch.

Similarly doing multiple repeated skips back via the jog keys can sometimes glitch - these actions also are throttled to
reduce the risk of this happening.

### Linux

The JavaFX implementation on Linux is not as robust as that on macOS, visual artefacts are sometimes noticeable (badly
rendered JavaFX controls, buffer flickering and so on) - it is not common, but it has been observed.


### macOS

When playing some ".mov" files, it is possible to deadlock the native media player and the application when trying to
stop the media player during the application exit processing. Other file types appear to play, and stop, just fine.


## Copyright and Acknowledgements

Software development services provided by [capricasoftware.co.uk](http://capricasoftware.co.uk)

Icons from [MaterialUI](https://material.io/resources/icons) licensed under [Apache license version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

Copyright © MBARI 2020
