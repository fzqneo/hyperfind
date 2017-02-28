#!/usr/bin/env bash


wget -nv http://repository.mygrid.org.uk/artifactory/mygrid-all/net/java/dev/jai-imageio/jai-imageio-core-standalone/1.2-pre-dr-b04-2014-09-13/jai-imageio-core-standalone-1.2-pre-dr-b04-2014-09-13.jar -O jai_imageio.jar
wget -nv https://java.net/projects/swingx/downloads/download/releases/swingx-all-1.6.4.jar -O swingx.jar

shasum -c libs-SHA1