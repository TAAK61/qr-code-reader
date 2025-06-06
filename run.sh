#!/bin/bash

echo "🚀 QR Code Reader - Lancement de l'application"
echo "============================================"
echo ""

echo "📋 Vérification de l'environnement..."

# Vérifier Java
if ! command -v java &> /dev/null; then
    echo "❌ Erreur: Java n'est pas installé ou n'est pas dans le PATH"
    echo "💡 Veuillez installer Java 11 ou supérieur"
    exit 1
fi

echo "✅ Java détecté"
java -version

echo ""
echo "📦 Construction du projet..."
./gradlew build

if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la construction du projet"
    exit 1
fi

echo "✅ Construction réussie"
echo ""
echo "🎯 Démarrage de l'application QR Code Reader..."
echo ""

./gradlew run