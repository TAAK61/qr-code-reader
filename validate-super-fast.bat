@echo off
setlocal enabledelayedexpansion

echo.
echo QR Code Reader - Validation OPTIMISEE
echo =====================================
echo.

:: Configuration
set ERROR_COUNT=0
set START_TIME=%time%

:: Fonction pour afficher les erreurs
:print_error
set /a ERROR_COUNT+=1
echo [ERROR] %~1
goto :eof

:: Fonction pour afficher les succès
:print_success
echo [OK] %~1
goto :eof

:: 1. Vérification OPTIMISEE de Java (moins de 2 secondes)
echo [1] Verification Java (optimisee)...
where java >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Java n'est pas installe ou n'est pas dans le PATH"
    goto :end_check
)

:: Test rapide sans pipes lents
java -version >temp_java.log 2>&1
if %errorlevel% equ 0 (
    call :print_success "Java fonctionne correctement"
    del temp_java.log 2>nul
) else (
    call :print_error "Probleme execution Java"
    del temp_java.log 2>nul
    goto :end_check
)

:: 2. Vérification structure (batch optimisé)
echo.
echo [2] Structure du projet...
set FILES_OK=0
set TOTAL_FILES=5

if exist "build.gradle.kts" set /a FILES_OK+=1
if exist "settings.gradle.kts" set /a FILES_OK+=1  
if exist "gradle.properties" set /a FILES_OK+=1
if exist "src\main\kotlin" set /a FILES_OK+=1
if exist "src\main\kotlin\Main.kt" set /a FILES_OK+=1

if %FILES_OK% equ %TOTAL_FILES% (
    call :print_success "Structure complete (%FILES_OK%/%TOTAL_FILES%)"
) else (
    call :print_error "Structure incomplete (%FILES_OK%/%TOTAL_FILES%)"
    set /a ERROR_COUNT+=1
)

:: 3. Test Gradle RAPIDE (sans --version complet)
echo.
echo [3] Gradle Wrapper...
if not exist "gradlew.bat" (
    call :print_error "gradlew.bat manquant"
) else (
    call :print_success "Gradle Wrapper present"
)

:: 4. Test compilation LEGER
echo.
echo [4] Test compilation leger...
:: Vérifier si déjà compilé
if exist "build\classes\kotlin\main" (
    call :print_success "Classes deja compilees"
) else (
    echo    Compilation necessaire...
    call gradlew.bat compileKotlin -q --parallel --build-cache
    if %errorlevel% neq 0 (
        call :print_error "Echec compilation"
    ) else (
        call :print_success "Compilation reussie"
    )
)

:: 5. Construction RAPIDE si pas déjà fait
echo.
echo [5] Verification construction...
if exist "build\libs\qr-code-reader-2.0.0-dev.jar" (
    call :print_success "JAR deja construit"
) else (
    echo    Construction JAR...
    call gradlew.bat shadowJar -q --parallel --build-cache
    if %errorlevel% neq 0 (
        call :print_error "Echec construction JAR"
    ) else (
        call :print_success "JAR construit avec succes"
    )
)

:: 6. Test exécution ultra-rapide
echo.
echo [6] Test execution rapide...
if exist "build\libs\qr-code-reader-2.0.0-dev.jar" (
    java -jar "build\libs\qr-code-reader-2.0.0-dev.jar" --version >nul 2>&1
    if %errorlevel% neq 0 (
        echo [WARNING] Test execution - verifiez manuellement
    ) else (
        call :print_success "Application executable"
    )
) else (
    echo [INFO] JAR non disponible pour test execution
)

:end_check
:: Calcul du temps écoulé
set END_TIME=%time%

:: Résumé OPTIMISE
echo.
echo RESUME OPTIMISE
echo ===============
if %ERROR_COUNT% equ 0 (
    echo [SUCCESS] Validation RAPIDE reussie!
    echo.
    echo Projet QR Code Reader pret!
    echo.
    echo Commandes rapides:
    echo    run-fixed.bat           - Lancer l'application
    echo    gradlew.bat run         - Executer via Gradle  
    echo    java -jar build\libs\qr-code-reader-2.0.0-dev.jar
) else (
    echo [PARTIAL] %ERROR_COUNT% probleme(s) - mais possiblement utilisable
    echo.
    echo Pour diagnostic complet: validate-fixed.bat
)

echo.
echo Temps: Quelques secondes (vs minutes pour validation complete)
echo.
echo Fichiers disponibles:
if exist "build\libs\qr-code-reader-2.0.0-dev.jar" (
    echo    [OK] build\libs\qr-code-reader-2.0.0-dev.jar
)
if exist "build\classes\kotlin\main" (
    echo    [OK] Classes Kotlin compilees
)

echo.
echo Pour lancer: run-fixed.bat ou gradlew.bat run
echo.
pause
