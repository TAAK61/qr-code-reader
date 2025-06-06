@echo off
setlocal enabledelayedexpansion

echo.
echo QR Code Reader - Validation Ultra-Rapide
echo ========================================
echo.

:: Configuration
set ERROR_COUNT=0
set STARTTIME=%time%

:: Fonction d'erreur
:print_error
set /a ERROR_COUNT+=1
echo [ERROR] %~1
goto :eof

:: Fonction de succès
:print_success
echo [OK] %~1
goto :eof

:: 1. Vérification Java ULTRA-RAPIDE (sans version)
echo [1/3] Verification rapide Java...
where java >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Java manquant dans PATH"
) else (
    call :print_success "Java detecte"
)

:: 2. Vérification structure projet
echo [2/3] Verification structure...

:: Fichiers critiques
if exist "build.gradle.kts" (call :print_success "build.gradle.kts") else (call :print_error "build.gradle.kts manquant")
if exist "settings.gradle.kts" (call :print_success "settings.gradle.kts") else (call :print_error "settings.gradle.kts manquant")
if exist "gradle.properties" (call :print_success "gradle.properties") else (call :print_error "gradle.properties manquant")

:: Source structure
if exist "src\main\kotlin" (call :print_success "Structure source Kotlin") else (call :print_error "Dossier src manquant")
if exist "src\main\kotlin\Main.kt" (call :print_success "Main.kt") else (call :print_error "Main.kt manquant")
if exist "src\main\kotlin\QRCodeReader.kt" (call :print_success "QRCodeReader.kt") else (call :print_error "QRCodeReader.kt manquant")

:: Test structure
if exist "src\test\kotlin" (call :print_success "Structure tests") else (call :print_error "Dossier tests manquant")

:: 3. Vérification configuration Gradle
echo [3/3] Verification configuration...

findstr /C:"kotlin" build.gradle.kts >nul 2>&1
if %errorlevel% equ 0 (call :print_success "Configuration Kotlin") else (call :print_error "Configuration Kotlin manquante")

findstr /C:"zxing" build.gradle.kts >nul 2>&1
if %errorlevel% equ 0 (call :print_success "Dependance ZXing") else (call :print_error "ZXing manquant")

findstr /C:"junit" build.gradle.kts >nul 2>&1
if %errorlevel% equ 0 (call :print_success "Configuration tests") else (call :print_error "Tests non configures")

:: Résumé
echo.
echo ========================================
if %ERROR_COUNT% equ 0 (
    echo STATUS: PROJET VALIDE ✓
    echo Aucune erreur detectee
    echo Le projet est pret pour la compilation
) else (
    echo STATUS: %ERROR_COUNT% ERREUR(S) DETECTEE(S) ✗
    echo Veuillez corriger les erreurs avant de continuer
)

set ENDTIME=%time%
echo.
echo Validation terminee en quelques secondes
echo.

if %ERROR_COUNT% equ 0 (exit /b 0) else (exit /b 1)
