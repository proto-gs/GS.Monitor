plugins {
    kotlin("multiplatform") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

group = "com.gshttp.monitor"
version = "1.0.1"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
            // ИСПРАВЛЕНО: передаем чистую кастомную строку без использования перечислений плагина


            packageName = "gs-monitor"
            vendor = "G. Smerdov"
            packageVersion = "1.0.0"

            modules(
                "java.instrument",
                "java.management",
                "java.naming",
                "java.sql",
                "java.xml",
                "java.desktop",
                "jdk.crypto.ec"
            )

            windows {
                packageName = "GS.Monitor"
                menu = true
                shortcut = true
                dirChooser = true
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }

            linux {
                packageName = "gs-monitor"
                shortcut = true
                menuGroup = "Development"
                appCategory = "Utility"
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))


            }
        }
    }
}






















