plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
    // jacoco // Temporarily disabled to avoid Java version compatibility issues
}

group = "com.qrcoder"
version = "2.0.0-dev"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// JavaFX configuration
javafx {
    version = "17.0.2"
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    
    // JavaFX dependencies for GUI (managed by plugin above)
    implementation("org.openjfx:javafx-controls:17.0.2")
    implementation("org.openjfx:javafx-fxml:17.0.2")
    implementation("org.openjfx:javafx-swing:17.0.2")
      // Webcam support
    implementation("com.github.sarxos:webcam-capture:0.3.12")
    
    // JSON processing for custom data formats (Task 79)
    implementation("com.google.code.gson:gson:2.10.1")
    
      // Testing dependencies - JUnit 5 for parameterized tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    // Mockito for mocking dependencies (Task 18)
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    
    // Keep JUnit 4 for backward compatibility
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.9.2")
}

application {
    mainClass.set("main.MainKt")
    
    // JavaFX runtime arguments
    applicationDefaultJvmArgs = listOf(
        "--add-exports", "javafx.base/com.sun.javafx.runtime=ALL-UNNAMED"
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

// Configure test task to use JUnit 5
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.shadowJar {
    archiveBaseName.set("qr-code-reader")
    archiveVersion.set("2.0-dev")
    archiveClassifier.set("all")
}

// JavaFX run configuration - Remove problematic module path
tasks.run.configure {
    // JavaFX plugin handles module path automatically
    // Just ensure proper JVM args for modern Java versions
    if (JavaVersion.current() >= JavaVersion.VERSION_11) {
        jvmArgs = listOf(
            "--add-exports", "javafx.base/com.sun.javafx.runtime=ALL-UNNAMED",
            "--add-exports", "javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED"
        )
    }
}
