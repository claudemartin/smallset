REM I did this on Windows, so I use a simple batch file.
SET JAVA_HOME=C:\Program Files\Java\java-20-valhalla
SET ANT_HOME=C:\Program Files\Ant\apache-ant-1.10.14

"%ANT_HOME%\bin\ant" clean compile build  run

REM "%JAVA_HOME%\bin\java" -XX:+EnablePrimitiveClasses -cp smallset.jar ch.claude_martin.smallset.Main
