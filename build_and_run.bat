@echo off
setlocal enabledelayedexpansion

echo ATM System with Persistence - Build
echo.

REM Try to find Maven
set MAVEN_CMD=
for %%A in (mvn.cmd mvn.bat mvn) do (
    where %%A >nul 2>nul
    if not errorlevel 1 (
        set MAVEN_CMD=mvn
        goto :found_maven
    )
)

REM Check common Maven installation paths
if exist "C:\Program Files\Apache\maven\bin\mvn.cmd" set MAVEN_CMD=C:\Program Files\Apache\maven\bin\mvn
if exist "C:\apache-maven\bin\mvn.cmd" set MAVEN_CMD=C:\apache-maven\bin\mvn

:found_maven
if "%MAVEN_CMD%"=="" (
    echo ERROR: Maven not found. Please install Maven from https://maven.apache.org/download.cgi
    echo and add it to your PATH, or place it in C:\Program Files\Apache\maven
    exit /b 1
)

echo Using Maven: %MAVEN_CMD%
echo.
echo Compiling and running with Maven...
%MAVEN_CMD% clean compile exec:java -Dexec.mainClass="Main"

endlocal
