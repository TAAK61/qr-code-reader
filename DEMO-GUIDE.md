# QR Code Reader - Demo Guide

## 🎯 Quick Start

### Mode GUI (Recommandé)
```bash
java -jar build/libs/qr-code-reader-2.0-dev-all.jar
```

### Mode CLI
```bash
java -jar build/libs/qr-code-reader-2.0-dev-all.jar --cli test-images/qr-hello-world.png
```

## 🖼️ Images de Test Disponibles

Le projet inclut 20 images QR de test dans le dossier `test-images/` :

### Images Simples
- `qr-hello-world.png` - Message simple "Hello World"
- `qr-numbers.png` - Séquence numérique
- `qr-alphanumeric.png` - Texte alphanumérique

### Contenu Structuré
- `qr-vcard-test.png` - Carte de visite (vCard)
- `qr-wifi-test.png` - Configuration WiFi
- `qr-email.png` - Adresse email
- `qr-phone.png` - Numéro de téléphone
- `qr-sms.png` - Message SMS

### URLs et Liens
- `qr-kotlin-docs.png` - Lien vers documentation Kotlin
- `qr-github-kotlin.png` - Lien GitHub
- `qr-long-url.png` - URL longue

### Contenu Avancé
- `qr-json.png` - Données JSON
- `qr-calendar.png` - Événement calendrier
- `qr-location.png` - Coordonnées GPS
- `qr-unicode.png` - Caractères Unicode
- `qr-special-chars.png` - Caractères spéciaux

### Contenu Volumineux
- `qr-large-content.png` - Texte long
- `qr-long-text.png` - Paragraphe complet
- `qr-multiline.png` - Texte multi-lignes

## 🎮 Fonctionnalités GUI

### Scanner Tab
1. **Drag & Drop** : Glissez-déposez une image QR dans la zone
2. **Bouton Browse** : Cliquez pour sélectionner un fichier
3. **Preview** : Visualisez l'image avant traitement
4. **Results** : Consultez le contenu décodé
5. **Copy** : Copiez le résultat dans le presse-papiers

### Batch Processing Tab
1. **Add Files** : Ajoutez plusieurs images à traiter
2. **Process All** : Traitez toutes les images en lot
3. **Progress** : Suivez l'avancement en temps réel
4. **Results List** : Consultez tous les résultats

### History Tab
1. **Table View** : Historique de tous les QR codes traités
2. **Export CSV** : Exportez l'historique au format CSV
3. **Clear History** : Effacez l'historique

### Raccourcis Clavier
- `Ctrl+O` : Ouvrir un fichier
- `Ctrl+B` : Basculer vers traitement par lot
- `Ctrl+H` : Afficher l'historique
- `Ctrl+Q` : Quitter l'application
- `F1` : Aide

## 🧪 Tests de Démonstration

### Test 1: QR Code Simple
```bash
java -jar build/libs/qr-code-reader-2.0-dev-all.jar --cli test-images/qr-hello-world.png
```

### Test 2: QR Code vCard
```bash
java -jar build/libs/qr-code-reader-2.0-dev-all.jar --cli test-images/qr-vcard-test.png
```

### Test 3: QR Code WiFi
```bash
java -jar build/libs/qr-code-reader-2.0-dev-all.jar --cli test-images/qr-wifi-test.png
```

### Test 4: QR Code JSON
```bash
java -jar build/libs/qr-code-reader-2.0-dev-all.jar --cli test-images/qr-json.png
```

## 🔧 Mode Batch (GUI)

1. Lancez l'application en mode GUI
2. Allez dans l'onglet "Batch Processing"
3. Cliquez "Add Files" et sélectionnez plusieurs images de test
4. Cliquez "Process All" pour traiter toutes les images
5. Consultez les résultats dans la liste

## 📊 Historique et Export

1. Traitez quelques images QR
2. Allez dans l'onglet "History"
3. Consultez l'historique complet
4. Cliquez "Export CSV" pour sauvegarder les résultats

## 🎨 Interface Moderne

L'application utilise JavaFX avec un style moderne incluant :
- Thème professionnel avec couleurs bootstrap
- Effets d'ombre et hover sur les boutons
- Indicateurs de progression animés
- Feedback visuel pour le drag & drop
- Dialogs d'erreur user-friendly

## 🚀 Prochaines Étapes

Les fonctionnalités suivantes sont prêtes pour développement :
- Support webcam (Task 47)
- Lecture depuis presse-papiers (Task 45)
- Internationalisation (Task 49)
- Support formats additionnels (Tasks 71-80)
