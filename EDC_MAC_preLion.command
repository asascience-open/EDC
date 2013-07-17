#!/bin/sh
cd "${0%/*}/"
export DYLD_LIBRARY_PATH=""
java  -Xmx512m -Xdock:name="EDC" -jar EDC.jar "" "$1"