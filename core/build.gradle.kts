import com.android.build.api.dsl.androidLibrary
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm("desktop")

    androidLibrary {
        namespace = "com.mrboomdev.awery.core"
        compileSdk = 35
        minSdk = 25
    }
}

buildkonfig {
    packageName = "com.mrboomdev.awery.generated"
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        field("CHANNEL", "STABLE")
        field("GIT_COMMIT", rootProject.ext["gitCommitHash"]!!.toString())
        field("VERSION_NAME", rootProject.ext["versionName"]!!.toString())
        field("VERSION_CODE", rootProject.ext["versionCode"]!!.toString().toInt())
    }

    defaultConfigs("alpha") {
        field("CHANNEL", "ALPHA")
    }

    defaultConfigs("beta") {
        field("CHANNEL", "BETA")
    }
}

fun TargetConfigDsl.field(name: String, value: String) =
    buildConfigField(STRING, name, value, const = true)

fun TargetConfigDsl.field(name: String, value: Int) =
    buildConfigField(INT, name, value.toString(), const = true)