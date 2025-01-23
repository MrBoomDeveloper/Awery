import com.android.build.api.dsl.ApplicationProductFlavor
import com.mrboomdev.awery.gradle.ProjectVersion.generateVersionCode
import com.mrboomdev.awery.gradle.ProjectVersion.getGitCommitHash
import com.mrboomdev.awery.gradle.settings.GenerateSettingsTask
import com.mrboomdev.awery.gradle.settings.generatedSettingsKotlinDirectory
import com.mrboomdev.awery.gradle.settings.generatedSettingsResourcesDirectory

plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
}

val releaseVersion = properties["awery.app.version"].toString()
val packageName = "com.mrboomdev.awery"

room {
    schemaDirectory("${projectDir}/../schemas")
}

android {
    namespace = packageName
    compileSdk = properties["awery.sdk.target"].toString().toInt()

    defaultConfig {
        applicationId = packageName

        targetSdk = properties["awery.sdk.target"].toString().toInt()
        minSdk = properties["awery.sdk.min"].toString().toInt()

        versionCode = generateVersionCode()
        versionName = "$releaseVersion-${getGitCommitHash(project)}"
        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}")
    }

    androidResources {
        generateLocaleConfig = true
    }
    
    sourceSets["main"].apply { 
        kotlin.srcDir(generatedSettingsKotlinDirectory)
        resources.srcDir(generatedSettingsResourcesDirectory)
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
        viewBinding = true
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xcontext-receivers", "-Xmulti-platform", "-opt-in=kotlin.ExperimentalStdlibApi")
    }

    flavorDimensions += listOf("channel")

    productFlavors {
        fun ApplicationProductFlavor.createChannelProductFlavor(id: String, title: String) {
            dimension = "channel"
            versionNameSuffix = "-$id"
            applicationIdSuffix = ".$id"

            buildConfigField("String", "FILE_PROVIDER",
                "\"${packageName}.$id.FileProvider\"")

            buildConfigField("${packageName}.app.update.UpdatesChannel", "CHANNEL",
                "${packageName}.app.update.UpdatesChannel.${id.uppercase()}")

            manifestPlaceholders["fileProvider"] = "${packageName}.$id.FileProvider"
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
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.shortcuts)
    implementation(libs.androidx.preference)
    implementation(libs.bundles.aniyomi)
    implementation(projects.ext)
    implementation(projects.resources)
	
	// Database
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)

    // UI
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)
    implementation(libs.ebook.reader)
    implementation(libs.pageindicatorview)
    implementation(libs.colorpickerview)
    implementation(libs.balloon)
    implementation(libs.bigimageviewer)
    implementation(libs.glide.imageloader)
    implementation(libs.glide.imageViewFactory)
    implementation(libs.konfetti.xml)
    implementation(libs.animatedbottombar)
    implementation(libs.hauler)
	implementation(projects.shared)

	// Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.tv.material)
    implementation(libs.compose.tv.foundation)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(compose.components.resources)

    // Markdown
    implementation(libs.markwon.core)
    implementation(libs.markwon.editor)
    implementation(libs.markwon.strikethrough)
    implementation(libs.markwon.html)
    implementation(libs.markwon.image)
    implementation(libs.markwon.image.glide)
    implementation(libs.markwon.linkify)

    // Exoplayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    // Networking
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Serialization
    implementation(libs.safeargsnext)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.json.okio)
    implementation(libs.kotlinx.serialization.protobuf)

    // For removal
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.glide)
    implementation(libs.glide.okhttp3)
    implementation(libs.bundles.okhttp)
    implementation(libs.retrostreams)

	// Debugging
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.xcrash)
    debugImplementation(libs.leakcanary)
}

composeCompiler {
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file(
            "compose-stability.txt"))
}

tasks.register<GenerateSettingsTask>("generateSettings") {
    packageName = "com.mrboomdev.awery.generated"
    className = "AwerySettings"
    inputFiles = listOf(
        layout.projectDirectory.file("src/main/assets/app_settings.json"),
        layout.projectDirectory.file("src/main/assets/system_settings.json")
    )
}.let { tasks["preBuild"].dependsOn(it) }