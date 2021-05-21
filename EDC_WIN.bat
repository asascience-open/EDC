@echo off
cd "%1"
echo Running Java Environmental Data Connector...
#JAVA_INST#  -Djavax.xml.xpath.XPathFactory:http://java.sun.com/jaxp/xpath/dom=com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl -Dsun.java2d.noddraw=true -Xmx512m -jar "EDC.jar" "" "%2"
