# QR Code Reader - Modern GUI Edition

Un lecteur de QR Code moderne avec interface graphique et ligne de commande, développé en Kotlin avec JavaFX.

## 🚀 Fonctionnalités

### Interface Graphique (GUI) - ✨ Nouveau !
- **Interface moderne et intuitive** avec JavaFX
- **Glisser-déposer** d'images directement dans l'application
- **Aperçu d'image** avec zoom et défilement
- **Traitement par lots** pour plusieurs images
- **Historique des lectures** avec export CSV
- **Raccourcis clavier** (Ctrl+O, Ctrl+B, Ctrl+H, Ctrl+Q, F1)
- **Rapports d'erreur conviviaux** avec dialogues informatifs

### Interface Ligne de Commande (CLI)
- Lecture de QR codes à partir d'images (PNG, JPG, JPEG, GIF, BMP)
- Interface en ligne de commande simple et efficace
- Détection automatique du mode basée sur les arguments

### Architecture
- Architecture modulaire et extensible
- Tests unitaires complets
- JAR exécutable autonome
- Support dual-mode (GUI/CLI)

## 📦 Installation

### Prérequis
- Java 11 ou supérieur
- Gradle (optionnel, le wrapper est inclus)

### Compilation
```bash
.\gradlew clean build
```

## 📖 Utilisation

### Mode GUI (par défaut)
```bash
# Lancer l'interface graphique
java -jar build\libs\qr-code-reader-2.0-dev-all.jar
```

**Fonctionnalités GUI disponibles :**
- **Onglet Scanner** : Glissez-déposez ou sélectionnez une image, visualisez l'aperçu et les résultats
- **Onglet Traitement par lots** : Ajoutez plusieurs images et traitez-les en une seule fois
- **Onglet Historique** : Consultez l'historique des lectures et exportez en CSV
- **Raccourcis clavier** :
  - `Ctrl+O` : Ouvrir une image
  - `Ctrl+B` : Mode traitement par lots
  - `Ctrl+H` : Afficher l'historique
  - `Ctrl+Q` : Quitter
  - `F1` : Aide

### Mode CLI
```bash
# Lire un QR code depuis une image (mode CLI automatique)
java -jar build\libs\qr-code-reader-2.0-dev-all.jar image.png

# Forcer le mode CLI
java -jar build\libs\qr-code-reader-2.0-dev-all.jar --cli image.png

# Afficher l'aide
java -jar build\libs\qr-code-reader-2.0-dev-all.jar --help
```

**Exemple de sortie CLI :**
```
QR Code Reader v2.0-dev (Modern GUI Edition)
=============================================
📄 Processing: test-image.png
✅ QR Code successfully decoded!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Content:
Hello from QR Code Reader!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## 🏗️ Architecture

```
src/
├── main/kotlin/
│   ├── main/
│   │   └── Main.kt              # Point d'entrée dual-mode
│   ├── qr/
│   │   └── QRCodeReader.kt      # Logique principale
│   └── ui/
│       └── QRCodeReaderGUI.kt   # Interface graphique JavaFX
├── main/resources/
│   └── styles/
│       └── modern-style.css     # Styles modernes
└── test/kotlin/
    └── QRCodeReaderTest.kt      # Tests unitaires
```

## 🧪 Tests

```bash
.\gradlew test
```

## 📋 Versions

- **v1.0** (main) : Version stable de base avec CLI
- **v2.0-dev** (development) : Version moderne avec GUI + CLI avancé

## 🎯 Tâches Implémentées

Cette version implémente plusieurs tâches de la liste des améliorations :

### Expérience Utilisateur (Tasks 41-50)
- ✅ **Task 41** : Interface graphique (GUI)
- ✅ **Task 42** : Support glisser-déposer
- ✅ **Task 43** : Aperçu d'image avant détection
- ✅ **Task 44** : Fonctionnalité d'historique
- ✅ **Task 46** : Mode traitement par lots
- ✅ **Task 48** : Rapports d'erreur conviviaux
- ✅ **Task 50** : Raccourcis clavier

### À Venir
- 🔄 **Task 45** : Support presse-papiers
- 🔄 **Task 47** : Support webcam
- 🔄 **Task 49** : Internationalisation

## 📄 Licence

MIT License - voir le fichier LICENSE pour plus de détails.

## 🤝 Contribution

1. Fork le projet
2. Créez votre branche feature (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Committez vos changements (`git commit -m 'Ajout nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Ouvrez une Pull Request

## 🚀 Démarrage Rapide

```bash
# Cloner le repository
git clone <repository-url>
cd qr-code-reader

# Compiler l'application
.\gradlew build

# Lancer en mode GUI
java -jar build\libs\qr-code-reader-2.0-dev-all.jar

# Ou tester avec l'image d'exemple
java -jar build\libs\qr-code-reader-2.0-dev-all.jar --cli test-images\qr-test-hello.png
```

---

*Développé avec ❤️ en Kotlin + JavaFX*
