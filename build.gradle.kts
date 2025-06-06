plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
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
    
    testImplementation("junit:junit:4.13.2")
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

tasks.shadowJar {
    archiveBaseName.set("qr-code-reader")
    archiveVersion.set("2.0-dev")
    archiveClassifier.set("all")
}

// JavaFX run configuration
tasks.run.configure {
    if (JavaVersion.current() >= JavaVersion.VERSION_11) {
        jvmArgs = listOf(
            "--module-path", System.getProperty("javafx.runtime.path", ""),
            "--add-modules", "javafx.controls,javafx.fxml,javafx.swing"
        )
    }
}
