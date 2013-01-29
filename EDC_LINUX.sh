#!/bin/sh
cd `dirname $0`
jre7_linux/bin/java -Djava.library.path=lib/wwj -Xmx512m -jar EDC.jar