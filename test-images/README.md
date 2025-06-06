# Test Images for QR Code Reader

This directory contains sample QR code images for testing the application.

## Available Test Images

### URL QR Codes
- `qr-github.png` - GitHub repository URL
- `qr-kotlin.png` - Kotlin official website
- `qr-google.png` - Google search

### Text QR Codes
- `qr-hello-world.png` - Simple "Hello World" text
- `qr-lorem.png` - Lorem ipsum text

### Contact QR Codes
- `qr-vcard.png` - Contact information in vCard format

### WiFi QR Codes
- `qr-wifi.png` - WiFi network configuration

## Usage

Use these images to test the QR Code Reader application:

```bash
# Interactive mode
gradlew run
# Then enter: test-images/qr-github.png

# Direct mode
java -jar build/libs/qr-code-reader-2.0.0-dev.jar test-images/qr-hello-world.png
```

## Generating New Test Images

You can generate additional QR codes using online tools:
- [QR Code Generator](https://www.qr-code-generator.com/)
- [QRCode Monkey](https://www.qrcode-monkey.com/)

Place the generated images in this directory for testing.