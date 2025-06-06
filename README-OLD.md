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
`ash
.\gradlew clean build
`

### Exécution
`ash
java -jar build\libs\qr-code-reader-1.0-all.jar <chemin-vers-image>
`

## 📖 Utilisation

`ash
# Lire un QR code depuis une image
java -jar qr-code-reader-1.0-all.jar mon-qr-code.png

# Exemple de sortie
QR Code Reader v1.0
QR Code trouvé: https://github.com/votre-repo
`

## 🏗️ Architecture

`
src/
├── main/kotlin/
│   ├── main/
│   │   └── Main.kt          # Point d'entrée
│   └── qr/
│       └── QRCodeReader.kt  # Logique principale
└── test/kotlin/
    └── QRCodeReaderTest.kt  # Tests unitaires
`

## 🧪 Tests

`ash
.\gradlew test
`

## 📋 Versions

- **v1.0** (main) : Version stable de base
- **v2.0-dev** (development) : Version avancée avec fonctionnalités étendues

## 📄 Licence

MIT License - voir le fichier LICENSE pour plus de détails.

## 🤝 Contribution

1. Fork le projet
2. Créez votre branche feature (git checkout -b feature/nouvelle-fonctionnalite)
3. Committez vos changements (git commit -m 'Ajout nouvelle fonctionnalité')
4. Push vers la branche (git push origin feature/nouvelle-fonctionnalite)
5. Ouvrez une Pull Request

---

*Développé avec ❤️ en Kotlin*
