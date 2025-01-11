plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    // for publishing to maven central
    `maven-publish`
    signing
    id("cl.franciscosolis.sonatype-central-upload") version "1.0.3"
}

group = "llc.virtue"
version = "0.0.1"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
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

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commons.math3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)
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
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("kopairwise")
                description.set("A Kotlin library for pairwise combination generation.")
                url.set("https://github.com/virtue-llc-jp/kopairwise")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("virtue-llc-jp")
                        name.set("VIRTUE LLC Japan")
                        email.set("info@virtue.llc")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/virtue-llc-jp/kopairwise.git")
                    developerConnection.set("scm:git:ssh://github.com/virtue-llc-jp/kopairwise.git")
                    url.set("https://github.com/virtue-llc-jp/kopairwise")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.named<cl.franciscosolis.sonatypecentralupload.SonatypeCentralUploadTask>("sonatypeCentralUpload") {
    // 公開するファイルを生成するタスクに依存する。
    dependsOn("jar", "sourcesJar", "javadocJar", "generatePomFileForMavenPublication")

    // Central Portalで生成したトークンを指定する。
    username = System.getenv("SONATYPE_CENTRAL_USERNAME")
    password = System.getenv("SONATYPE_CENTRAL_PASSWORD")

    // タスク名から成果物を取得する。
    archives = files(
        tasks.named("jar"),
        tasks.named("sourcesJar"),
        tasks.named("javadocJar"),
    )
    // POMファイルをタスクの成果物から取得する。
    pom = file(
        tasks.named("generatePomFileForMavenPublication").get().outputs.files.single()
    )

    // PGPの秘密鍵を指定する。
    signingKey = System.getenv("PGP_SIGNING_KEY")
    // PGPの秘密鍵のパスフレーズを指定する。
    signingKeyPassphrase = System.getenv("PGP_SIGNING_KEY_PASSPHRASE")
}
