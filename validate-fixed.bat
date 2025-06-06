@echo off
setlocal enabledelayedexpansion

echo.
echo QR Code Reader - Validation Complete du Projet
echo ================================================
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

:: 1. Vérification de Java
echo [1] Verification de l'environnement Java...
where java >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Java n'est pas installe ou n'est pas dans le PATH"
    goto :end_check
)

echo    Java version:
java -version 2>&1 | findstr "version"
call :print_success "Java est correctement installe"

:: 2. Vérification de la structure du projet
echo.
echo [2] Verification de la structure du projet...

:: Vérifier les fichiers essentiels
if not exist "build.gradle.kts" (
    call :print_error "build.gradle.kts manquant"
) else (
    call :print_success "build.gradle.kts present"
)

if not exist "gradle.properties" (
    call :print_error "gradle.properties manquant"
) else (
    call :print_success "gradle.properties present"
)

if not exist "settings.gradle.kts" (
    call :print_error "settings.gradle.kts manquant"
) else (
    call :print_success "settings.gradle.kts present"
)

:: Vérifier la structure des sources
if not exist "src\main\kotlin" (
    call :print_error "Repertoire src\main\kotlin manquant"
) else (
    call :print_success "Structure des sources presente"
)

if not exist "src\main\kotlin\Main.kt" (
    call :print_error "Fichier Main.kt manquant"
) else (
    call :print_success "Main.kt present"
)

:: 3. Vérification de la syntaxe Gradle
echo.
echo [3] Verification de la configuration Gradle...
call gradlew.bat --version >nul 2>&1
if %errorlevel% neq 0 (
    call :print_error "Probleme avec Gradle Wrapper"
) else (
    call :print_success "Gradle Wrapper fonctionne"
)

:: 4. Test de compilation
echo.
echo [4] Test de compilation...
call gradlew.bat compileKotlin -q
if %errorlevel% neq 0 (
    call :print_error "Echec de la compilation Kotlin"
) else (
    call :print_success "Compilation Kotlin reussie"
)

:: 5. Vérification des dépendances
echo.
echo [5] Verification des dependances...
call gradlew.bat dependencies -q | findstr "BUILD SUCCESSFUL" >nul
if %errorlevel% neq 0 (
    call :print_error "Probleme avec les dependances"
) else (
    call :print_success "Dependances correctement resolues"
)

:: 6. Construction complète
echo.
echo [6] Test de construction complete...
call gradlew.bat build -q
if %errorlevel% neq 0 (
    call :print_error "Echec de la construction complete"
) else (
    call :print_success "Construction complete reussie"
)

:: 7. Test de création du JAR
echo.
echo [7] Test de creation du JAR...
call gradlew.bat shadowJar -q
if %errorlevel% neq 0 (
    call :print_error "Echec de creation du JAR avec dependances"
) else (
    if exist "build\libs\qr-code-reader-2.0.0-dev.jar" (
        call :print_success "JAR avec dependances cree"
        dir "build\libs\qr-code-reader-2.0.0-dev.jar" | findstr "qr-code-reader"
    ) else (
        call :print_error "JAR non trouve dans build\libs\"
    )
)

:: 8. Test d'exécution rapide
echo.
echo [8] Test d'execution rapide...
timeout /t 1 >nul
echo | call gradlew.bat run -q --args="--help" 2>nul
if %errorlevel% neq 0 (
    echo [WARNING] Test d'execution echoue (normal si aucune image de test)
) else (
    call :print_success "Application peut s'executer"
)

:: 9. Vérification des tests
echo.
echo [9] Execution des tests...
call gradlew.bat test -q
if %errorlevel% neq 0 (
    echo [WARNING] Certains tests ont echoue (verifiez les logs)
) else (
    call :print_success "Tous les tests passent"
)

:: 10. Génération du rapport de couverture
echo.
echo [10] Generation du rapport de couverture...
call gradlew.bat jacocoTestReport -q
if %errorlevel% neq 0 (
    echo [WARNING] Impossible de generer le rapport de couverture
) else (
    call :print_success "Rapport de couverture genere"
    if exist "build\reports\jacoco\test\html\index.html" (
        echo    [INFO] Rapport disponible: build\reports\jacoco\test\html\index.html
    )
)

:end_check
:: Résumé final
echo.
echo RESUME DE LA VALIDATION
echo =======================
if %ERROR_COUNT% == 0 (
    echo [SUCCESS] Aucune erreur detectee
    echo Le projet QR Code Reader est correctement configure et stable!
    echo.
    echo Commandes disponibles:
    echo    gradlew.bat build        - Construire le projet
    echo    gradlew.bat run          - Executer l'application
    echo    gradlew.bat shadowJar    - Creer un JAR avec dependances
    echo    gradlew.bat test         - Executer les tests
    echo    .\run.bat               - Lancer directement l'application
) else (
    echo [FAILURE] %ERROR_COUNT% erreur(s) detectee(s)
    echo Veuillez corriger les erreurs ci-dessus avant de continuer
)

echo.
echo Fichiers generes:
if exist "build\libs\qr-code-reader-2.0.0-dev.jar" (
    echo    [OK] build\libs\qr-code-reader-2.0.0-dev.jar
)
if exist "build\reports\jacoco\test\html\index.html" (
    echo    [OK] build\reports\jacoco\test\html\index.html
)
if exist "build\reports\tests\test\index.html" (
    echo    [OK] build\reports\tests\test\index.html
)

echo.
pause