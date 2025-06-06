# QR Code Reader v1.0

A simple and efficient Kotlin application for reading QR codes from image files.

## 🚀 Features

- **Multiple Image Formats**: PNG, JPG, JPEG, BMP, GIF support
- **Command-line Interface**: Easy to use CLI with interactive mode
- **Content Type Detection**: Automatically detects URLs, emails, phone numbers, etc.
- **Error Handling**: Clear error messages and validation
- **Cross-platform**: Works on Windows, macOS, and Linux

## 📦 Quick Start

### Prerequisites
- Java 11 or higher
- Gradle (optional, wrapper included)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/qr-code-reader.git
   cd qr-code-reader
   ```

2. **Build the application:**
   ```bash
   ./gradlew build
   ```

3. **Run the application:**
   ```bash
   ./gradlew run
   ```

### Usage

#### Interactive Mode
```bash
java -jar qr-code-reader-1.0.0.jar
```
Then enter image file paths when prompted.

#### Command Line Mode
```bash
java -jar qr-code-reader-1.0.0.jar path/to/your/qr-image.png
```

#### Example Output
```
╔════════════════════════════════════╗
║         QR Code Reader v1.0        ║
║    Simple QR Code Detection Tool   ║
╚════════════════════════════════════╝

Processing: sample-qr.png
✅ QR Code detected successfully!
📄 Content: https://github.com/example/repo
⏱️  Processing time: 45ms
🔗 Type: URL
```

## 🛠️ Building from Source

### Build JAR file
```bash
./gradlew shadowJar
```
The executable JAR will be created in `build/libs/qr-code-reader-1.0.0.jar`

### Run tests
```bash
./gradlew test
```

### Development mode
```bash
./gradlew runApp
```

## 📁 Supported Formats

| Format | Extension | Status |
|--------|-----------|--------|
| PNG    | .png      | ✅ Full Support |
| JPEG   | .jpg, .jpeg | ✅ Full Support |
| BMP    | .bmp      | ✅ Full Support |
| GIF    | .gif      | ✅ Full Support |

## 🎯 Content Type Detection

The application automatically detects and displays the type of content in QR codes:

- 🌐 **URLs** - Web links (http/https)
- 📧 **Email** - Email addresses and mailto links
- 📞 **Phone** - Phone numbers and tel links
- 📶 **WiFi** - WiFi configuration
- 👤 **vCard** - Contact information
- 📝 **Text** - Plain text content

## 🔧 System Requirements

- **Java**: Version 11 or higher
- **Memory**: Minimum 256MB RAM
- **Storage**: 50MB for application and dependencies
- **OS**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+ or equivalent)

## 📖 API Reference

### QRCodeReader Class

```kotlin
class QRCodeReader {
    fun readQRCode(filePath: String): String
    fun readQRCode(image: BufferedImage): String
}
```

#### Methods

- **readQRCode(filePath: String)**: Reads QR code from file path
  - Returns: Decoded text content
  - Throws: IllegalArgumentException, RuntimeException

- **readQRCode(image: BufferedImage)**: Reads QR code from BufferedImage
  - Returns: Decoded text content  
  - Throws: IllegalArgumentException, RuntimeException

## 🧪 Testing

The project includes comprehensive tests:

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

## 🚀 Performance

- **Small QR codes** (< 100x100): ~10-30ms
- **Medium QR codes** (100x500): ~30-100ms  
- **Large QR codes** (> 500x500): ~100-300ms

*Performance may vary based on system specifications and image complexity.*

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 Changelog

### v1.0.0 (2024-01-XX)
- Initial release
- Basic QR code reading functionality
- Command-line interface
- Support for PNG, JPG, JPEG, BMP, GIF formats
- Interactive and batch processing modes
- Content type detection
- Cross-platform compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check this README and code comments
- **Issues**: Report bugs via GitHub Issues
- **Questions**: Use GitHub Discussions

## 🙏 Acknowledgments

- [ZXing](https://github.com/zxing/zxing) - The core QR code reading library
- [Kotlin](https://kotlinlang.org/) - Programming language
- [Gradle](https://gradle.org/) - Build system

---

Made with ❤️ by the QR Code Reader Team