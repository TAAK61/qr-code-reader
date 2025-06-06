@echo off
setlocal enabledelayedexpansion

echo.
echo QR Code Reader - Execution JAR
echo ==============================
echo.

:: Vérification du JAR
if not exist "build\libs\qr-code-reader-1.0-all.jar" (
    echo [ERROR] JAR non trouve!
    echo.
    echo Le JAR doit etre compile d'abord:
    echo   quick-build.bat
    echo.
    pause
    exit /b 1
)

echo [INFO] Lancement du QR Code Reader...
echo.
echo ==============================

:: Lancement avec gestion d'erreur
java -jar "build\libs\qr-code-reader-1.0-all.jar"

set JAVA_EXIT_CODE=%errorlevel%

echo.
echo ==============================
if %JAVA_EXIT_CODE% equ 0 (
    echo [OK] Application terminee normalement
) else (
    echo [WARNING] Application terminee avec code: %JAVA_EXIT_CODE%
)

echo.
echo Appuyez sur une touche pour fermer...
pause >nul
