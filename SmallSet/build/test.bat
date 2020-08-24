SET JAVA_HOME=C:\Program Files\Java\jdk-14-valhalla

REM Sadly, this doesn't work for some reason.

"%JAVA_HOME%\bin\java" -cp "../bin" -jar "..\lib\junit-platform-console-standalone-1.6.2.jar" -c ch.claude_martin.smallset.SmallSetTest

REM  "%JAVA_HOME%\bin\java" -cp "../bin;../lib/*" org.junit.platform.console.ConsoleLauncher -c ch.claude_martin.smallset.SmallSetTest
