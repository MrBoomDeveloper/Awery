import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(androidLibs.plugins.library)
}

kotlin {
    jvmToolchain(properties["awery.java.desktop"].toString().toInt())
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.core"
        compileSdk = properties["awery.sdk.target"].toString().toInt()
        minSdk = properties["awery.sdk.min"].toString().toInt()

        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.fromTarget(properties["awery.java.android"].toString())
                }
            }
        }
    }

    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xexpect-actual-classes"
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            api(libs.ktor.client.core)
            implementation(libs.filekit.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.encoding)

            implementation(libs.fileKache)
            implementation(composeLibs.runtime)
        }

        androidMain.dependencies {
            implementation(androidLibs.core)
            implementation(androidLibs.browser)
        }
    }
}