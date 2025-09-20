import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(androidLibs.plugins.library)
}

kotlin {
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.extension.loaders.androidcompat"
        compileSdk = 35
        minSdk = 25
    }

    compilerOptions {
        freeCompilerArgs = listOf("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
        }
        
        androidMain.dependencies {
            api(androidLibs.preference)
        }
    }
}