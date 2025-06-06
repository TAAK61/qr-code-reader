@echo off
setlocal enabledelayedexpansion

echo.
echo QR Code Reader - Validation Rapide du Projet
echo ============================================
echo.

:: Configuration
set ERROR_COUNT=0

:: Fonction pour afficher les erreurs
:print_error
set /a ERROR_COUNT+=1
echo [ERROR] %~1
goto :eof

:: Fonction pour afficher les succès
:print_success
echo [OK] %~1
goto :eof

:: 1. Vérification RAPIDE de Java
echo [1] Verification RAPIDE de Java...
where java >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Java n'est pas installe ou n'est pas dans le PATH"
    goto :end_check
) else (
    call :print_success "Java est disponible dans le PATH"
)

:: Version Java rapide - sans filtrage
echo    Verification version Java...
java -version 2>temp_java_version.txt
if %errorlevel% equ 0 (
    call :print_success "Java fonctionne correctement"
    del temp_java_version.txt 2>nul
) else (
    call :print_error "Probleme avec l'execution de Java"
    del temp_java_version.txt 2>nul
    goto :end_check
)

:: 2. Vérification RAPIDE de la structure
echo.
echo [2] Verification structure (fichiers essentiels)...

:: Vérification rapide des fichiers critiques
set CRITICAL_FILES=build.gradle.kts settings.gradle.kts src\main\kotlin\Main.kt
set MISSING_FILES=0

for %%f in (%CRITICAL_FILES%) do (
    if not exist "%%f" (
        echo [ERROR] Fichier critique manquant: %%f
        set /a MISSING_FILES+=1
        set /a ERROR_COUNT+=1
    )
)

if %MISSING_FILES% equ 0 (
    call :print_success "Tous les fichiers critiques presents"
) else (
    echo [ERROR] %MISSING_FILES% fichier(s) critique(s) manquant(s)
)

:: 3. Test RAPIDE de Gradle
echo.
echo [3] Test RAPIDE de Gradle...
if not exist "gradlew.bat" (
    call :print_error "gradlew.bat manquant"
    goto :end_check
)

:: Test rapide sans sortie complète
call gradlew.bat --version >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Gradle Wrapper ne fonctionne pas"
) else (
    call :print_success "Gradle Wrapper OK"
)

:: 4. Test de syntaxe RAPIDE
echo.
echo [4] Test syntaxe Kotlin (rapide)...
call gradlew.bat compileKotlin --dry-run >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Possible probleme de syntaxe (test complet recommande)
) else (
    call :print_success "Syntaxe Kotlin semble correcte"
)

:: 5. Test RAPIDE des dépendances
echo.
echo [5] Verification dependances (cache)...
if exist ".gradle\caches" (
    call :print_success "Cache Gradle present - dependances probablement OK"
) else (
    echo [INFO] Pas de cache - premiere construction sera plus longue
)

:: 6. Vérification des fichiers BUILD
echo.
echo [6] Verification fichiers de construction...
if exist "build\classes\kotlin\main" (
    call :print_success "Classes compilees trouvees"
) else (
    echo [INFO] Aucune classe compilee - construction necessaire
)

:end_check
:: Résumé RAPIDE
echo.
echo RESUME RAPIDE
echo =============
if %ERROR_COUNT% equ 0 (
    echo [SUCCESS] Validation rapide reussie!
    echo.
    echo Pour une validation complete, utilisez: validate-fixed.bat
    echo Pour construire le projet: gradlew.bat build
    echo Pour lancer l'application: run-fixed.bat
) else (
    echo [WARNING] %ERROR_COUNT% probleme(s) detecte(s)
    echo.
    echo Executez 'validate-fixed.bat' pour un diagnostic complet
)

echo.
echo Temps ecoule: quelques secondes (vs plusieurs minutes pour validation complete)
echo.
pause
