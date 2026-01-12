import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // PDF parsing
    implementation("org.apache.pdfbox:pdfbox:3.0.1")

    // Kotlin serialization
    implementation(libs.kotlinx.serialization.json)

    // YAML support (kaml)
    implementation("com.charleskorn.kaml:kaml:0.55.0")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

application {
    mainClass.set("com.katiba.parser.ConstitutionParserKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.katiba.parser.ConstitutionParserKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

