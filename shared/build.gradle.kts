import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.android.library)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
	jvmToolchain(17)
	
	compilerOptions {
		freeCompilerArgs = listOf("-Xexpect-actual-classes")
	}

	androidTarget()
	jvm("desktop")

	applyDefaultHierarchyTemplate()

	sourceSets {
		commonMain.dependencies {
			// Core
			implementation(projects.ext)
			implementation(compose.runtime)
			implementation(libs.kotlinx.serialization.json)
			
			// Ui
			implementation(compose.ui)
			implementation(compose.foundation)
			implementation(compose.components.resources)
			implementation(libs.androidx.lifecycle.viewmodel.compose)

			// Adaptive layout
			implementation(libs.androidx.adaptive)
			implementation(libs.androidx.adaptive.layout)
			implementation(libs.androidx.adaptive.navigation)

			// Components
			implementation(compose.material3)
			implementation(libs.coil.compose)
		}

		androidMain.dependencies {
			implementation(files("../libs/safe-args-next.aar"))
			implementation(libs.androidx.core)
			implementation(libs.material)
		}

		val desktopMain by getting {
			dependencies {
				implementation(compose.desktop.common)
			}
		}
	}
}

compose.resources {
	packageOfResClass = "com.mrboomdev.awery.generated"
	publicResClass = true
}

android {
	namespace = "com.mrboomdev.awery.shared"
	compileSdk = properties["awery.sdk.target"].toString().toInt()

	defaultConfig {
		minSdk = properties["awery.sdk.min"].toString().toInt()
	}

	buildFeatures {
		buildConfig = true
	}
}