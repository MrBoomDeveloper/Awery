import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()

    androidLibrary {
        namespace = "com.mrboomdev.awery.extension.api"
        compileSdk = 35
        minSdk = 25
    }
}