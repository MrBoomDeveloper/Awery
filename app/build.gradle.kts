import com.android.build.api.dsl.ApplicationProductFlavor
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.ksp)
	alias(libs.plugins.android.app)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

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
			dependencies {
				// Core
				implementation(projects.resources)
				implementation(compose.runtime)
				
				// Data
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.lifecycle.viewmodel.compose)
				
				// Ui
				implementation(compose.ui)
				implementation(compose.foundation)
				api(compose.components.resources)
				api(libs.sonner)
				implementation(libs.androidx.navigation)
				
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
			implementation(libs.androidx.core)
			implementation(libs.androidx.appcompat)
			implementation(libs.androidx.splashscreen)
			
			implementation(libs.material)
			implementation(libs.bundles.exoplayer)
			implementation(libs.safeargsnext)
			
			implementation(project.dependencies.platform(libs.compose.bom))
			implementation(libs.compose.activity)
			implementation(libs.compose.ui)
			implementation(libs.compose.material3)
			
			implementation(projects.shared)
			implementation(projects.resources)
			implementation(projects.ext)
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
			}
		}
	}
}

android {
	namespace = properties["awery.packageName"].toString()
	compileSdk = properties["awery.sdk.target"].toString().toInt()
	
	defaultConfig {
		applicationId = namespace
		
		targetSdk = properties["awery.sdk.target"].toString().toInt()
		minSdk = properties["awery.sdk.min"].toString().toInt()
		
		versionCode = rootProject.ext["versionCode"].toString().toInt()
		versionName = rootProject.ext["versionName"].toString()
		buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}")
	}
	
	buildTypes {
		debug {
			isDebuggable = true
			isMinifyEnabled = false
			versionNameSuffix = "-debug"
		}
		
		release {
			versionNameSuffix = "-release"
			isDebuggable = false
			isMinifyEnabled = false
			isShrinkResources = false
			signingConfig = signingConfigs["debug"]
			setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
		}
	}
	
	buildFeatures {
		buildConfig = true
		compose = true
	}
	
	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.1"
	}
	
	flavorDimensions += listOf("channel", "platform")
	
	productFlavors {
		fun ApplicationProductFlavor.createChannelProductFlavor(id: String, title: String) {
			dimension = "channel"
			versionNameSuffix = "-$id"
			applicationIdSuffix = ".$id"
			
			buildConfigField("String", "FILE_PROVIDER",
				"\"${properties["awery.packageName"]}.$id.FileProvider\"")
			
			manifestPlaceholders["fileProvider"] = "${properties["awery.packageName"]}.$id.FileProvider"
			manifestPlaceholders["appLabel"] = "Awery $title"
		}
		
		register("alpha") {
			createChannelProductFlavor("alpha", "Alpha")
			buildConfigField("String", "UPDATES_REPOSITORY", "\"itsmechinmoy/awery-updater\"")
			isDefault = true
		}
		
		register("beta") {
			createChannelProductFlavor("beta", "Beta")
			buildConfigField("String", "UPDATES_REPOSITORY", "\"MrBoomDeveloper/Awery\"")
		}
		
		register("stable") {
			createChannelProductFlavor("stable", "Stable")
			buildConfigField("String", "UPDATES_REPOSITORY", "\"MrBoomDeveloper/Awery\"")
			manifestPlaceholders["appLabel"] = "Awery"
			applicationIdSuffix = null
		}
		
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

compose.desktop {
	application {
		mainClass = "com.mrboomdev.awery.MainKt"
		
		nativeDistributions {
			targetFormats(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Msi)
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

compose.resources {
	generateResClass = never
}