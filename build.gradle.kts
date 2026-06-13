plugins {
    kotlin("multiplatform") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

group = "com.gshttp.monitor"
version = "1.0.0"

kotlin {

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.MICROSOFT)
    }

    jvm("desktop") {
        withJava()
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "GS.Monitor"
            packageVersion = "1.0.0"
            vendor = "G. Smerdov"

            windows {
                menu = true
                shortcut = true
                dirChooser = true
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
        }
    }
}









