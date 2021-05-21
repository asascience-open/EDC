#!/bin/sh
cd "${0%/*}/"
export DYLD_LIBRARY_PATH=""
#JAVA_INST# -Djavax.xml.xpath.XPathFactory:http://java.sun.com/jaxp/xpath/dom=com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl  -Xmx512m -Xdock:name="EDC" -jar EDC.jar "" "$1"