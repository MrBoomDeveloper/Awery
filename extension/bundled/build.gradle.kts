import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(androidLibs.plugins.library)
}

kotlin {
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.extension.bundled"
        compileSdk = 35
        minSdk = 25
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes"
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.extension.sdk)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.encoding)
        }
    }
}