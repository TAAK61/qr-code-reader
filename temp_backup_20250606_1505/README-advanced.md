# QR Code Reader

## Description
This project is a QR code reader application developed in Kotlin. It allows users to read QR codes from images and process them for enhanced detection and decoding.

## Project Structure
```
qr-code-reader
├── src
│   └── main
│       └── kotlin
│           ├── Main.kt
│           ├── qr
│           │   ├── QRCodeReader.kt
│           │   └── QRCodeProcessor.kt
│           └── utils
│               └── ImageUtils.kt
├── build.gradle.kts
├── gradle.properties
└── README.md
```

## Setup Instructions
1. Clone the repository:
   ```
   git clone <repository-url>
   ```
2. Navigate to the project directory:
   ```
   cd qr-code-reader
   ```
3. Open the project in your preferred IDE.

4. Build the project using Gradle:
   ```
   ./gradlew build
   ```

## Usage
1. Run the application:
   ```
   ./gradlew run
   ```
2. Follow the prompts to load an image containing a QR code.

3. The application will decode the QR code and display the result.

## Dependencies
This project uses the following libraries:
- ZXing (Zebra Crossing) for QR code reading.
- Java AWT for image processing.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License. See the LICENSE file for more details.