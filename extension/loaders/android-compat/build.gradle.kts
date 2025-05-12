import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm("desktop")

    androidLibrary {
        namespace = "com.mrboomdev.awery.extension.loaders.androidcompat"
        compileSdk = 35
        minSdk = 25
    }

    compilerOptions {
        freeCompilerArgs = listOf("-Xexpect-actual-classes")
    }
}