import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import com.mrboomdev.awery.gradle.GenerateConfigurationsTask
import com.mrboomdev.awery.gradle.ProjectVersion.getGitCommitHash
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
	alias(libs.plugins.buildkonfig)
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
		inputDirectory = rootProject.layout.projectDirectory.dir(
			"resources/src/commonMain/composeResources")
	} to generatedConfigurationsDirectory,
	
	tasks.register<GenerateSettingsTask>("generateSettings") {
		className = "AwerySettings"
		inputFiles = listOf(
			rootProject.layout.projectDirectory.file(
				"resources/src/commonMain/composeResources/files/app_settings.json"),
			
			rootProject.layout.projectDirectory.file(
				"resources/src/commonMain/composeResources/files/system_settings.json")
		)
	} to generatedSettingsDirectory
).onEach {
	tasks.withType<KotlinCompile>().configureEach {
		dependsOn(it.first)
	}
}

kotlin {
	jvmToolchain(17)
	
	applyDefaultHierarchyTemplate()
	androidTarget()
	jvm("desktop")
	
	compilerOptions {
		freeCompilerArgs = listOf(
			"-Xexpect-actual-classes"
		)
	}

	sourceSets {
		all {
			languageSettings.optIn(
				"androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi"
			)
		}
		
		commonMain {
			for((_, generatedDirectory) in generations) {
				kotlin.srcDir(generatedDirectory.dir("kotlin"))
			}
			
			dependencies {
				// Core
				implementation(projects.resources)
				implementation(projects.ext)
				
				// Language extensions
				implementation(libs.kotlinx.serialization.json)
				implementation(kotlin("reflect"))
				
				// Yomi
				implementation(libs.bundles.yomi)
				implementation(libs.bundles.okhttp)
				
				// Database
				implementation(libs.room.runtime)
				implementation(libs.androidx.sqlite)
				implementation(libs.androidx.sqlite.bundled)
				
				// Compose Core
				implementation(libs.compose.runtime)
				implementation(libs.compose.ui)
				implementation(libs.compose.foundation)
				implementation(libs.lifecycle.viewmodel)
				implementation(libs.lifecycle.savedstate)
				implementation(libs.navigation.jetpack)
				api(compose.components.resources)
				
				// Adaptive layout
				implementation(libs.compose.adaptive)
				implementation(libs.compose.adaptive.layout)
				implementation(libs.compose.adaptive.navigation)
				
				// Components
				implementation(libs.compose.material3)
				implementation(libs.bundles.coil)
				api(libs.sonner)
			}
		}

		androidMain.dependencies {
			implementation(libs.safeargsnext)
			implementation(libs.androidx.core)
			implementation(libs.material)
			implementation(libs.xcrash)
			implementation(libs.androidx.preference)
			implementation(libs.quickjs.android)
			implementation(libs.bundles.exoplayer)
			implementation(libs.accompanist.drawablepainter)
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
				implementation(libs.quickjs.desktop)
				
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

buildkonfig {
	packageName = "com.mrboomdev.awery.generated"
	exposeObjectWithName = "BuildKonfig"
	
	defaultConfigs {
		field("CHANNEL", "STABLE")
		field("GIT_COMMIT", getGitCommitHash(project))
		field("VERSION_NAME", rootProject.ext["versionName"]!!.toString())
		field("VERSION_CODE", rootProject.ext["versionCode"]!!.toString().toInt())
	}
	
	defaultConfigs("alpha") {
		field("CHANNEL", "ALPHA")
	}
	
	defaultConfigs("beta") {
		field("CHANNEL", "BETA")
	}
}

fun TargetConfigDsl.field(name: String, value: String) = 
	buildConfigField(STRING, name, value, const = true)

fun TargetConfigDsl.field(name: String, value: Int) = 
	buildConfigField(INT, name, value.toString(), const = true)