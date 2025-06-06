#!/usr/bin/env pwsh
# filepath: c:\Projet_It\vs code\qr-code-reader\deploy-github-final.ps1

Write-Host ""
Write-Host "QR Code Reader - Déploiement GitHub Final" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Vérification préliminaire
if (!(Test-Path "build\libs\qr-code-reader-1.0-all.jar")) {
    Write-Host "❌ JAR non trouvé! Exécutez d'abord:" -ForegroundColor Red
    Write-Host "   .\validate-final.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "[1/8] Préparation du repository Git..." -ForegroundColor Yellow

# Initialiser Git si nécessaire
if (!(Test-Path ".git")) {
    git init
    Write-Host "  ✓ Git initialisé" -ForegroundColor Green
} else {
    Write-Host "  ✓ Git déjà initialisé" -ForegroundColor Green
}

# Configuration Git de base
git config --local user.name "QR Code Reader Developer" 2>$null
git config --local user.email "developer@qrcode-reader.com" 2>$null

Write-Host ""
Write-Host "[2/8] Création du README principal..." -ForegroundColor Yellow

$readmeContent = @"
# QR Code Reader

Un lecteur de QR Code robuste et efficace développé en Kotlin.

## 🚀 Fonctionnalités

- Lecture de QR codes à partir d'images (PNG, JPG, etc.)
- Interface en ligne de commande simple
- Architecture modulaire et extensible
- Tests unitaires complets
- JAR exécutable autonome

## 📦 Installation

### Prérequis
- Java 11 ou supérieur
- Gradle (optionnel, le wrapper est inclus)

### Compilation
```bash
.\gradlew clean build
```

### Exécution
```bash
java -jar build\libs\qr-code-reader-1.0-all.jar <chemin-vers-image>
```

## 📖 Utilisation

```bash
# Lire un QR code depuis une image
java -jar qr-code-reader-1.0-all.jar mon-qr-code.png

# Exemple de sortie
QR Code Reader v1.0
QR Code trouvé: https://github.com/votre-repo
```

## 🏗️ Architecture

```
src/
├── main/kotlin/
│   ├── main/
│   │   └── Main.kt          # Point d'entrée
│   └── qr/
│       └── QRCodeReader.kt  # Logique principale
└── test/kotlin/
    └── QRCodeReaderTest.kt  # Tests unitaires
```

## 🧪 Tests

```bash
.\gradlew test
```

## 📋 Versions

- **v1.0** (main) : Version stable de base
- **v2.0-dev** (development) : Version avancée avec fonctionnalités étendues

## 📄 Licence

MIT License - voir le fichier LICENSE pour plus de détails.

## 🤝 Contribution

1. Fork le projet
2. Créez votre branche feature (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Committez vos changements (`git commit -m 'Ajout nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Ouvrez une Pull Request

---

*Développé avec ❤️ en Kotlin*
"@

$readmeContent | Out-File -FilePath "README.md" -Encoding UTF8
Write-Host "  ✓ README.md créé" -ForegroundColor Green

Write-Host ""
Write-Host "[3/8] Création du .gitignore..." -ForegroundColor Yellow

$gitignoreContent = @"
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

# Kotlin
*.class
*.jar
!build/libs/qr-code-reader-*-all.jar

# IDE
.idea/
.vscode/
*.iml
*.ipr
*.iws

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Temporary files
*.tmp
*.temp
temp/

# Test outputs
test-results/
test-reports/

# Cache
cache/
.cache/
"@

$gitignoreContent | Out-File -FilePath ".gitignore" -Encoding UTF8
Write-Host "  ✓ .gitignore créé" -ForegroundColor Green

Write-Host ""
Write-Host "[4/8] Ajout des fichiers au repository..." -ForegroundColor Yellow

git add .
git commit -m "🎉 Initial commit - QR Code Reader v1.0

✨ Fonctionnalités:
- Lecture de QR codes depuis images
- Architecture Kotlin modulaire  
- Tests unitaires
- JAR exécutable ($('{0:N2}' -f ((Get-Item 'build\libs\qr-code-reader-1.0-all.jar').Length / 1MB)) MB)
- Documentation complète

🏗️ Structure:
- Main.kt : Point d'entrée CLI
- QRCodeReader.kt : Logique principale
- Tests unitaires complets
- Build Gradle optimisé

✅ Status: Prêt pour production"

Write-Host "  ✓ Commit initial créé" -ForegroundColor Green

Write-Host ""
Write-Host "[5/8] Création de la branche main (v1.0)..." -ForegroundColor Yellow

# Renommer la branche actuelle en main
git branch -M main
Write-Host "  ✓ Branche main configurée" -ForegroundColor Green

Write-Host ""
Write-Host "[6/8] Création de la branche development (v2.0-dev)..." -ForegroundColor Yellow

# Créer et basculer vers la branche development
git checkout -b development

# Mettre à jour la version pour development
$buildDevContent = @"
plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.qrcoder"
version = "2.0.0-dev"

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
    archiveVersion.set("2.0-dev")
    archiveClassifier.set("all")
}
"@

$buildDevContent | Out-File -FilePath "build.gradle.kts" -Encoding UTF8

# Mettre à jour le Main.kt pour development
$mainDevContent = @"
package main

import qr.QRCodeReader
import java.io.File

fun main(args: Array<String>) {
    println("QR Code Reader v2.0-dev (Development Branch)")
    println("============================================")
    
    if (args.isEmpty()) {
        println("Usage: java -jar qr-code-reader.jar <image-path>")
        println("")
        println("Development features:")
        println("  • Enhanced error handling")
        println("  • Future: Batch processing")  
        println("  • Future: Multiple formats")
        println("  • Future: Advanced filtering")
        return
    }
    
    val imagePath = args[0]
    
    try {
        val reader = QRCodeReader()
        val result = reader.readQRCode(imagePath)
        println("QR Code trouvé: `$result")
        println("Status: ✅ Lecture réussie (dev mode)")
    } catch (e: Exception) {
        println("❌ Erreur: `${e.message}")
        println("Debug info: `${e.javaClass.simpleName}")
    }
}
"@

$mainDevContent | Out-File -FilePath "src\main\kotlin\main\Main.kt" -Encoding UTF8

git add .
git commit -m "🚀 Development branch v2.0-dev

🔧 Améliorations development:
- Version 2.0.0-dev
- Messages debug améliorés
- Interface préparée pour futures fonctionnalités
- Architecture prête pour extensions

📋 Roadmap v2.0:
- [ ] Traitement par lots
- [ ] Formats multiples  
- [ ] Filtres avancés
- [ ] Interface graphique
- [ ] Configuration avancée

🌟 Branche active pour nouvelles fonctionnalités"

Write-Host "  ✓ Branche development configurée" -ForegroundColor Green

Write-Host ""
Write-Host "[7/8] Retour à la branche main..." -ForegroundColor Yellow
git checkout main
Write-Host "  ✓ Sur branche main (stable v1.0)" -ForegroundColor Green

Write-Host ""
Write-Host "[8/8] Création des tags de version..." -ForegroundColor Yellow
git tag -a "v1.0.0" -m "🏷️ Release v1.0.0 - Stable

✅ QR Code Reader version stable
🚀 Fonctionnalités complètes et testées  
📦 JAR autonome prêt pour distribution
🔧 Architecture Kotlin optimisée"

git checkout development
git tag -a "v2.0.0-dev" -m "🏷️ Development v2.0.0-dev

🔧 Branche de développement active
🚀 Nouvelles fonctionnalités en cours
📋 Roadmap d'améliorations prévu
🌟 Base pour futures versions"

git checkout main

Write-Host "  ✓ Tags créés: v1.0.0 (main) et v2.0.0-dev (development)" -ForegroundColor Green

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🎉 DÉPLOIEMENT GITHUB TERMINÉ!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Structure créée:" -ForegroundColor White
Write-Host "  📁 main (v1.0.0)      : Version stable" -ForegroundColor Green
Write-Host "  📁 development (v2.0-dev) : Version avancée" -ForegroundColor Yellow
Write-Host ""
Write-Host "Prochaines étapes:" -ForegroundColor White
Write-Host "  1. git remote add origin <your-github-repo>" -ForegroundColor Gray
Write-Host "  2. git push -u origin main" -ForegroundColor Gray  
Write-Host "  3. git push origin development" -ForegroundColor Gray
Write-Host "  4. git push origin --tags" -ForegroundColor Gray
Write-Host ""
Write-Host "Commandes utiles:" -ForegroundColor White
Write-Host "  git branch -a                    # Voir toutes les branches" -ForegroundColor Cyan
Write-Host "  git checkout main               # Basculer vers stable" -ForegroundColor Cyan
Write-Host "  git checkout development        # Basculer vers dev" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
