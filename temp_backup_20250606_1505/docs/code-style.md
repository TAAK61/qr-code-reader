# Kotlin Code Style Configuration for QR Code Reader

## General Rules
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use UTF-8 encoding for all files
- End files with a newline character

## Naming Conventions
- **Classes**: PascalCase (e.g., `QRCodeReader`, `ImageProcessor`)
- **Functions**: camelCase (e.g., `readQRCode`, `processImage`)
- **Variables**: camelCase (e.g., `contrastFactor`, `imageWidth`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `MAX_IMAGE_SIZE`, `DEFAULT_TIMEOUT`)
- **Packages**: lowercase with dots (e.g., `qr.processors`, `utils.image`)

## File Organization
```kotlin
// 1. Package declaration
package com.example.qrreader

// 2. Imports (grouped and sorted)
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.max

// 3. Class/interface declaration
/**
 * KDoc comment for the class
 */
class MyClass {
    // 4. Companion object (if any)
    companion object {
        const val CONSTANT = "value"
    }
    
    // 5. Properties
    private val property = "value"
    
    // 6. Initialization blocks
    init {
        // initialization code
    }
    
    // 7. Functions
    fun publicFunction() {
        // implementation
    }
    
    private fun privateFunction() {
        // implementation
    }
}
```

## Documentation Standards
- Use KDoc for all public classes, functions, and properties
- Include `@param` and `@return` tags for functions
- Add `@throws` for exceptions
- Include usage examples with `@sample`

```kotlin
/**
 * Reads and decodes a QR code from an image file.
 * 
 * This function loads the specified image, applies preprocessing to enhance
 * detection accuracy, and attempts to decode any QR codes found.
 * 
 * @param filePath The path to the image file containing the QR code
 * @param enhanceContrast Whether to apply contrast enhancement (default: true)
 * @return The decoded QR code result with content and metadata
 * @throws QRCodeNotFoundException if no QR code is detected in the image
 * @throws InvalidImageException if the file is not a valid image
 * 
 * @sample
 * ```kotlin
 * val reader = QRCodeReader()
 * val result = reader.readQRCode("/path/to/qr-image.png")
 * println("QR Content: ${result.content}")
 * ```
 */
fun readQRCode(filePath: String, enhanceContrast: Boolean = true): QRCodeResult
```

## Error Handling
- Use custom exceptions that extend a base exception class
- Always provide meaningful error messages
- Include context information in exceptions

```kotlin
// Good
throw QRCodeNotFoundException(
    "No QR code detected in image: $fileName",
    "Try enhancing image quality or ensuring QR code is clearly visible"
)

// Avoid
throw Exception("Error")
```

## Function Design
- Keep functions focused on a single responsibility
- Limit function parameters (max 5-6 parameters)
- Use data classes for complex parameter groups
- Prefer immutable parameters

```kotlin
// Good
data class ImageProcessingOptions(
    val enhanceContrast: Boolean = true,
    val contrastFactor: Double = 1.5,
    val reduceNoise: Boolean = true,
    val resizeIfLarge: Boolean = true
)

fun processImage(image: BufferedImage, options: ImageProcessingOptions): BufferedImage

// Avoid
fun processImage(
    image: BufferedImage,
    enhanceContrast: Boolean,
    contrastFactor: Double,
    reduceNoise: Boolean,
    resizeIfLarge: Boolean,
    maxWidth: Int,
    maxHeight: Int
): BufferedImage
```

## Variable Declarations
- Use `val` for immutable variables, `var` only when necessary
- Initialize variables at declaration when possible
- Use meaningful variable names

```kotlin
// Good
val processedImage = enhanceContrast(originalImage, 1.5)
val qrCodeResults = mutableListOf<QRCodeResult>()

// Avoid
var img = enhance(orig, 1.5)
val list = mutableListOf<QRCodeResult>()
```

## Control Flow
- Use early returns to reduce nesting
- Prefer `when` over `if-else` chains for multiple conditions
- Use safe calls (`?.`) and Elvis operator (`?:`) for null safety

```kotlin
// Good
fun validateImage(image: BufferedImage?): Boolean {
    if (image == null) return false
    if (image.width <= 0 || image.height <= 0) return false
    
    return when (image.type) {
        BufferedImage.TYPE_INT_RGB,
        BufferedImage.TYPE_INT_ARGB,
        BufferedImage.TYPE_BYTE_GRAY -> true
        else -> false
    }
}

// Use safe calls
val fileName = file?.name ?: "unknown"
```

## String Handling
- Use string templates instead of concatenation
- Prefer `StringBuilder` for complex string building
- Use raw strings for multi-line strings

```kotlin
// Good
val message = "Processing image: $fileName (${image.width}x${image.height})"

val helpText = """
    QR Code Reader Usage:
    - Drag and drop image files
    - Press 'Open' to select files
    - Use 'Settings' to configure processing
""".trimIndent()

// Avoid
val message = "Processing image: " + fileName + " (" + image.width + "x" + image.height + ")"
```

## Collection Handling
- Use appropriate collection types (List, Set, Map)
- Prefer immutable collections when possible
- Use collection operations (map, filter, etc.) over loops

```kotlin
// Good
val supportedExtensions = setOf("png", "jpg", "jpeg", "bmp", "gif")
val validFiles = files.filter { it.extension.lowercase() in supportedExtensions }
val fileNames = validFiles.map { it.name }

// Avoid
val supportedExtensions = mutableListOf("png", "jpg", "jpeg", "bmp", "gif")
val validFiles = mutableListOf<File>()
for (file in files) {
    if (supportedExtensions.contains(file.extension.lowercase())) {
        validFiles.add(file)
    }
}
```

## Resource Management
- Use `use` function for automatic resource cleanup
- Close resources in finally blocks if `use` is not applicable

```kotlin
// Good
fun loadProperties(file: File): Properties {
    return file.inputStream().use { stream ->
        Properties().apply { load(stream) }
    }
}

// For multiple resources
fun copyImage(source: File, destination: File) {
    source.inputStream().use { input ->
        destination.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}
```

## Testing Standards
- Use descriptive test names that explain what is being tested
- Follow Given-When-Then or Arrange-Act-Assert patterns
- Use nested test classes to group related tests
- Test both success and failure scenarios

```kotlin
@Test
@DisplayName("Should successfully read QR code from valid PNG image")
fun shouldReadQRCodeFromValidPngImage() {
    // Given
    val testImage = createTestQRCodeImage("Hello World")
    val reader = QRCodeReader()
    
    // When
    val result = reader.readQRCode(testImage)
    
    // Then
    assertEquals("Hello World", result.content)
    assertEquals("QR_CODE", result.format)
    assertTrue(result.confidence > 0.8)
}
```

## Performance Guidelines
- Avoid creating unnecessary objects in loops
- Use lazy initialization for expensive resources
- Consider using coroutines for asynchronous operations
- Profile code to identify bottlenecks

```kotlin
// Good - lazy initialization
class ImageProcessor {
    private val expensiveResource by lazy {
        createExpensiveResource()
    }
    
    fun processImage(image: BufferedImage): BufferedImage {
        return expensiveResource.process(image)
    }
}

// Good - avoid object creation in loops
fun processPixels(image: BufferedImage): BufferedImage {
    val result = BufferedImage(image.width, image.height, image.type)
    val color = Color(0, 0, 0) // Reuse color object
    
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            // Process pixel without creating new Color objects
            val rgb = image.getRGB(x, y)
            val processed = processPixelValue(rgb)
            result.setRGB(x, y, processed)
        }
    }
    
    return result
}
```