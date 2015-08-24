@echo off
"%JAVA_HOME%\bin\java" -cp "standalone/*" com.helger.as2.app.MainOpenAS2Server src/main/resources/config/config.xml
