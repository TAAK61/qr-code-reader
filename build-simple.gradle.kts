plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.qrreader"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

application {
    mainClass.set("MainKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("qr-code-reader")
    archiveVersion.set("1.0.0")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

// Task to run the application
tasks.register<JavaExec>("runApp") {
    group = "application"
    description = "Run the QR Code Reader application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("MainKt")
}