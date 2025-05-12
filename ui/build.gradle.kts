import com.android.build.api.dsl.androidLibrary
import com.sun.jna.internal.ReflectionUtils.isDefault
import org.codehaus.groovy.runtime.ArrayTypeUtils.dimension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.ui"
        compileSdk = 35
        minSdk = 25
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.data)
            implementation(projects.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.components.resources)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.savedstate)
            implementation(libs.compose.adaptive)
            implementation(libs.compose.adaptive.layout)
            implementation(libs.compose.adaptive.navigation)
            implementation(libs.sonner)
            implementation(libs.navigation.jetpack)
            implementation(libs.coil.compose)
            implementation(libs.coil.svg)
            implementation(libs.coil.network.okhttp)
        }

        androidMain.dependencies {
            implementation(libs.material)
            implementation(libs.bundles.exoplayer)
            implementation(libs.accompanist.drawablepainter)

            // Android TV
            implementation(libs.compose.tv.foundation)
            implementation(libs.compose.tv.material)

            // These artifacts are only supported on Android
            implementation(libs.coil.gif)
            implementation(libs.coil.video)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

//composeCompiler {
//    stabilityConfigurationFiles.add(
//        rootProject.layout.projectDirectory.file(
//            "compose-stability.txt"))
//}

compose.resources {
    generateResClass = never
}