@echo off
cd %~dp0 ..
set LIB_DIR=.\lib
set CONF_DIR=.\conf
set EXPORT_DIR=.\export
"%JAVA_HOME%\bin\java.exe" -Xmx1024m -classpath %LIB_DIR%\*;%CONF_DIR%;%EXPORT_DIR% -Dtitle=EggFund org.springframework.boot.loader.launch.JarLauncher