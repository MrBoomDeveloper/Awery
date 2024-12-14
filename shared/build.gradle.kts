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

	sourceSets {
		val desktopMain by getting

		commonMain.dependencies {
			// Used projects
			api(projects.ext)
			api(projects.ext.lib)

			// Core
			implementation(compose.foundation)
			implementation(compose.runtime)
			implementation(compose.ui)
			implementation(compose.components.resources)

			// Adaptive layout
			implementation(libs.androidx.adaptive)
			implementation(libs.androidx.adaptive.layout)
			implementation(libs.androidx.adaptive.navigation)

			// Components
			implementation(compose.material3)
			implementation(libs.coil.compose)
		}

		androidMain.dependencies {
			implementation(libs.androidx.core)
			implementation(libs.material)
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