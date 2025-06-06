import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.dokka") version "1.9.10"
    jacoco
    `maven-publish`
}

group = "com.qrcoder"
version = "2.0.0-dev"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
    
    // QR Code processing with ZXing
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    
    // Logging framework
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.slf4j:slf4j-api:2.0.9")
    
    // Configuration management
    implementation("com.typesafe:config:1.4.3")
    
    // Testing framework
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    
    // Test runtime
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("MainKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    finalizedBy(tasks.jacocoTestReport)
}

// Configure JaCoCo for code coverage
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// Configure Shadow JAR with proper dependencies
tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("qr-code-reader")
    archiveVersion.set("2.0.0-dev")
    
    manifest {
        attributes["Main-Class"] = "MainKt"
        attributes["Implementation-Title"] = "QR Code Reader"
        attributes["Implementation-Version"] = project.version
    }
    
    // Minimize JAR size by excluding unnecessary files
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/DEPENDENCIES")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
}

// Standard JAR configuration (lighter, without dependencies)
tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
        attributes["Implementation-Title"] = "QR Code Reader"
        attributes["Implementation-Version"] = project.version
    }
    archiveClassifier.set("slim")
}

// Task to run the application
tasks.register<JavaExec>("runApp") {
    group = "application"
    description = "Run the QR Code Reader application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("MainKt")
}

// Documentation generation
tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("docs/api"))
}

// Source JAR
tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

// Javadoc JAR
tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
    dependsOn(tasks.dokkaHtml)
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            
            pom {
                name.set("QR Code Reader")
                description.set("A powerful QR code reader application with advanced image processing")
                url.set("https://github.com/example/qr-code-reader")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("developer")
                        name.set("QR Code Reader Team")
                        email.set("team@qrcoder.com")
                    }
                }
            }
        }
    }
}

// Quality assurance tasks
tasks.register("qualityCheck") {
    group = "verification"
    description = "Run all quality checks"
    dependsOn(tasks.test, tasks.jacocoTestReport)
}