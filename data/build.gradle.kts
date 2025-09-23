import com.android.build.api.dsl.androidLibrary
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.buildkonfig)
    alias(composeLibs.plugins.compiler)
    alias(androidLibs.plugins.library)
}

room {
    schemaDirectory("$projectDir/dbSchemas")
}

kotlin {
    jvm("desktop")

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.mrboomdev.awery.data"
        compileSdk = 35
        minSdk = 25
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.extension.sdk)
            implementation(projects.resources)
            implementation(composeLibs.resources)

            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.filekit.core)
            implementation(composeLibs.runtime)

            // Settings
            implementation(libs.settings)
            implementation(libs.settings.coroutines)

            // Database
            api(libs.room.runtime)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

buildkonfig {
    packageName = "com.mrboomdev.awery.data"
    objectName = "AweryBuildConfig"

    fun TargetConfigDsl.field(name: String, value: String) =
        buildConfigField(FieldSpec.Type.STRING, name, value, const = true)

    fun TargetConfigDsl.field(name: String, value: Int) =
        buildConfigField(FieldSpec.Type.INT, name, value.toString(), const = true)

    fun TargetConfigDsl.field(name: String, value: Boolean) =
        buildConfigField(FieldSpec.Type.BOOLEAN, name, value.toString(), const = true)

    defaultConfigs {
        field("appVersion", properties["awery.app.versionName"].toString())
        field("appVersionCode", properties["awery.app.versionCode"].toString().toInt())
        field("extVersion", properties["VERSION_NAME"].toString())
        field("debug", false)
    }
    
    targetConfigs { 
        create("androidDebug") {
            field("debug", true)
        }
    }
}

dependencies {
    add("kspDesktop", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
}

// Reason: Task ':data:kspKotlinDesktop' uses this output of task ':data:generateBuildKonfig' 
// without declaring an explicit or implicit dependency. 
// This can lead to incorrect results being produced, depending on what order the tasks are executed.
afterEvaluate {
    afterEvaluate { 
        afterEvaluate {
            // Task generateBuildKonfig is being registered after a little delay,
            // so we do this magic to link all this shit together.
            for(task in arrayOf(/*"kspKotlinAndroid", */"kspKotlinDesktop")) {
                tasks.getByName(task).dependsOn(tasks.getByName("generateBuildKonfig"))
            }
        }
    }
}