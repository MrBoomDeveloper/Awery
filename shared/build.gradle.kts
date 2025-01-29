import com.mrboomdev.awery.gradle.GenerateConfigurationsTask
import com.mrboomdev.awery.gradle.generatedConfigurationsDirectory
import com.mrboomdev.awery.gradle.settings.GenerateSettingsTask
import com.mrboomdev.awery.gradle.settings.generatedSettingsDirectory
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.ksp)
	alias(libs.plugins.android.library)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.room)
}

room {
	schemaDirectory("${rootProject.projectDir}/schemas")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

val generations = listOf(
	tasks.register<GenerateConfigurationsTask>("generateConfigurations") {
		inputDirectory = rootProject.layout.projectDirectory.dir("resources/src/commonMain/composeResources")
	} to generatedConfigurationsDirectory,
	
	tasks.register<GenerateSettingsTask>("generateSettings") {
		className = "AwerySettings"
		inputFiles = listOf(
			rootProject.layout.projectDirectory.file("resources/src/commonMain/composeResources/files/app_settings.json"),
			rootProject.layout.projectDirectory.file("resources/src/commonMain/composeResources/files/system_settings.json")
		)
	} to generatedSettingsDirectory
)

kotlin {
	jvmToolchain(17)
	
	applyDefaultHierarchyTemplate()
	androidTarget()
	jvm("desktop")
	
	compilerOptions {
		freeCompilerArgs = listOf("-Xexpect-actual-classes")
	}

	sourceSets {
		commonMain {
			for((_, generatedDirectory) in generations) {
				kotlin.srcDir(generatedDirectory.dir("kotlin"))
			}
			
			dependencies {
				// Core
				implementation(projects.resources)
				implementation(projects.ext)
				implementation(compose.runtime)
				
				// Yomi
				implementation(libs.bundles.yomi)
				implementation(libs.bundles.okhttp)
				
				// Data
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.lifecycle.viewmodel.compose)
				
				// Database
				implementation(libs.room.runtime)
				implementation("androidx.sqlite:sqlite:2.4.0")
				implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha12")
				
				// Ui
				implementation(compose.ui)
				implementation(compose.foundation)
				api(compose.components.resources)
				api(libs.sonner)
				
				// Navigation
				implementation(libs.androidx.navigation)
				api(libs.voyager.navigator)
				api(libs.voyager.screenmodel)
				api(libs.voyager.tab.navigator)
				api(libs.voyager.transitions)
				
				// Adaptive layout
				implementation(libs.adaptive)
				implementation(libs.adaptive.layout)
				implementation(libs.adaptive.navigation)
				
				// Components
				implementation(compose.material3)
				implementation(libs.coil.compose)
			}
		}

		androidMain.dependencies {
			implementation(libs.safeargsnext)
			implementation(libs.androidx.core)
			implementation(libs.material)
			implementation(libs.xcrash)
			implementation(libs.androidx.preference)
		}
		
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		invokeWhenCreated("androidTv") {
			dependencies {
				implementation(libs.compose.tv.foundation)
				implementation(libs.compose.tv.material)
			}
		}
		
		val desktopMain by getting {
			dependencies {
				implementation(compose.desktop.common)
				
				// Android platform classes ported to jvm
				implementation(projects.compat)
			}
		}
	}
}

dependencies {
	add("kspDesktop", libs.room.compiler)
	add("kspAndroid", libs.room.compiler)
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
	
	flavorDimensions += "platform"
	
	productFlavors {
		register("mobile") {
			isDefault = true
			dimension = "platform"
		}
		
		register("tv") {
			dimension = "platform"
		}
	}
}

composeCompiler {
	stabilityConfigurationFiles.add(
		rootProject.layout.projectDirectory.file(
			"compose-stability.txt"))
}

compose.resources {
	generateResClass = never
}

generations.forEach {
	tasks.withType<KotlinCompile>().configureEach {
		dependsOn(it.first)
	}
}