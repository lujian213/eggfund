#!/bin/bash
cd "$(dirname "$0")"/..||exit 1
export LIB_DIR=./lib
export CONF_DIR=./conf
export EXPORT_DIR=./export
"$JAVA_HOME/bin/java" -Xmx1024m -classpath $LIB_DIR/"*":"$CONF_DIR":$EXPORT_DIR -Dtitle=EggFund org.springframework.boot.loader.launch.JarLauncher &