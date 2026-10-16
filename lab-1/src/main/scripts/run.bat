@echo off
setlocal DisableDelayedExpansion
set "LAB_JAVA=java"
if defined JAVA_HOME set "LAB_JAVA=%JAVA_HOME%\bin\java.exe"
if exist "@cached.jdk@\bin\java.exe" set "LAB_JAVA=@cached.jdk@\bin\java.exe"

if "%~1"=="" goto defaults
if "%~1"=="--crud-demo" if "%~2"=="" goto defaults

"%LAB_JAVA%" -jar "%~dp0@jar.name@" %*
exit /b %errorlevel%

:defaults
"%LAB_JAVA%" -jar "%~dp0@jar.name@" "%~dp0characters.csv" "%~dp0locations.txt" %*
exit /b %errorlevel%
