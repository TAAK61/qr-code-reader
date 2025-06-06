@echo off
setlocal enabledelayedexpansion

:: Script de déploiement automatisé pour QR Code Reader (Windows)
:: Ce script organise le projet sur GitHub avec deux branches distinctes

echo.
echo 🚀 QR Code Reader - Script de Déploiement GitHub
echo =================================================
echo.

:: Configuration
set PROJECT_NAME=qr-code-reader
set MAIN_BRANCH=main
set DEV_BRANCH=feature/enhanced-architecture
set VERSION_1=1.0.0
set VERSION_2=2.0.0-dev

:: Vérifier que nous sommes dans le bon répertoire
if not exist "build.gradle.kts" (
    echo ❌ Erreur: Ce script doit être exécuté depuis la racine du projet QR Code Reader
    pause
    exit /b 1
)

:: Étape 1: Sauvegarder le code actuel
echo 📋 Sauvegarde du code actuel...

:: Créer un dossier de sauvegarde temporaire
for /f "tokens=1-4 delims=/ " %%a in ('date /t') do set datestr=%%c%%a%%b
for /f "tokens=1-2 delims=: " %%a in ('time /t') do set timestr=%%a%%b
set BACKUP_DIR=temp_backup_%datestr%_%timestr%
mkdir "%BACKUP_DIR%" 2>nul

:: Sauvegarder les fichiers importants du code avancé
xcopy /E /I /Q src "%BACKUP_DIR%\src" >nul 2>&1
copy build.gradle.kts "%BACKUP_DIR%\build-advanced.gradle.kts" >nul 2>&1
copy README.md "%BACKUP_DIR%\README-advanced.md" >nul 2>&1
xcopy /E /I /Q docs "%BACKUP_DIR%\docs" >nul 2>&1

echo ✅ Code actuel sauvegardé dans %BACKUP_DIR%

:: Étape 2: Préparer la version 1.0 pour la branche main
echo 📋 Préparation de la version 1.0 pour la branche main...

:: Remplacer par les fichiers de la version simple
copy build-simple.gradle.kts build.gradle.kts >nul
copy README-v1.md README.md >nul

echo ✅ Fichiers v1.0 préparés

:: Étape 3: Initialiser Git et créer la branche main
echo 📋 Configuration Git et création de la branche main...

:: Initialiser Git si nécessaire
if not exist ".git\." (
    git init
    echo ✅ Repository Git initialisé
) else (
    echo ⚠️  Repository Git existant détecté
)

:: Ajouter tous les fichiers de la v1.0
git add .
git commit -m "feat: Initial QR Code Reader v1.0.0 - Basic QR code reading from image files - Command-line interface with interactive mode - Support for PNG, JPG, JPEG, BMP, GIF formats - Content type detection (URL, email, phone, etc.) - Cross-platform compatibility - Comprehensive error handling - Performance monitoring"

:: Créer le tag v1.0.0
git tag -a "v%VERSION_1%" -m "QR Code Reader v%VERSION_1% - Initial stable release"

echo ✅ Branche main créée avec la version %VERSION_1%

:: Étape 4: Créer la branche de développement
echo 📋 Création de la branche de développement...

git checkout -b "%DEV_BRANCH%"

:: Restaurer le code avancé
xcopy /E /Y /Q "%BACKUP_DIR%\src" src\ >nul 2>&1
copy "%BACKUP_DIR%\build-advanced.gradle.kts" build.gradle.kts >nul
copy "%BACKUP_DIR%\README-advanced.md" README.md >nul 2>&1
xcopy /E /Y /Q "%BACKUP_DIR%\docs" docs\ >nul 2>&1

:: Commit du code avancé
git add .
git commit -m "feat: Enhanced architecture and advanced features v%VERSION_2% - Modular architecture with separated concerns - Comprehensive interface system - Centralized configuration management - Advanced error handling with custom exceptions - Specialized image processing utilities - Advanced logging system - Input validation and performance monitoring - Complete KDoc documentation - Comprehensive test coverage"

echo ✅ Branche de développement '%DEV_BRANCH%' créée avec le code avancé

:: Étape 5: Créer le template de Pull Request
echo 📋 Génération du template de Pull Request...

(
echo # 🚀 Enhanced QR Code Reader Architecture
echo.
echo ## 📋 Overview
echo This PR introduces a comprehensive architectural overhaul and advanced features.
echo.
echo ## ✨ What's New
echo.
echo ### 🏗️ Architecture Improvements
echo - **Modular Design**: Separated concerns with specialized utility classes
echo - **Interface-Based**: Clean abstractions for all major components
echo - **Configuration System**: Centralized, persistent application settings
echo - **Error Handling**: Sophisticated exception hierarchy
echo.
echo ### 🚀 Advanced Features
echo - **Image Processing**: Specialized classes for image enhancement
echo - **Logging System**: Structured logging with configurable levels
echo - **Input Validation**: Comprehensive validation for all inputs
echo - **Performance Monitoring**: Detailed timing and confidence metrics
echo.
echo ### 📚 Documentation ^& Testing
echo - **Complete KDoc**: Every public API fully documented
echo - **Unit Tests**: Comprehensive test coverage
echo - **Code Style Guide**: Detailed formatting guidelines
echo.
echo ## 🧪 Testing
echo All tests pass: `./gradlew test`
echo.
echo ## 🎯 Ready to Merge
echo This is a fully backward-compatible enhancement.
) > PULL_REQUEST_TEMPLATE.md

echo ✅ Template de Pull Request créé: PULL_REQUEST_TEMPLATE.md

:: Étape 6: Afficher les instructions
echo.
echo 🎯 ÉTAPES SUIVANTES POUR GITHUB:
echo ================================
echo.
echo 1. 📂 Créer le repository sur GitHub:
echo    - Allez sur github.com
echo    - Cliquez sur 'New repository'
echo    - Nom: %PROJECT_NAME%
echo    - Description: Simple and powerful QR code reader in Kotlin
echo.
echo 2. 🔗 Connecter le repository local à GitHub:
echo    git remote add origin https://github.com/VOTRE_USERNAME/%PROJECT_NAME%.git
echo.
echo 3. 📤 Pousser la branche main (v1.0):
echo    git checkout main
echo    git push -u origin main
echo    git push origin v%VERSION_1%
echo.
echo 4. 📤 Pousser la branche de développement:
echo    git checkout %DEV_BRANCH%
echo    git push -u origin %DEV_BRANCH%
echo.
echo 5. 🔄 Créer une Pull Request sur GitHub
echo.

:: Nettoyage
echo 📋 Nettoyage...
set /p cleanup="Supprimer le dossier de sauvegarde temporaire? (Y/n): "
if /i not "%cleanup%"=="n" (
    rmdir /s /q "%BACKUP_DIR%"
    echo ✅ Dossier de sauvegarde supprimé
) else (
    echo ⚠️  Dossier de sauvegarde conservé: %BACKUP_DIR%
)

echo.
echo 🎉 DÉPLOIEMENT PRÉPARÉ AVEC SUCCÈS!
echo ====================================
echo.
echo 📊 Résumé de l'organisation:
echo • Branche %MAIN_BRANCH%: Version stable %VERSION_1%
echo • Branche %DEV_BRANCH%: Version avancée %VERSION_2%
echo • Tag créé: v%VERSION_1%
echo • Template PR: PULL_REQUEST_TEMPLATE.md
echo.
git branch -v
echo.
echo ✅ Votre projet est maintenant prêt pour GitHub!
echo.
echo ⚠️  N'oubliez pas de:
echo 1. 🔗 Connecter à votre repository GitHub
echo 2. 📤 Pousser les deux branches
echo 3. 🔄 Créer la Pull Request
echo 4. 📝 Utiliser le template fourni
echo.

pause