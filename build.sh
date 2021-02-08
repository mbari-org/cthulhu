#!/usr/bin/env bash

# build script used internally at MBARI

export JPACKAGE_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-14.jdk/Contents/Home"
source "$HOME/workspace/M3/m3-deployspace/cthulhu/env-config.sh"
gradlew clean jpackage --info
