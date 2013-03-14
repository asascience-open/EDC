#!/bin/sh
cd "${0%/*}/"
jre1.7.0_17.jre/Contents/Home/bin/java  -Xmx512m -Xdock:name="EDC" -jar EDC.jar "" "$1"
