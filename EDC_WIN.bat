@echo off
cd "%1"
echo Running Java Environmental Data Connector...
"java" -Djava.library.path=lib/wwj -Xmx512m -jar "EDC_Standalone.jar"
