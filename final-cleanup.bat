@echo off
setlocal enabledelayedexpansion

echo.
echo QR Code Reader - Nettoyage Final
echo ===============================
echo.

:: Phase 1: Suppression des fichiers dupliqués
echo [1/5] Suppression des fichiers dupliques...

:: Supprimer les fichiers *-simple.kt qui causent des redéclarations
if exist "src\main\kotlin\Main-simple.kt" (
    del "src\main\kotlin\Main-simple.kt"
    echo [OK] Main-simple.kt supprime
)

:: Supprimer les fichiers de sauvegarde
if exist "src\main\kotlin\*.backup" (
    del "src\main\kotlin\*.backup"
    echo [OK] Fichiers backup supprimes
)

:: Phase 2: Nettoyage des dossiers dupliqués
echo [2/5] Nettoyage structure dupliquee...

:: Garder seulement la version dans src/main/kotlin/qr/ et supprimer les autres
if exist "src\main\kotlin\QRCodeReader.kt" (
    del "src\main\kotlin\QRCodeReader.kt"
    echo [OK] QRCodeReader.kt duplique supprime (garde celui dans qr/)
)

:: Phase 3: Création d'un Main.kt minimal fonctionnel
echo [3/5] Creation Main.kt minimal...

echo package main > "src\main\kotlin\Main.kt"
echo. >> "src\main\kotlin\Main.kt"
echo import qr.QRCodeReader >> "src\main\kotlin\Main.kt"
echo import java.io.File >> "src\main\kotlin\Main.kt"
echo. >> "src\main\kotlin\Main.kt"
echo fun main(args: Array^<String^>) { >> "src\main\kotlin\Main.kt"
echo     println("QR Code Reader v1.0") >> "src\main\kotlin\Main.kt"
echo     println("==================") >> "src\main\kotlin\Main.kt"
echo. >> "src\main\kotlin\Main.kt"
echo     if (args.isEmpty()) { >> "src\main\kotlin\Main.kt"
echo         println("Usage: java -jar qr-code-reader.jar ^<image-path^>") >> "src\main\kotlin\Main.kt"
echo         return >> "src\main\kotlin\Main.kt"
echo     } >> "src\main\kotlin\Main.kt"
echo. >> "src\main\kotlin\Main.kt"
echo     val imagePath = args[0] >> "src\main\kotlin\Main.kt"
echo     val imageFile = File(imagePath) >> "src\main\kotlin\Main.kt"
echo. >> "src\main\kotlin\Main.kt"
echo     if (!imageFile.exists()) { >> "src\main\kotlin\Main.kt"
echo         println("Erreur: Fichier non trouve: $imagePath") >> "src\main\kotlin\Main.kt"
echo         return >> "src\main\kotlin\Main.kt"
echo     } >> "src\main\kotlin\Main.kt"
echo. >> "src\main\kotlin\Main.kt"
echo     try { >> "src\main\kotlin\Main.kt"
echo         val reader = QRCodeReader() >> "src\main\kotlin\Main.kt"
echo         val result = reader.readQRCode(imagePath) >> "src\main\kotlin\Main.kt"
echo         println("QR Code detecte: $result") >> "src\main\kotlin\Main.kt"
echo     } catch (e: Exception) { >> "src\main\kotlin\Main.kt"
echo         println("Erreur lors de la lecture: ${e.message}") >> "src\main\kotlin\Main.kt"
echo     } >> "src\main\kotlin\Main.kt"
echo } >> "src\main\kotlin\Main.kt"

echo [OK] Main.kt minimal cree

:: Phase 4: Nettoyage des exceptions dupliquées
echo [4/5] Nettoyage exceptions dupliquees...

:: Créer un fichier d'exceptions simplifié
if exist "src\main\kotlin\qr\exceptions" (
    if exist "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt" (
        echo package qr.exceptions > "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        echo. >> "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        echo class QRCodeException(message: String, cause: Throwable? = null) : Exception(message, cause) >> "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        echo. >> "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        echo class InvalidImageFormatException(message: String) : QRCodeException(message) >> "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        echo class ImageProcessingException(message: String, cause: Throwable? = null) : QRCodeException(message, cause) >> "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        echo class InvalidInputException(message: String) : QRCodeException(message) >> "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        echo class ProcessingTimeoutException(message: String) : QRCodeException(message) >> "src\main\kotlin\qr\exceptions\QRCodeExceptions.kt"
        
        echo [OK] Exceptions simplifiees
    )
)

:: Phase 5: Suppression des builds précédents
echo [5/5] Nettoyage builds precedents...
if exist "build" (
    rmdir /s /q "build" 2>nul
    echo [OK] Dossier build nettoye
)

if exist ".gradle" (
    rmdir /s /q ".gradle" 2>nul
    echo [OK] Cache Gradle nettoye
)

echo.
echo ===============================
echo NETTOYAGE FINAL TERMINE
echo ===============================
echo.
echo Le projet est maintenant pret pour:
echo   1. quick-build.bat  (compilation rapide)
echo   2. run-jar.bat      (execution)
echo.
echo Structure minimal creee:
echo   - Main.kt simplifie
echo   - Exceptions de base
echo   - Suppression des doublons
echo.

pause
