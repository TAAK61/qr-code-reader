@echo off
setlocal enabledelayedexpansion

echo.
echo QR Code Reader - Compilation Rapide
echo ===================================
echo.

:: Vérification préliminaire
if not exist "build.gradle.kts" (
    echo [ERROR] Pas de build.gradle.kts trouve
    echo Veuillez executer fix-all.bat d'abord
    pause
    exit /b 1
)

:: Nettoyage rapide
echo [1/4] Nettoyage build...
if exist "build" rmdir /s /q "build" 2>nul
echo [OK] Build nettoye

:: Compilation
echo [2/4] Compilation Kotlin...
call gradlew compileKotlin --console=plain --quiet
if %errorlevel% neq 0 (
    echo [ERROR] Echec compilation Kotlin
    echo.
    echo Executez pour plus de details:
    echo   gradlew compileKotlin
    pause
    exit /b 1
)
echo [OK] Compilation reussie

:: Tests rapides
echo [3/4] Tests unitaires...
call gradlew test --console=plain --quiet
if %errorlevel% neq 0 (
    echo [WARNING] Certains tests ont echoue
    echo Continuons avec la creation du JAR...
) else (
    echo [OK] Tests passes
)

:: Création JAR
echo [4/4] Creation JAR executable...
call gradlew shadowJar --console=plain --quiet
if %errorlevel% neq 0 (
    echo [ERROR] Echec creation JAR
    pause
    exit /b 1
)

:: Vérification du JAR
if exist "build\libs\qr-code-reader-1.0-all.jar" (
    echo [OK] JAR cree: build\libs\qr-code-reader-1.0-all.jar
    
    echo.
    echo ===================================
    echo COMPILATION TERMINEE AVEC SUCCES!
    echo ===================================
    echo.
    echo Pour tester:
    echo   java -jar build\libs\qr-code-reader-1.0-all.jar
    echo.
    echo Ou utilisez:
    echo   run-jar.bat
    echo.
) else (
    echo [ERROR] JAR non trouve apres compilation
    exit /b 1
)

echo Compilation terminee en quelques secondes
pause
