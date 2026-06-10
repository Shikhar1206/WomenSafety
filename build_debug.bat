@echo off
cd /d c:\Users\agarw\AndroidStudioProjects\WomenSafety
echo === Renaming google-services.json ===
rename "app\google-services (5).json" "google-services.json" 2>nul
if exist "app\google-services.json" (echo google-services.json OK) else (echo WARNING: google-services.json NOT FOUND)
echo.
echo === Running Gradle Build ===
call gradlew.bat assembleDebug --stacktrace 2>&1
