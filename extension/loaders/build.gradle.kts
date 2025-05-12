import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm("desktop")

    androidLibrary {
        namespace = "com.mrboomdev.awery.extension.loaders"
        compileSdk = 35
        minSdk = 25
    }

    sourceSets {
        commonMain.dependencies {
            // Mock Android stuff on desktop and provide
            // access to the api from a common source.
            implementation(projects.extension.loaders.androidCompat)

            implementation(libs.jsoup)
            implementation(libs.ksoup)
            implementation(libs.injekt)
            implementation(libs.rx.java)
            implementation(libs.okhttp)
        }

        androidMain.dependencies {
            implementation(libs.quickjs.android)
            implementation(libs.rx.android)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.quickjs.desktop)
            }
        }
    }
}