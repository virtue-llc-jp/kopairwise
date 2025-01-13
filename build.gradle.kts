import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    // can use kotest
    id("io.kotest") version "0.4.11"

    // for publishing to maven central
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "llc.virtue"
version = "0.0.2"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    // Use kotest
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.datatest)
    testImplementation(libs.kotest.property)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

// publishing to maven central
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates(group.toString(), name, version.toString())

    pom {
        name.set(project.name)
        description.set("A Kotlin library for pairwise combination generation.")
        inceptionYear.set("2025")
        url.set("https://github.com/virtue-llc-jp/kopairwise")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("virtue-llc-jp")
                name.set("VIRTUE LLC Japan")
                url.set("info@virtue.llc")
            }
        }
        scm {
            url.set("https://github.com/virtue-llc-jp/kopairwise")
            connection.set("scm:git:git://github.com/virtue-llc-jp/kopairwise.git")
            developerConnection.set("scm:git:ssh://github.com/virtue-llc-jp/kopairwise.git")
        }
    }

    configure(
        KotlinJvm(
//            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            javadocJar = JavadocJar.None(),
            sourcesJar = true,
        )
    )
}
