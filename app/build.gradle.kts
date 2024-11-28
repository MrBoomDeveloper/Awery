import com.android.build.api.dsl.ApplicationBaseFlavor
import com.android.build.api.dsl.ApplicationProductFlavor
import java.util.Locale
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.room)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val releaseVersion = "1.0.5.3"
val packageName = "com.mrboomdev.awery"

fun ApplicationBaseFlavor.setupVersion() {
    // Note: Please, don't edit it if you don't know what it does
    val startVersionCode = 2808
    val startMillis = 1719658313080

    val gitCommitHash = providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()

    versionName = "$releaseVersion-$gitCommitHash"

    versionCode = (startVersionCode + (System.currentTimeMillis() - startMillis) / 1000).let {
        if(it.toInt() <= 0 || it >= Int.MAX_VALUE) {
            throw IllegalStateException("We've reached an Integer limit! " +
                    "Now Awery 2 must be released! Generated version code: $it")
        }

        it.toInt()
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = packageName
    compileSdk = 35

    defaultConfig {
        applicationId = packageName
        targetSdk = 35
        minSdk = 25
        setupVersion()
        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}")
    }

    androidResources {
        generateLocaleConfig = true
    }

    sourceSets {
        get("main").apply {
            java.srcDirs(file("$projectDir/awery_gen/main/java"))
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
        }

        release {
            versionNameSuffix = "-release"
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs["debug"]
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xcontext-receivers", "-Xmulti-platform", "-opt-in=kotlin.ExperimentalStdlibApi")
    }

    flavorDimensions += listOf("channel")

    productFlavors {
        fun ApplicationProductFlavor.createChannelProductFlavor(id: String, title: String) {
            dimension = "channel"
            versionNameSuffix = "-$id"
            applicationIdSuffix = ".$id"

            buildConfigField("String", "FILE_PROVIDER",
                "\"${packageName}.$id.FileProvider\"")

            buildConfigField("${packageName}.app.update.UpdatesChannel", "CHANNEL",
                "${packageName}.app.update.UpdatesChannel.${id.uppercase()}")

            manifestPlaceholders["fileProvider"] = "${packageName}.$id.FileProvider"
            manifestPlaceholders["appLabel"] = "Awery $title"
        }

        register("alpha") {
            createChannelProductFlavor("alpha", "Alpha")
            buildConfigField("String", "UPDATES_REPOSITORY", "\"itsmechinmoy/awery-updater\"")
            isDefault = true
        }

        register("beta") {
            createChannelProductFlavor("beta", "Beta")
            buildConfigField("String", "UPDATES_REPOSITORY", "\"MrBoomDeveloper/Awery\"")
        }

        register("stable") {
            createChannelProductFlavor("stable", "Stable")
            buildConfigField("String", "UPDATES_REPOSITORY", "\"MrBoomDeveloper/Awery\"")
            manifestPlaceholders["appLabel"] = "Awery"
            applicationIdSuffix = null
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.shortcuts)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.xcrash.android)
    implementation(libs.deprecated.android.retrostreams)
    implementation(libs.bundles.aniyomi)
    implementation(project(":ext"))

    // Database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // UI
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)
    implementation(libs.ebook.reader)
    implementation(libs.pageindicatorview)
    implementation(libs.colorpickerview)
    implementation(libs.balloon)
    implementation(libs.bigimageviewer)
    implementation(libs.glide.imageloader)
    implementation(libs.glide.imageViewFactory)
    implementation(libs.konfetti.xml)
    implementation(libs.animatedBottomBar)
    implementation(libs.hauler)
    implementation(project(":ui"))

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.android.activity)
    implementation(libs.compose.android.ui)
    implementation(libs.compose.android.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.tv.material)
    implementation(libs.compose.tv.foundation)

    // Markdown
    implementation(libs.markwon.core)
    implementation(libs.markwon.editor)
    implementation(libs.markwon.strikethrough)
    implementation(libs.markwon.html)
    implementation(libs.markwon.image)
    implementation(libs.markwon.image.glide)
    implementation(libs.markwon.linkify)

    // Exoplayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    // Networking
    api(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.glide.annotations)
    implementation(libs.glide)
    implementation(libs.glide.okhttp3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.bundles.okhttp)

    // Serialization
    implementation(files("../libs/safe-args-next.aar"))
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.json.okio)
    implementation(libs.kotlinx.serialization.protobuf)

	// Debugging
    implementation(libs.compose.android.ui.tooling)
    debugImplementation(libs.leakcanary.android)
}

fun String.toCamelCase(): String {
    var result = ""

    for(s in split("_")) {
        result += Character.toUpperCase(s.toCharArray()[0])

        if(s.length > 1) {
            result += s.substring(1, s.length).lowercase(Locale.ENGLISH)
        }
    }

    return result
}

fun formatKey(o: Setting, usedKeys: MutableSet<String>): String {
    var result = "\tpublic static final "

    if(o.type == "select" && o.items != null) {
        val enumName = o.key!!.uppercase().toCamelCase() + "_Values"

        result += "EnumSetting<$enumName> ${o.key.uppercase()} =\n\t\t\tnew EnumSetting<>(\"${o.key}\", $enumName.class);\n\n"

        result += "\tpublic enum $enumName implements EnumWithKey {\n\t\t"

        val iterator = o.items.iterator()

        while(iterator.hasNext()) {
            val item = iterator.next()

            if(item.key != null && !usedKeys.add(item.key)) {
                throw IllegalStateException("Duplicate keys \"${item.key}\" were found in settings.json!" +
                        "\nYou have to remove one of them for app to work properly.")
            }

            result += item.key!!.uppercase() + "(\"${item.key}\")"

            if(iterator.hasNext()) {
                result += ",\n\t\t"
            }
        }

        result += ";\n\n\t\tprivate final String key;\n\n\t\t${enumName}(String key) {\n"
        result += "\t\t\tthis.key = key;\n\t\t}\n\n\t\t@Override\n\t\tpublic String getKey() {\n"
        result += "\t\t\treturn key;\n\t\t}\n\t}\n\n"

        return result
    }

    if(listOf("action", "select", "multiselect").contains(o.type)) {
        return result + "String ${o.key!!.uppercase()} = \"${o.key}\";\n"
    }

    when(o.type) {
        "string" -> result += "String"
        "integer", "select_integer" -> result += "Integer"
        "boolean", "screen_boolean" -> result += "Boolean"
    }

    result += "Setting ${o.key!!.uppercase()} = () -> \"${o.key}\";\n"
    return result
}

fun collectKeys(from: Setting, usedKeys: MutableSet<String>): String {
    if(from.key != null && !usedKeys.add(from.key)) {
        throw IllegalStateException("Duplicate keys \"${from.key}\" were found in settings.json!" +
                "\nYou have to remove one of them for app to work properly.")
    }

    return buildString {
        when(from.type) {
            "screen" -> {
                if(from.items != null) {
                    for(item in from.items) {
                        append(collectKeys(item, usedKeys))
                    }
                }
            }

            "string", "integer", "boolean", "action",
            "select", "select_integer", "multiselect" -> append(formatKey(from, usedKeys))
        }
    }
}

data class Setting(val key: String?, val type: String?, val items: List<Setting>?)

@OptIn(ExperimentalStdlibApi::class)
fun generateSettingsClass(dir: File) {
    val settings = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter<Setting>().fromJson(
        file("$projectDir/src/main/assets/settings.json").readText())!!

    File(dir, "AwerySettings.java").writeText(buildString {
        append("package com.mrboomdev.awery.generated;\n")
        append("\n")
        append("import com.mrboomdev.awery.data.settings.NicePreferences.*;\n")
        append("import com.mrboomdev.awery.data.settings.NicePreferences;\n")
        append("import com.mrboomdev.awery.data.settings.SettingsItem;\n")
        append("\n")
        append("// Auto-generated class created during the compilation. Please, do not edit it.\n")
        append("public class AwerySettings {\n")

        append("\tpublic static SettingsItem get(String key) {\n")
        append("\t\treturn NicePreferences.getSettingsMap().findItem(key);\n")
        append("\t}\n\n")

        append(collectKeys(settings, HashSet()))
        append("}")
    })
}

tasks.register("generateClasses") {
    file("$projectDir/awery_gen/main/java/com/mrboomdev/awery/generated").apply {
        mkdirs()
        generateSettingsClass(this)
    }
}

tasks["preBuild"].dependsOn(tasks["generateClasses"])