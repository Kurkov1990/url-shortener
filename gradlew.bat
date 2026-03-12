@echo off
where gradle >nul 2>&1
if %errorlevel%==0 (
  gradle %*
  exit /b %errorlevel%
)
echo Gradle is not installed and Gradle wrapper JAR is not included in this environment.
echo Install Gradle 9.4+ or generate wrapper locally with: gradle wrapper --gradle-version 9.4.0
exit /b 1
