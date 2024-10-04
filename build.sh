#!/bin/sh
javac ApiCentrumEngine.java
lukman@DESKTOP-4UFUH8D:/mnt/d/Swamedia/APIM/hello-word$ jar cfm ApiCentrumEngine.jar apicentrum-am-4.3.0/ MANIFEST.MF *.class
native-image -jar ApiCentrumEngine.jar