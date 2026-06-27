plugins {
    kotlin("multiplatform") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

group = "com.gshttp.monitor"
version = "1.0.2"

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
                implementation(compose.materialIconsExtended)
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            val currentOs = System.getProperty("os.name").lowercase()

            if (currentOs.contains("win")) {
                targetFormats(
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
                )
            } else if (currentOs.contains("linux")) {
                targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            }

            packageName = "gs.monitor"
            packageVersion = "1.0.2"
            vendor = "G. Smerdov"

            
            includeAllModules = true

            windows {
                menu = true
                shortcut = true
                dirChooser = true
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }

            linux {
                shortcut = true
                menuGroup = "Development"
                appCategory = "Utility"
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}














