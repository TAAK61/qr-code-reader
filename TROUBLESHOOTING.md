# Guide de Dépannage - QR Code Reader

## Problème d'Encodage des Scripts

Si vous rencontrez des erreurs d'encodage avec les scripts batch (caractères bizarres), utilisez les versions fixes :

### Scripts Corrigés
- `validate-fixed.bat` - Version corrigée du script de validation
- `run-fixed.bat` - Version corrigée du script d'exécution

## Problème Gradle Version Catalogs

**Erreur** : `There is no feature named VERSION_CATALOGS`

Cette erreur se produit avec des versions anciennes de Gradle qui ne supportent pas les Version Catalogs.

### Solution Automatique
```cmd
# Exécuter le script de migration
migrate-gradle.bat
```

### Solution Manuelle
1. Remplacer `settings.gradle.kts` par le contenu de `settings-fixed.gradle.kts`
2. Remplacer `build.gradle.kts` par le contenu de `build-fixed.gradle.kts`

### Utilisation
```cmd
# Au lieu de validate.bat, utilisez :
validate-fixed.bat

# Au lieu de run.bat, utilisez :
run-fixed.bat

# Pour migrer automatiquement :
migrate-gradle.bat
```

## Problèmes Courants

### 1. Java non trouvé
**Erreur** : `Java n'est pas installe ou n'est pas dans le PATH`

**Solution** :
- Installez Java 11 ou supérieur
- Ajoutez Java au PATH système
- Redémarrez votre terminal

### 2. Gradle Wrapper ne fonctionne pas
**Erreur** : `Probleme avec Gradle Wrapper`

**Solution** :
```cmd
# Régénérer le wrapper
gradle wrapper --gradle-version 8.4
```

### 3. Compilation échoue
**Erreur** : `Echec de la compilation Kotlin`

**Solution** :
- Vérifiez que tous les fichiers source sont présents
- Exécutez : `gradlew clean build --info`
- Vérifiez les dépendances

### 4. Tests échouent
**Erreur** : `Certains tests ont echoue`

**Solution** :
```cmd
# Voir les détails des tests
gradlew test --info

# Générer le rapport HTML
gradlew test jacocoTestReport
```

## Commandes de Diagnostic

### Vérification complète
```cmd
validate-fixed.bat
```

### Nettoyage et reconstruction
```cmd
gradlew clean build
```

### Informations système
```cmd
java -version
gradlew --version
```

### Logs détaillés
```cmd
gradlew build --info --stacktrace
```

## Structure de Fichiers Requise

```
qr-code-reader/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew.bat
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── Main.kt
│   │   └── resources/
│   └── test/
│       └── kotlin/
├── validate-fixed.bat
└── run-fixed.bat
```

## Contact Support

Si les problèmes persistent :
1. Exécutez `validate-fixed.bat`
2. Copiez les messages d'erreur
3. Vérifiez que tous les fichiers requis sont présents
4. Consultez les logs dans `build/reports/`