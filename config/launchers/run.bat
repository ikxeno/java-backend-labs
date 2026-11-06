@echo off
setlocal DisableDelayedExpansion
set "LAB_JAVA=java"
if defined JAVA_HOME set "LAB_JAVA=%JAVA_HOME%\bin\java.exe"
if exist "@cached.jdk@\bin\java.exe" set "LAB_JAVA=@cached.jdk@\bin\java.exe"

"%LAB_JAVA%" -jar "%~dp0@jar.name@" %*
exit /b %errorlevel%
