#!/bin/bash
pluginName=`echo "${PWD##*/}"`

mvn clean
mvn package

rm _server/server/plugins/${pluginName}-*.jar
cp target/${pluginName}-*.jar _server/server/plugins/
