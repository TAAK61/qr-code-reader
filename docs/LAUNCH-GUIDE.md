# 🚀 Guide de Lancement - QR Code Reader

## 📋 Méthodes pour Lancer l'Application

### 🎯 **Méthode 1: Scripts de Lancement (Recommandé)**

#### Windows
```cmd
# Double-cliquer sur le fichier ou exécuter dans le terminal
run.bat
```

#### Linux/macOS
```bash
# Rendre le script exécutable puis le lancer
chmod +x run.sh
./run.sh
```

### 🛠️ **Méthode 2: Commandes Gradle Directes**

#### Construction et Exécution
```bash
# 1. Construire le projet
./gradlew build

# 2. Exécuter l'application
./gradlew run
```

#### Exécution Alternative
```bash
# Construire un JAR exécutable
./gradlew shadowJar

# Exécuter le JAR
java -jar build/libs/qr-code-reader-2.0.0-dev.jar
```

### ⚡ **Méthode 3: Lancement Rapide**

#### Depuis VS Code
1. Ouvrir le terminal intégré (`Ctrl+`` `)
2. Exécuter: `./gradlew run`

#### Depuis IntelliJ IDEA
1. Ouvrir le projet
2. Naviguer vers `Main.kt`
3. Cliquer sur le bouton ▶️ vert à côté de `fun main()`

## 🖥️ **Interface de l'Application**

### Mode Interactif
Quand vous lancez l'application, vous verrez :

```
🚀 QR Code Reader v2.0
======================

Interactive Mode
Supported formats: PNG, JPG, JPEG, BMP, GIF
Type 'help' for commands, 'quit' to exit

Enter image file path: _
```

### Commandes Disponibles
- **Chemin d'image** : Traiter une image QR
- **`help`** ou **`h`** : Afficher l'aide
- **`quit`** ou **`q`** : Quitter l'application

### Exemples d'Utilisation

#### Traitement d'une Image
```
Enter image file path: C:\Images\mon_qr.png

Processing: C:\Images\mon_qr.png
✅ QR Code detected successfully!
📄 Content: https://github.com/example/repo
⏱️  Processing time: 45ms
🔗 Type: URL
```

#### Gestion d'Erreurs
```
Enter image file path: fichier_inexistant.png
❌ Error: File not found
💡 Tip: Ensure the QR code is clearly visible and not damaged
```

## 🔧 **Dépannage**

### Problèmes Courants

#### 1. **Java non trouvé**
```
❌ Erreur: Java n'est pas installé
```
**Solution :** Installer Java 11+ et l'ajouter au PATH

#### 2. **Échec de construction**
```
❌ Erreur lors de la construction du projet
```
**Solutions :**
- Vérifier la connexion Internet (pour télécharger les dépendances)
- Exécuter `./gradlew clean build`
- Vérifier que le fichier `build.gradle.kts` n'est pas corrompu

#### 3. **Permission refusée (Linux/macOS)**
```
Permission denied: ./gradlew
```
**Solution :** `chmod +x gradlew`

### Logs et Débogage

#### Activer les Logs Détaillés
```bash
./gradlew run --debug
```

#### Vérifier la Configuration
```bash
./gradlew properties
```

## 📊 **Exemple Complet de Session**

```bash
$ ./gradlew run

🚀 QR Code Reader v2.0
======================

Interactive Mode
Supported formats: PNG, JPG, JPEG, BMP, GIF
Type 'help' for commands, 'quit' to exit

Enter image file path: test-images/qr-url.png

Processing: test-images/qr-url.png
✅ QR Code detected successfully!
📄 Content: https://kotlin.org
⏱️  Processing time: 23ms
🔗 Type: URL

Enter image file path: test-images/qr-text.png

Processing: test-images/qr-text.png
✅ QR Code detected successfully!
📄 Content: Hello World from QR Code!
⏱️  Processing time: 18ms
📝 Type: Text

Enter image file path: quit
Thank you for using QR Code Reader!
```

## 🎯 **Prochaines Étapes**

Une fois l'application lancée avec succès :

1. **Tester avec vos images QR** - Préparez quelques images avec des QR codes
2. **Explorer les fonctionnalités** - Essayez différents types de contenu
3. **Consulter la documentation** - Voir `docs/` pour plus d'informations
4. **Contribuer** - Proposer des améliorations via GitHub

---

**Votre application QR Code Reader est prête à l'emploi !** 🎉