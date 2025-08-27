@echo off
REM EyeAI Plugin Build Script
REM This script builds the EyeAI plugin JAR file

echo Building EyeAI plugin...
echo.

cd /d "%~dp0"

if exist gradlew.bat (
    echo Using Gradle wrapper...
    call gradlew.bat shadowJar
) else (
    echo Gradle wrapper not found. Trying system Gradle...
    gradle shadowJar
)

echo.
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo Plugin JAR files are located in build\libs\
    echo.
    dir build\libs\*.jar
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%
    echo Please check the error messages above
)

echo.
echo Press any key to exit...
pause >nul
