#!/bin/sh
cd `dirname $0`
java -Djava.library.path=lib/wwj -Xmx512m -Xdock:name="EDC" -jar EDC_Standalone.jar
