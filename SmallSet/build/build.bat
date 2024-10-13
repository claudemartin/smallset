REM I did this on Windows, so I use a simple batch file.
SET JAVA_HOME=C:\Program Files\Java\jdk-23-valhalla
SET ANT_HOME=C:\Program Files\Ant\apache-ant-1.10.14

"%ANT_HOME%\bin\ant" test demo build 
