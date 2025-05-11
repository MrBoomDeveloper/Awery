import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm("desktop")

    androidLibrary {
        namespace = "com.mrboomdev.awery.data"
        compileSdk = 35
        minSdk = 25
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("reflect"))
            implementation(projects.core)
            implementation(libs.compose.runtime)

            // Settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            // Database
            implementation(libs.room.runtime)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

dependencies {
    add("kspDesktop", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
}