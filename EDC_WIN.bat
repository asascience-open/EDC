@echo off
cd "%1"
echo Running Java Environmental Data Connector...
"jre8/bin/java.exe" -Dsun.java2d.noddraw=true -Xmx512m -jar "EDC.jar" "" "%2"
