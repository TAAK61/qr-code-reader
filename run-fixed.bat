@echo off
echo QR Code Reader - Lancement de l'application
echo ============================================
echo.

echo Verification de l'environnement...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java n'est pas installe ou n'est pas dans le PATH
    echo [TIP] Veuillez installer Java 11 ou superieur
    pause
    exit /b 1
)

echo [OK] Java detecte
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr "version"') do echo    %%i

echo.
echo Construction du projet...
call gradlew.bat clean build -q

if %errorlevel% neq 0 (
    echo [ERROR] Erreur lors de la construction du projet
    echo [TIP] Executez 'validate-fixed.bat' pour plus de details
    pause
    exit /b 1
)

echo [OK] Construction reussie
echo.
echo Demarrage de l'application QR Code Reader...
echo.

call gradlew.bat run

echo.
echo Application terminee.
pause