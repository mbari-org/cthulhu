# Cthulhu Requirements

Cthulhu will be a video playback application that communications with [MBARI's VARS appliation](https://github.com/mbari-media-management/vars-annotation). 

## Requirements

The high-level requirements are:


1. The video player must support playback of video encoded with [ProRes 422](https://support.apple.com/en-us/HT202410) amd [H.264](https://en.wikipedia.org/wiki/Advanced_Video_Coding). Additionally, support for [HEVC](https://en.wikipedia.org/wiki/High_Efficiency_Video_Coding), [VP9](https://en.wikipedia.org/wiki/VP9), and/or [AV1](https://en.wikipedia.org/wiki/AV1) are strongly desired.
2. Video resolutions up to 4K must be supported.
3. [Supports a pre-defined remote communications protocol (implemented via UDP)](UDP) that allows external applications to control and query Cthulhu.
4. [Displays bounding-boxes and labels of annotations as a video overlay](UI). 
5. Users will be able to create bounding box annotations directly on the video window.
6. The video player will work on [macOS](https://www.apple.com/macos/catalina/). However, cross-platform support (Linux, Windows) is a desired feature.


[JavaFX](https://openjfx.io/) is the preferred framework for development as the developers who will be maintaining this software are most familiar with it. JavaFX recently added support for sharing native memory buffers, allowsing for g C/C++based frameworks are also acceptable if Java does not meet the performance criteria.

Cthulhu will be made available via the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
