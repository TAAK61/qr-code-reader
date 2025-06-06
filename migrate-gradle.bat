@echo off
echo.
echo QR Code Reader - Migration vers version compatible
echo =================================================
echo.

echo [1] Sauvegarde des fichiers originaux...
if exist "settings.gradle.kts" (
    copy "settings.gradle.kts" "settings.gradle.kts.backup" >nul 2>&1
    echo [OK] settings.gradle.kts sauvegarde
)

if exist "build.gradle.kts" (
    copy "build.gradle.kts" "build.gradle.kts.backup" >nul 2>&1
    echo [OK] build.gradle.kts sauvegarde
)

echo.
echo [2] Application des corrections...

if exist "settings-fixed.gradle.kts" (
    copy "settings-fixed.gradle.kts" "settings.gradle.kts" >nul 2>&1
    echo [OK] settings.gradle.kts corrige
) else (
    echo [ERROR] settings-fixed.gradle.kts introuvable
)

if exist "build-fixed.gradle.kts" (
    copy "build-fixed.gradle.kts" "build.gradle.kts" >nul 2>&1
    echo [OK] build.gradle.kts corrige
) else (
    echo [ERROR] build-fixed.gradle.kts introuvable
)

echo.
echo [3] Test de la configuration...
call gradlew.bat --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Gradle fonctionne correctement
) else (
    echo [ERROR] Probleme avec Gradle
)

echo.
echo [4] Test de compilation...
call gradlew.bat compileKotlin -q
if %errorlevel% equ 0 (
    echo [OK] Compilation reussie
) else (
    echo [ERROR] Echec de compilation
    echo [INFO] Verifiez que tous les fichiers sources sont presents
)

echo.
echo Migration terminee!
echo.
echo Fichiers corriges:
echo   - settings.gradle.kts (original sauvegarde: settings.gradle.kts.backup)
echo   - build.gradle.kts (original sauvegarde: build.gradle.kts.backup)
echo.
echo Pour tester: validate-fixed.bat
echo.
pause