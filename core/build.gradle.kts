import com.android.build.api.dsl.androidLibrary
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(androidLibs.plugins.library)
}

kotlin {
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.core"
        compileSdk = 35
        minSdk = 25
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
        }

        androidMain.dependencies {
            implementation(androidLibs.core)
            implementation(androidLibs.browser)
        }
    }
}