import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(androidLibs.plugins.library)
    alias(composeLibs.plugins.multiplatform)
    alias(composeLibs.plugins.compiler)
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.ui"
        compileSdk = 35
        minSdk = 25
    }

    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xexpect-actual-classes",
            "-Xcontext-parameters"
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.room.runtime)
            implementation(libs.humanReadable)
            implementation("io.github.z4kn4fein:semver:3.0.0")
            implementation("me.xdrop:fuzzywuzzy:1.4.0")
			implementation("com.eygraber:compose-placeholder-material3:1.0.12")

            // Projects
            implementation(projects.core)
            implementation(projects.data)
            implementation(projects.resources)
            implementation(projects.extension.sdk)
            implementation(projects.extension.loaders)

            // Kotlin
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // ViewModel
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.savedstate)

            // Compose
            implementation(composeLibs.runtime)
            implementation(composeLibs.foundation)
            implementation(composeLibs.ui)
            implementation(composeLibs.resources)
            api(composeLibs.material3)
            implementation(composeLibs.haze)
            implementation(composeLibs.materialKolor)
            implementation(composeLibs.coil.compose)
            implementation(composeLibs.coil.svg)
            implementation(composeLibs.coil.network.okhttp)
            implementation(composeLibs.html)
            implementation(composeLibs.confetti)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)

            // Navigation
            implementation(composeLibs.navigation.jetpack)
            compileOnly(composeLibs.navigation.core.jvm)
            compileOnly(composeLibs.navigation.jetpack.jvm)

            // Gradle doesn't know what the fuck is going on with the platform resolution,
            // so we have to glue up this whole fucking thing together by our own hands 🥰
            compileOnly("com.kmpalette:kmpalette-core-jvm:3.1.0") {
                exclude(group = "com.kmpalette", module = "androidx-palette")
                exclude(group = "com.kmpalette", module = "kmpalette-bitmap-loader")
            }

            compileOnly("com.kmpalette:extensions-network-jvm:3.1.0") {
                exclude(group = "com.kmpalette", module = "kmpalette-bitmap-loader")
            }

            compileOnly("com.kmpalette:androidx-palette-jvm:3.1.0")
            compileOnly("com.kmpalette:kmpalette-bitmap-loader-jvm:3.1.0")
        }

        androidMain.dependencies {
            implementation(androidLibs.material)
            implementation(androidLibs.bundles.exoplayer)
            implementation(androidLibs.compose.activity)
            implementation(androidLibs.compose.drawablepainter)
            implementation(composeLibs.tv.material)

            // ExoPlayer
            implementation(androidLibs.media3.exoplayer)
            implementation(androidLibs.media3.exoplayer.dash)
            implementation(androidLibs.media3.exoplayer.hls)
            implementation(androidLibs.media3.datasource.okhttp)
            implementation(androidLibs.media3.session)
            implementation(androidLibs.media3.ui)
            implementation(androidLibs.media3.ui.compose)

            // These artifacts are only supported on Android
            implementation(composeLibs.coil.gif)
            implementation(composeLibs.coil.video)

            runtimeOnly("com.kmpalette:kmpalette-core:3.1.0")
            runtimeOnly("com.kmpalette:extensions-network:3.1.0")
        }

        val desktopMain by getting {
            dependencies {
                implementation(composeLibs.desktop.asString()) {
                    exclude(group = "org.jetbrians.compose.material")
                }
                
                implementation("uk.co.caprica:vlcj:4.11.0")

                runtimeOnly("com.kmpalette:kmpalette-core-jvm:3.1.0")
                runtimeOnly("com.kmpalette:extensions-network-jvm:3.1.0")
                runtimeOnly("com.kmpalette:androidx-palette-jvm:3.1.0")
                runtimeOnly("com.kmpalette:kmpalette-bitmap-loader-jvm:3.1.0")
                implementation("org.jetbrains.compose.ui:ui-util:${composeLibs.versions.compose.get()}")
            }
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

fun Provider<MinimalExternalModuleDependency>.asString() = buildString { 
    with(get()) {
        append(group)
        append(":")
        append(name)
        append(":")
        append(version)
    }
}