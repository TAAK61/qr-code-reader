# QR Code Reader Project Guidelines

This document provides essential information for developers working on the QR Code Reader project.

## Build/Configuration Instructions

### Prerequisites
- JDK 11 or higher
- Gradle (or use the Gradle wrapper after fixing it)

### Setting Up the Project

1. **Fix the Gradle Wrapper (if using)**
   ```bash
   # Run this command in the project root to generate the Gradle wrapper files
   gradle wrapper --gradle-version 7.4
   ```

2. **Build the Project**
   ```bash
   # Using Gradle directly
   gradle build
   
   # Or using the Gradle wrapper (after fixing)
   ./gradlew build
   ```

3. **Run the Application**
   ```bash
   # Using Gradle
   gradle run
   
   # Or using the Gradle wrapper
   ./gradlew run
   ```

4. **Create a Distributable JAR**
   ```bash
   # Creates a fat JAR with all dependencies
   gradle jar
   ```
   The JAR will be created in `build/libs/` directory.

## Testing Information

### Running Tests

The project uses JUnit 5 for testing. Tests can be run using Gradle:

```bash
# Run all tests
gradle test

# Run a specific test class
gradle test --tests "utils.ImageUtilsTest"

# Run a specific test method
gradle test --tests "utils.ImageUtilsTest.testConvertToGrayscale"
```

### Adding New Tests

1. Create test classes in the `src/test/kotlin` directory, mirroring the package structure of the main code.
2. Use JUnit 5 annotations (`@Test`, `@BeforeEach`, etc.) to define test methods.
3. Follow the naming convention: `ClassNameTest` for test classes and `testMethodName` for test methods.

### Example Test

Here's an example test for the `ImageUtils` class:

```kotlin
package utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.awt.Color
import java.awt.image.BufferedImage

class ImageUtilsTest {
    
    @Test
    fun testConvertToGrayscale() {
        // Create a test image with colored pixels
        val width = 10
        val height = 10
        val testImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = testImage.createGraphics()
        
        // Fill with red color
        graphics.color = Color.RED
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()
        
        // Convert to grayscale
        val grayscaleImage = ImageUtils.convertToGrayscale(testImage)
        
        // Check that the image type is grayscale
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, grayscaleImage.type)
        
        // Check dimensions are preserved
        assertEquals(width, grayscaleImage.width)
        assertEquals(height, grayscaleImage.height)
        
        // Sample a pixel to verify it's gray (R=G=B)
        val rgb = grayscaleImage.getRGB(5, 5)
        val color = Color(rgb)
        
        // In grayscale, all color components should be approximately equal
        assertEquals(color.red, color.green)
        assertEquals(color.green, color.blue)
    }
}
```

## Additional Development Information

### Project Structure

- `src/main/kotlin/` - Main source code
  - `Main.kt` - Application entry point
  - `qr/` - QR code processing classes
    - `QRCodeReader.kt` - Core functionality for reading QR codes
    - `QRCodeProcessor.kt` - Image processing for QR code detection
  - `utils/` - Utility classes
    - `ImageUtils.kt` - Image manipulation utilities

### Dependencies

The project uses the following main dependencies:
- ZXing (3.5.2) - For QR code detection and processing
- Kotlin Standard Library - For Kotlin language features

### Code Style Guidelines

1. **Kotlin Conventions**
   - Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
   - Use 4 spaces for indentation
   - Maximum line length: 100 characters

2. **Documentation**
   - Document all public classes and methods with KDoc comments
   - Include examples for complex functionality

3. **Error Handling**
   - Use exceptions for exceptional conditions
   - Provide meaningful error messages
   - Log errors appropriately

### Debugging Tips

1. **QR Code Detection Issues**
   - If QR codes aren't being detected properly, try:
     - Increasing image contrast
     - Ensuring adequate lighting in the image
     - Using higher resolution images
     - Checking that the QR code has adequate quiet zones (margins)

2. **Performance Optimization**
   - For large images, consider resizing before processing
   - The `QRCodeProcessor.resizeImage()` method can be used to reduce image dimensions while maintaining aspect ratio

### Common Issues and Solutions

1. **"No QR code found in the image"**
   - Ensure the image is clear and well-lit
   - Try preprocessing the image with `QRCodeProcessor.processImageForQR()`
   - Verify the QR code is valid and properly formatted

2. **OutOfMemoryError when processing large images**
   - Use the `QRCodeProcessor.resizeImage()` method to reduce image dimensions
   - Increase JVM heap size with `-Xmx` flag if necessary
