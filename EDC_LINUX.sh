#!/bin/sh
cd `dirname $0`
jre1.8.0_65/bin/java  -Xmx512m -jar EDC.jar "" $1
