import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

sourceSets {
    main {
        resources.srcDir("src/resources")
    }
}

compose.desktop {
    application {
        mainClass = "io.github.daisukikaffuchino.ciallo0fff.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Ciallo0FFF"
            packageVersion = "2.0.0"
            vendor = "daisukiKaffuChino"
            copyright = "© 2026 daisukiKaffuChino. All rights reserved."
            description = "Live-Controller WebSocket Client."

            windows {
                shortcut = true
                menu = true
                menuGroup = "Ciallo0FFF"
                iconFile.set(project.file("src/resources/ic_launcher_win.ico"))
            }

            linux {
                menuGroup = "Ciallo0FFF"
                debMaintainer = "konohatamira@outlook.com"
                iconFile.set(project.file("src/resources/ic_launcher_linux.png"))
            }

            macOS {
                bundleID = "io.github.daisukikaffuchino.ciallo0fff"
                appCategory = "public.app-category.utilities"
                iconFile.set(project.file("src/resources/ic_launcher_mac.icns"))
            }

        }
    }
}

afterEvaluate {
    tasks.named<JavaExec>("run") {
        jvmArgs("-Dciallo0fff.testEnvironment=true")
        args("--test-environment")
    }
}
