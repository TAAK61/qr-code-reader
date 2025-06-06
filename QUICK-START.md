# 🚀 Quick Start Guide - QR Code Reader

## ⚡ Démarrage Rapide

### 1. Validation Complète du Projet
```cmd
validate.bat
```
Ce script vérifie que tout est correctement configuré.

### 2. Lancer l'Application
```cmd
run.bat
```
Ou directement :
```cmd
gradlew run
```

### 3. Construire le JAR Exécutable
```cmd
gradlew shadowJar
```
Puis exécuter :
```cmd
java -jar build\libs\qr-code-reader-2.0.0-dev.jar
```

## 🔧 Commandes Disponibles

| Commande | Description |
|----------|-------------|
| `gradlew build` | Construire le projet |
| `gradlew run` | Exécuter l'application |
| `gradlew test` | Lancer les tests |
| `gradlew shadowJar` | Créer un JAR avec dépendances |
| `gradlew clean` | Nettoyer le projet |
| `gradlew jacocoTestReport` | Rapport de couverture de tests |

## 📁 Structure du Projet

```
qr-code-reader/
├── 📄 build.gradle.kts          # Configuration Gradle
├── 📄 gradle.properties         # Propriétés Gradle
├── 📄 settings.gradle.kts       # Configuration du projet
├── 📄 .editorconfig            # Configuration de l'éditeur
├── 📄 .gitignore               # Fichiers à ignorer
├── 📄 run.bat                  # Script de lancement
├── 📄 validate.bat             # Script de validation
├── 📁 src/main/kotlin/          # Code source principal
├── 📁 src/test/kotlin/          # Tests
├── 📁 test-images/             # Images de test
├── 📁 gradle/wrapper/          # Gradle Wrapper
└── 📁 docs/                    # Documentation
```

## ✅ Améliorations Apportées

### Configuration Gradle
- ✅ **Gradle Wrapper** mis à jour (v8.4)
- ✅ **Shadow Plugin** pour JAR avec dépendances
- ✅ **JaCoCo** pour couverture de code
- ✅ **Configuration optimisée** des tâches

### Qualité du Code
- ✅ **EditorConfig** pour formatage uniforme
- ✅ **GitIgnore** complet et optimisé
- ✅ **Configuration IDE** standardisée

### Scripts et Validation
- ✅ **Script de validation** complet (10 vérifications)
- ✅ **Script de lancement** amélioré
- ✅ **Gestion d'erreurs** robuste

### Documentation
- ✅ **Guide de démarrage** rapide
- ✅ **Documentation des tests**
- ✅ **Instructions détaillées**

## 🎯 Prochaines Étapes

1. **Exécuter la validation** : `validate.bat`
2. **Tester l'application** : `run.bat`
3. **Ajouter des images de test** dans `test-images/`
4. **Développer de nouvelles fonctionnalités**
5. **Créer des tests unitaires** supplémentaires

## 🐛 Dépannage

### Problèmes Courants

#### Java non trouvé
```
❌ Java n'est pas installé ou n'est pas dans le PATH
```
**Solution :** Installer Java 11+ et vérifier le PATH

#### Échec de compilation
```
❌ Échec de la compilation Kotlin
```
**Solutions :**
1. Vérifier la syntaxe Kotlin
2. Exécuter `gradlew clean build`
3. Vérifier les dépendances

#### Permission refusée
```
❌ Permission denied
```
**Solution :** Exécuter en tant qu'administrateur ou vérifier les permissions

---

**Votre projet QR Code Reader est maintenant robuste et stable !** 🎉