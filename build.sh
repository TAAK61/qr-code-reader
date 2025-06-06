#!/bin/bash
# Simple build script for QR Code Reader
echo "🔨 Building QR Code Reader..."
./gradlew clean build shadowJar
echo "✅ Build complete! JAR file: build/libs/qr-code-reader-2.0-dev-all.jar"
