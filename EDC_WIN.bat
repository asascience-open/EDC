@echo off
cd "%1"
echo Running Java Environmental Data Connector...
"jre7/bin/java.exe" -Djava.library.path=lib/wwj -Dsun.java2d.noddraw=true -Xmx512m -jar "EDC.jar"
