# QR Code Reader

[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)](https://gradle.org/)

## Description
This project is a QR code reader application developed in Kotlin. It allows users to read QR codes from images and process them for enhanced detection and decoding.

## Features
- ✅ Read QR codes from image files
- ✅ Process and enhance image quality for better detection
- ✅ Support for multiple image formats
- ✅ Graphical user interface
- ✅ Image preprocessing and visualization
- ✅ Error handling and validation

## Project Structure
```
qr-code-reader/
├── src/
│   └── main/
│       └── kotlin/
│           ├── Main.kt                 # Application entry point
│           ├── qr/
│           │   ├── QRCodeReader.kt     # Core QR reading functionality
│           │   └── QRCodeProcessor.kt  # Image processing utilities
│           ├── ui/
│           │   └── QRCodeReaderUI.kt   # Graphical user interface
│           └── utils/
│               └── ImageUtils.kt       # Image manipulation helpers
├── build.gradle.kts                    # Gradle build configuration
├── gradle.properties                   # Gradle properties
├── .gitignore                         # Git ignore rules
├── LICENSE                            # MIT License
└── README.md                          # Project documentation
```

## Prerequisites
- Java 11 or higher (JDK, not just JRE)
- No need to install Gradle separately as the project includes a Gradle wrapper

## Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/qr-code-reader.git
   ```
2. Navigate to the project directory:
   ```bash
   cd qr-code-reader
   ```
3. Build the project using Gradle:
   ```bash
   # On Windows
   .\gradlew build

   # On Linux/macOS
   ./gradlew build
   ```

## Usage

### Running with Gradle
1. Run the application:
   ```bash
   # On Windows
   .\gradlew run

   # On Linux/macOS
   ./gradlew run
   ```
2. The graphical user interface will open.
3. Use the interface to:
   - Click "Ouvrir une image" to select an image file containing a QR code
   - Click "Prétraiter l'image" to enhance the image for better QR code detection
   - Click "Scanner le QR code" to read the QR code from the image
   - Click "Sauvegarder l'image traitée" to save the processed image

### Running with JAR file
1. Create the JAR file:
   ```bash
   # On Windows
   .\gradlew jar

   # On Linux/macOS
   ./gradlew jar
   ```
2. Run the JAR file:
   ```bash
   # On Windows
   java -jar build\libs\qr-code-reader-1.0.0.jar

   # On Linux/macOS
   java -jar build/libs/qr-code-reader-1.0.0.jar
   ```
3. The graphical user interface will open.
4. Use the interface as described above to load, process, and scan QR codes.

## Dependencies
This project uses the following libraries:
- **ZXing (Zebra Crossing)** - For QR code reading and decoding
- **Java AWT** - For image processing and manipulation
- **Swing** - For the graphical user interface

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Troubleshooting

If you encounter issues when running the application:

1. **Java version issues**:
   - Ensure you have JDK 11 or higher installed
   - Verify your Java installation with `java -version`

2. **Image reading problems**:
   - Make sure the image file exists at the specified path
   - Check that the image format is supported (JPG, PNG, BMP, etc.)
   - Ensure the image contains a valid QR code
   - Try using a clearer image if the QR code is not being detected

3. **UI display issues**:
   - If the UI appears distorted, try resizing the window
   - If images don't display correctly, check that the image format is supported
   - On some systems, the Look and Feel might not apply correctly; the application will still function

4. **Gradle wrapper issues**:
   - If the Gradle wrapper doesn't work, you can regenerate it with:
     ```bash
     gradle wrapper --gradle-version 7.4
     ```
   - Ensure you have the necessary permissions to execute the wrapper scripts

5. **JAR execution problems**:
   - Verify the JAR was created successfully in the `build/libs` directory
   - Make sure you're using the correct path to the JAR file

## Issues
If you encounter any issues or have suggestions, please [open an issue](https://github.com/YOUR_USERNAME/qr-code-reader/issues).

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Acknowledgments
- Thanks to the ZXing team for their excellent QR code library
- Inspired by the need for simple, efficient QR code processing tools

---
⭐ Don't forget to star this repo if you found it helpful!
