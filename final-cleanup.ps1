#!/usr/bin/env pwsh
# filepath: c:\Projet_It\vs code\qr-code-reader\final-cleanup.ps1

Write-Host ""
Write-Host "QR Code Reader - Nettoyage Final PowerShell" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# Phase 1: Suppression des fichiers dupliqués
Write-Host "[1/6] Suppression des fichiers dupliqués..." -ForegroundColor Yellow

# Supprimer les fichiers en doublon
$duplicateFiles = @(
    "src\main\kotlin\Main-simple.kt",
    "src\main\kotlin\QRCodeReader.kt",
    "bin\main\Main-simple.kt",
    "bin\main\Main.kt",
    "bin\main\QRCodeReader.kt"
)

foreach ($file in $duplicateFiles) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "[OK] Supprimé: $file" -ForegroundColor Green
    }
}

# Phase 2: Nettoyage des caches
Write-Host ""
Write-Host "[2/6] Nettoyage des caches..." -ForegroundColor Yellow

$cacheDirs = @("build", ".gradle", "bin")
foreach ($dir in $cacheDirs) {
    if (Test-Path $dir) {
        Remove-Item $dir -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "[OK] Nettoyé: $dir" -ForegroundColor Green
    }
}

# Phase 3: Création d'un Main.kt minimal
Write-Host ""
Write-Host "[3/6] Création Main.kt minimal..." -ForegroundColor Yellow

$mainKtContent = @"
package main

import qr.QRCodeReader
import java.io.File

fun main(args: Array<String>) {
    println("QR Code Reader v1.0")
    println("==================")
    
    if (args.isEmpty()) {
        println("Usage: java -jar qr-code-reader.jar <image-path>")
        return
    }
    
    val imagePath = args[0]
    val imageFile = File(imagePath)
    
    if (!imageFile.exists()) {
        println("Erreur: Fichier non trouvé: `$imagePath")
        return
    }
    
    try {
        val reader = QRCodeReader()
        val result = reader.readQRCode(imagePath)
        println("QR Code détecté: `$result")
    } catch (e: Exception) {
        println("Erreur lors de la lecture: `${e.message}")
    }
}
"@

$mainKtContent | Out-File -FilePath "src\main\kotlin\Main.kt" -Encoding UTF8
Write-Host "[OK] Main.kt minimal créé" -ForegroundColor Green

# Phase 4: Simplification du QRCodeReader
Write-Host ""
Write-Host "[4/6] Simplification QRCodeReader..." -ForegroundColor Yellow

$qrReaderContent = @"
package qr

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QRCodeReader {
    private val reader = MultiFormatReader()
    
    fun readQRCode(imagePath: String): String {
        val file = File(imagePath)
        if (!file.exists()) {
            throw IllegalArgumentException("Fichier non trouvé: `$imagePath")
        }
        
        val image = ImageIO.read(file)
        return readQRCode(image)
    }
    
    fun readQRCode(image: BufferedImage): String {
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        
        return try {
            val result = reader.decode(bitmap)
            result.text
        } catch (e: NotFoundException) {
            throw RuntimeException("Aucun QR code trouvé dans l'image")
        } catch (e: Exception) {
            throw RuntimeException("Erreur lors de la lecture du QR code: `${e.message}")
        }
    }
}
"@

# Créer le dossier qr s'il n'existe pas
if (!(Test-Path "src\main\kotlin\qr")) {
    New-Item -ItemType Directory -Path "src\main\kotlin\qr" -Force | Out-Null
}

$qrReaderContent | Out-File -FilePath "src\main\kotlin\qr\QRCodeReader.kt" -Encoding UTF8
Write-Host "[OK] QRCodeReader.kt simplifié créé" -ForegroundColor Green

# Phase 5: Mise à jour build.gradle.kts
Write-Host ""
Write-Host "[5/6] Mise à jour build.gradle.kts..." -ForegroundColor Yellow

$buildGradleContent = @"
plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.qrcoder"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("main.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.shadowJar {
    archiveBaseName.set("qr-code-reader")
    archiveVersion.set("1.0")
    archiveClassifier.set("all")
}
"@

$buildGradleContent | Out-File -FilePath "build.gradle.kts" -Encoding UTF8
Write-Host "[OK] build.gradle.kts mis à jour" -ForegroundColor Green

# Phase 6: Validation rapide
Write-Host ""
Write-Host "[6/6] Validation de la structure..." -ForegroundColor Yellow

$requiredFiles = @(
    "build.gradle.kts",
    "settings.gradle.kts", 
    "src\main\kotlin\Main.kt",
    "src\main\kotlin\qr\QRCodeReader.kt"
)

$allPresent = $true
foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "[OK] $file présent" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] $file manquant" -ForegroundColor Red
        $allPresent = $false
    }
}

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
if ($allPresent) {
    Write-Host "NETTOYAGE TERMINÉ AVEC SUCCÈS!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines étapes:" -ForegroundColor White
    Write-Host "  1. .\quick-build.bat  (compilation)" -ForegroundColor Gray
    Write-Host "  2. .\run-jar.bat      (exécution)" -ForegroundColor Gray
} else {
    Write-Host "NETTOYAGE TERMINÉ AVEC ERREURS" -ForegroundColor Red
    Write-Host "Veuillez vérifier les fichiers manquants" -ForegroundColor Red
}
Write-Host "===========================================" -ForegroundColor Cyan
