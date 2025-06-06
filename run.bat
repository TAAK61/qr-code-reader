@echo off
echo 🚀 QR Code Reader - Lancement de l'application
echo ============================================
echo.

echo 📋 Vérification de l'environnement...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Erreur: Java n'est pas installé ou n'est pas dans le PATH
    echo 💡 Veuillez installer Java 11 ou supérieur
    pause
    exit /b 1
)

echo ✅ Java détecté
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr "version"') do echo    %%i

echo.
echo 📦 Construction du projet...
call gradlew.bat clean build -q

if %errorlevel% neq 0 (
    echo ❌ Erreur lors de la construction du projet
    echo 💡 Exécutez 'validate.bat' pour plus de détails
    pause
    exit /b 1
)

echo ✅ Construction réussie
echo.
echo 🎯 Démarrage de l'application QR Code Reader...
echo.

call gradlew.bat run

echo.
echo 📊 Session terminée. Appuyez sur une touche pour fermer...
pause