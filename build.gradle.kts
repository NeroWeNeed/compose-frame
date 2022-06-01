import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "github.nwn"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation("net.java.dev.jna:jna:5.11.0")
    implementation("net.java.dev.jna:jna-platform:5.11.0")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "compose-frame"
            packageVersion = "1.0.0"
        }
    }
}