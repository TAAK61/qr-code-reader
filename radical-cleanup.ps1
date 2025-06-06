#!/usr/bin/env pwsh
# filepath: c:\Projet_It\vs code\qr-code-reader\radical-cleanup.ps1

Write-Host ""
Write-Host "QR Code Reader - Nettoyage Radical" -ForegroundColor Red
Write-Host "==================================" -ForegroundColor Red
Write-Host ""

# Supprimer TOUS les fichiers source problématiques
Write-Host "[1/4] Suppression complète des sources..." -ForegroundColor Yellow

# Supprimer complètement le dossier src
if (Test-Path "src") {
    Remove-Item "src" -Recurse -Force
    Write-Host "[OK] Dossier src supprimé complètement" -ForegroundColor Green
}

# Recréer la structure minimale
Write-Host ""
Write-Host "[2/4] Recréation structure minimale..." -ForegroundColor Yellow

# Créer les dossiers
New-Item -ItemType Directory -Path "src\main\kotlin\main" -Force | Out-Null
New-Item -ItemType Directory -Path "src\main\kotlin\qr" -Force | Out-Null
New-Item -ItemType Directory -Path "src\test\kotlin" -Force | Out-Null

Write-Host "[OK] Structure de base recréée" -ForegroundColor Green

# Créer Main.kt ultra-simple
Write-Host ""
Write-Host "[3/4] Création fichiers ultra-simples..." -ForegroundColor Yellow

$mainContent = @"
package main

import qr.QRCodeReader
import java.io.File

fun main(args: Array<String>) {
    println("QR Code Reader v1.0")
    
    if (args.isEmpty()) {
        println("Usage: java -jar qr-code-reader.jar <image-path>")
        return
    }
    
    val imagePath = args[0]
    
    try {
        val reader = QRCodeReader()
        val result = reader.readQRCode(imagePath)
        println("QR Code trouvé: `$result")
    } catch (e: Exception) {
        println("Erreur: `${e.message}")
    }
}
"@

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
        
        val image = ImageIO.read(file) ?: throw IllegalArgumentException("Image invalide")
        return readQRCode(image)
    }
    
    fun readQRCode(image: BufferedImage): String {
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        
        return try {
            val result = reader.decode(bitmap)
            result.text
        } catch (e: NotFoundException) {
            throw RuntimeException("Aucun QR code trouvé")
        } catch (e: Exception) {
            throw RuntimeException("Erreur lecture QR: `${e.message}")
        }
    }
}
"@

$testContent = @"
package qr

import org.junit.Test
import org.junit.Assert.*

class QRCodeReaderTest {
    
    @Test
    fun testQRCodeReaderExists() {
        val reader = QRCodeReader()
        assertNotNull(reader)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testFileNotFound() {
        val reader = QRCodeReader()
        reader.readQRCode("nonexistent.png")
    }
}
"@

# Écrire les fichiers
$mainContent | Out-File -FilePath "src\main\kotlin\main\Main.kt" -Encoding UTF8
$qrReaderContent | Out-File -FilePath "src\main\kotlin\qr\QRCodeReader.kt" -Encoding UTF8
$testContent | Out-File -FilePath "src\test\kotlin\QRCodeReaderTest.kt" -Encoding UTF8

Write-Host "[OK] Main.kt créé" -ForegroundColor Green
Write-Host "[OK] QRCodeReader.kt créé" -ForegroundColor Green
Write-Host "[OK] Test de base créé" -ForegroundColor Green

# Mise à jour build.gradle.kts ultra-simple
Write-Host ""
Write-Host "[4/4] Build ultra-simple..." -ForegroundColor Yellow

$buildContent = @"
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

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    
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

$buildContent | Out-File -FilePath "build.gradle.kts" -Encoding UTF8
Write-Host "[OK] build.gradle.kts ultra-simple créé" -ForegroundColor Green

Write-Host ""
Write-Host "Nettoyage des caches..." -ForegroundColor Yellow
@("build", ".gradle") | ForEach-Object {
    if (Test-Path $_) {
        Remove-Item $_ -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "[OK] Cache $_ nettoyé" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "==================================" -ForegroundColor Red
Write-Host "NETTOYAGE RADICAL TERMINÉ!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Red
Write-Host ""
Write-Host "Structure créée:" -ForegroundColor White
Write-Host "  - src/main/kotlin/main/Main.kt" -ForegroundColor Gray
Write-Host "  - src/main/kotlin/qr/QRCodeReader.kt" -ForegroundColor Gray
Write-Host "  - src/test/kotlin/QRCodeReaderTest.kt" -ForegroundColor Gray
Write-Host "  - build.gradle.kts ultra-simple" -ForegroundColor Gray
Write-Host ""
Write-Host "Testez maintenant:" -ForegroundColor White
Write-Host "  .\gradlew clean build" -ForegroundColor Cyan
