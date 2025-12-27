import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(androidLibs.plugins.library)
}

kotlin {
    jvmToolchain(properties["awery.java.desktop"].toString().toInt())
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.extension.loaders"
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
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes"
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.data)
            implementation(projects.resources)
            implementation(projects.extension.sdk)
            implementation(projects.extension.bundled)

            implementation("net.lingala.zip4j:zip4j:2.11.5")
            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.json.okio)
            implementation(libs.settings)
            implementation(libs.ktor.client.core)
            implementation(libs.filekit.core)
            implementation(composeLibs.runtime)
            implementation(composeLibs.resources)
            
            // Mock Android stuff on desktop and provide
            // access to the api from a common source.
            implementation(projects.extension.loaders.androidCompat)

            // Yomi stuff
            implementation(libs.jsoup)
            implementation(libs.ksoup)
            implementation(libs.injekt)
            implementation(libs.rx.java)
            implementation(libs.bundles.okhttp)
        }

        androidMain.dependencies {
            implementation(androidLibs.core)
            implementation(androidLibs.webkit)
            implementation(androidLibs.bundles.ackpine)

            // Yomi stuff
            implementation(libs.quickjs.android)
            implementation(libs.rx.android)
        }

        val desktopMain by getting {
            dependencies {
                // Yomi stuff
                implementation(libs.quickjs.desktop)
            }
        }
    }
}