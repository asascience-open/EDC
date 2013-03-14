#!/bin/sh
cd "${0%/*}/"
java  -Xmx512m -Xdock:name="EDC" -jar EDC.jar "" "$1"