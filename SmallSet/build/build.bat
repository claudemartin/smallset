REM I did this on my work computer (windows), so I use a simple batch file.
SET JAVA_HOME=C:\Program Files\Java\jdk-14-valhalla
SET ANT_HOME=C:\Program Files\Ant\apache-ant-1.9.15

"%ANT_HOME%\bin\ant" clean compile build run

REM "%JAVA_HOME%\bin\java" --enable-preview -cp smallset.jar ch.claude_martin.smallset.Main
