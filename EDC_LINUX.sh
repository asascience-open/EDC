#!/bin/sh
cd `dirname $0`
java -Djava.library.path=lib/wwj -Xmx512m -jar EDC_Standalone.jar