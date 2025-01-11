import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
}

kotlin {
	jvmToolchain(17)

	jvm {
		withJava()
	}

	sourceSets {
		val jvmMain by getting {
			dependencies {
				// Core
				implementation(projects.ext)
				implementation(projects.shared)

				// Ui
				implementation("org.jetbrains.compose.ui:ui-desktop:1.6.11")
				implementation(compose.foundation)
				implementation(compose.components.resources)
				implementation(compose.desktop.currentOs)

				// Utils
				implementation(libs.okhttp)
				implementation(libs.moshi)
				implementation(kotlin("stdlib-jdk8"))
			}
		}
	}
}

compose.desktop {
	application {
		mainClass = "com.mrboomdev.awery.desktop.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
			packageName = "com.mrboomdev.awery"

			packageVersion = properties["awery.app.version"].toString().split(".").let { args ->
				return@let "${args[0]}.${args[1]}.${args[2]}" + args.getOrNull(3).let { thirdArg ->
					thirdArg ?: ""
				}
			}

			windows {
				console = true
				perUserInstall = true
				menu = true
				menuGroup = "Awery"
				includeAllModules = true
				upgradeUuid = "6a7f9795-c323-4c10-a73a-55ac5506af01"
			}
		}
	}
}