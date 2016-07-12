#!/bin/sh
cd "${0%/*}/"
export DYLD_LIBRARY_PATH=""
jre1.8.0_40.jre/Contents/Home/bin/java  -Xmx512m -Xdock:name="EDC" -jar EDC.jar "" "$1"