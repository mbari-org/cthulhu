#!/usr/bin/env bash

# build script used internally at MBARI

source "$HOME/workspace/M3/m3-deployspace/cthulhu/env-config.sh"
gradlew clean jpackage --info
