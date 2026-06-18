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

                // Сетевая библиотека для сканера URL
                implementation("com.squareup.okhttp3:okhttp:4.12.0")

                // Поддержка Dispatchers.Main для стабильной работы корутин на Windows/Linux
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            // Динамический выбор формата сборки под текущую ОС
            val currentOs = System.getProperty("os.name").lowercase()
            if (currentOs.contains("win")) {
                targetFormats(
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
                )
            } else if (currentOs.contains("linux")) {
                targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            }

            packageName = "gs-monitor"
            packageVersion = "1.0.1"
            vendor = "G. Smerdov"

            // ИСПРАВЛЕНО: Вместо жесткого списка модулей включаем автоматический сборщик.
            // Это позволит jlink корректно упаковать OkHttp и корутины.
            includeAllModules = true

            windows {
                menu = true
                shortcut = true
                dirChooser = true
                // Исправлено: Для Windows возвращен корректный .ico
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














