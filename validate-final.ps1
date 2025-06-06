#!/usr/bin/env pwsh
# filepath: c:\Projet_It\vs code\qr-code-reader\validate-final.ps1

Write-Host ""
Write-Host "QR Code Reader - Validation Finale" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""

$errors = 0

# Test 1: Structure des fichiers
Write-Host "[1/5] Vérification structure..." -ForegroundColor Yellow
$requiredFiles = @(
    "build.gradle.kts",
    "settings.gradle.kts", 
    "src\main\kotlin\main\Main.kt",
    "src\main\kotlin\qr\QRCodeReader.kt",
    "src\test\kotlin\QRCodeReaderTest.kt"
)

foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file manquant" -ForegroundColor Red
        $errors++
    }
}

# Test 2: Compilation rapide
Write-Host ""
Write-Host "[2/5] Test compilation..." -ForegroundColor Yellow
$compileResult = & .\gradlew compileKotlin --console=plain --quiet 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Compilation réussie" -ForegroundColor Green
} else {
    Write-Host "  ✗ Échec compilation" -ForegroundColor Red
    $errors++
}

# Test 3: Tests unitaires
Write-Host ""
Write-Host "[3/5] Tests unitaires..." -ForegroundColor Yellow
$testResult = & .\gradlew test --console=plain --quiet 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Tests passés" -ForegroundColor Green
} else {
    Write-Host "  ✗ Échec tests" -ForegroundColor Red
    $errors++
}

# Test 4: Création JAR
Write-Host ""
Write-Host "[4/5] Création JAR..." -ForegroundColor Yellow
$jarResult = & .\gradlew shadowJar --console=plain --quiet 2>&1
if ($LASTEXITCODE -eq 0 -and (Test-Path "build\libs\qr-code-reader-1.0-all.jar")) {
    Write-Host "  ✓ JAR créé avec succès" -ForegroundColor Green
    $jarSize = (Get-Item "build\libs\qr-code-reader-1.0-all.jar").Length / 1MB
    Write-Host "    Taille: $([math]::Round($jarSize, 2)) MB" -ForegroundColor Gray
} else {
    Write-Host "  ✗ Échec création JAR" -ForegroundColor Red
    $errors++
}

# Test 5: Test d'exécution basique
Write-Host ""
Write-Host "[5/5] Test d'exécution..." -ForegroundColor Yellow
if (Test-Path "build\libs\qr-code-reader-1.0-all.jar") {
    $execResult = & java -jar "build\libs\qr-code-reader-1.0-all.jar" 2>&1
    if ($execResult -match "QR Code Reader v1.0") {
        Write-Host "  ✓ Exécution fonctionnelle" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Problème d'exécution" -ForegroundColor Red
        $errors++
    }
} else {
    Write-Host "  ✗ JAR non disponible pour test" -ForegroundColor Red
    $errors++
}

# Résumé final
Write-Host ""
Write-Host "==================================" -ForegroundColor Green
if ($errors -eq 0) {
    Write-Host "🎉 PROJET COMPLÈTEMENT FONCTIONNEL!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prêt pour:" -ForegroundColor White
    Write-Host "  • Déploiement GitHub" -ForegroundColor Gray
    Write-Host "  • Distribution JAR" -ForegroundColor Gray
    Write-Host "  • Utilisation production" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Commandes utiles:" -ForegroundColor White
    Write-Host "  java -jar build\libs\qr-code-reader-1.0-all.jar <image>" -ForegroundColor Cyan
} else {
    Write-Host "❌ $errors ERREURS DÉTECTÉES" -ForegroundColor Red
    Write-Host "Le projet nécessite encore des corrections" -ForegroundColor Red
}
Write-Host "==================================" -ForegroundColor Green

exit $errors
