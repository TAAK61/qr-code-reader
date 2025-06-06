@echo off
echo.
echo QR Code Reader - Correction Complete Automatique
echo ===============================================
echo.

echo Cette operation va corriger automatiquement tous les problemes detectes:
echo - Configuration Gradle incompatible
echo - Conflits de classes (redeclarations)
echo - Fichiers redondants
echo.
echo Voulez-vous continuer? (O/N)
set /p confirm=
if /i not "%confirm%"=="O" if /i not "%confirm%"=="Y" if /i not "%confirm%"=="YES" if /i not "%confirm%"=="OUI" (
    echo Operation annulee.
    pause
    exit /b 0
)

echo.
echo [ETAPE 1/3] Migration de la configuration Gradle...
call migrate-gradle.bat >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Configuration Gradle corrigee
) else (
    echo [ERROR] Echec de la migration Gradle
    pause
    exit /b 1
)

echo.
echo [ETAPE 2/3] Nettoyage des conflits de code...
call cleanup-conflicts.bat >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Conflits de code resolus
) else (
    echo [ERROR] Echec du nettoyage
    pause
    exit /b 1
)

echo.
echo [ETAPE 3/3] Validation finale du projet...
echo.
call validate-fixed.bat

echo.
echo ========================================
echo CORRECTION COMPLETE TERMINEE!
echo ========================================
echo.
echo Le projet QR Code Reader est maintenant pret a etre utilise.
echo.
echo Commandes disponibles:
echo   run-fixed.bat           - Lancer l'application
echo   validate-fixed.bat      - Valider le projet
echo   gradlew build           - Construire le projet
echo   gradlew run             - Executer directement
echo.
pause