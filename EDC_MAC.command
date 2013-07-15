#!/bin/sh
cd "${0%/*}/"
export DYLD_LIBRARY_PATH=""
jre1.7.0_17.jre/Contents/Home/bin/java  -Xmx512m -Xdock:name="EDC" -jar EDC.jar "" "$1"
