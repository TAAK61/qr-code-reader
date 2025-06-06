import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.qrcoder"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // ZXing for QR code processing
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    // Testing dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

application {
    mainClass.set("MainKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Generate documentation
tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}

// Create JAR with dependencies
tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    
    // Include dependencies in the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Configure test reporting
tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

// Add JaCoCo for code coverage
apply(plugin = "jacoco")

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}