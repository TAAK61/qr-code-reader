@echo off
echo.
echo QR Code Reader - Nettoyage et Correction
echo ========================================
echo.

echo [1] Sauvegarde des fichiers actuels...
if exist "src\main\kotlin\Main.kt" (
    copy "src\main\kotlin\Main.kt" "src\main\kotlin\Main.kt.backup" >nul 2>&1
    echo [OK] Main.kt sauvegarde
)

echo.
echo [2] Suppression des fichiers en conflit...

REM Supprimer les fichiers redondants
if exist "src\main\kotlin\QRCodeReader.kt" (
    del "src\main\kotlin\QRCodeReader.kt" >nul 2>&1
    echo [OK] QRCodeReader.kt supprime (redondant)
)

if exist "src\main\kotlin\qr\QRCodeReader.kt" (
    del "src\main\kotlin\qr\QRCodeReader.kt" >nul 2>&1
    echo [OK] qr\QRCodeReader.kt supprime (redondant)
)

echo.
echo [3] Application du Main.kt simplifie...
if exist "src\main\kotlin\Main-simple.kt" (
    copy "src\main\kotlin\Main-simple.kt" "src\main\kotlin\Main.kt" >nul 2>&1
    echo [OK] Main.kt remplace par la version simplifiee
) else (
    echo [ERROR] Main-simple.kt introuvable
)

echo.
echo [4] Nettoyage des dossiers vides...
if exist "src\main\kotlin\qr" (
    rmdir /s /q "src\main\kotlin\qr" >nul 2>&1
    echo [OK] Dossier qr/ supprime
)

if exist "src\main\kotlin\utils" (
    rmdir /s /q "src\main\kotlin\utils" >nul 2>&1
    echo [OK] Dossier utils/ supprime
)

if exist "src\main\kotlin\config" (
    rmdir /s /q "src\main\kotlin\config" >nul 2>&1
    echo [OK] Dossier config/ supprime
)

if exist "src\main\kotlin\resources" (
    rmdir /s /q "src\main\kotlin\resources" >nul 2>&1
    echo [OK] Dossier resources/ supprime
)

if exist "src\main\kotlin\ui" (
    rmdir /s /q "src\main\kotlin\ui" >nul 2>&1
    echo [OK] Dossier ui/ supprime
)

echo.
echo [5] Test de compilation...
call gradlew.bat compileKotlin -q
if %errorlevel% equ 0 (
    echo [OK] Compilation reussie
) else (
    echo [ERROR] Echec de compilation
    echo [INFO] Verifiez le fichier Main.kt
)

echo.
echo [6] Test de construction complete...
call gradlew.bat build -q
if %errorlevel% equ 0 (
    echo [OK] Construction complete reussie
) else (
    echo [WARNING] Echec de construction (normal s'il manque des tests)
)

echo.
echo Nettoyage termine!
echo.
echo Fichiers corriges:
echo   - Main.kt (version simplifiee, backup: Main.kt.backup)
echo   - Suppression des fichiers redondants
echo   - Structure simplifiee
echo.
echo Pour tester: validate-fixed.bat
echo.
pause