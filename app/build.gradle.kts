import com.android.build.api.dsl.ApplicationProductFlavor
import java.util.Locale
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp").version("2.0.21-1.0.25")
    kotlin("plugin.serialization") version "2.0.20"
    alias(libs.plugins.compose.compiler)
    id("androidx.room").version(libs.versions.roomRuntime)
}

val useSdk = 35
val releaseVersion = "1.0.5.2"
val packageName = "com.mrboomdev.awery"

// Note: Please, don't edit it if you don't know what it does
val startVersionCode = 2808
val startMillis = 1719658313080

val gitCommitHash = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.get().trim()

android {
    namespace = packageName
    compileSdk = useSdk

    defaultConfig {
        applicationId = packageName
        targetSdk = useSdk
        minSdk = 25

        buildConfigField("boolean", "IS_BETA",
            "CHANNEL != ${packageName}.app.update.UpdatesChannel.STABLE")

        /* Only one version can be published per second or else horrible things will happen.
        *  After 70 years we'll be required to make Awery 2 because of an integer limit,
        *  Date receiver from the test below:
        *
        *   3865053756028  :  2145398250  -  Mon Jun 23 15:02:36 YEKT 2092
        *   3865747384956  :  2146091879  -  Tue Jul 01 15:43:04 YEKT 2092
        *   3866441013884  :  2146785508  -  Wed Jul 09 16:23:33 YEKT 2092
        *   3867134642812  :  2147479137  -  Thu Jul 17 17:04:02 YEKT 2092
        *   3867828271740  :  -2146794530  -  Fri Jul 25 17:44:31 YEKT 2092
        *  */
        versionName = "$releaseVersion-$gitCommitHash"
        versionCode = ((startVersionCode + ((System.currentTimeMillis() - startMillis) / 1000)).toInt())

        /*long i = System.currentTimeMillis()
        var a = new Date(i)
        while(true) {
            i += (1000 * 60 * 60 * 24 * 356)
            a.setTime(i)

            versionCode ((startVersionCode + ((i - startMillis) / 1000)) as int)

            System.out.println("$i  :  $versionCode  -  ${a}")

            if(versionCode < 0 || versionCode >= Integer.MAX_VALUE) {
                throw new IllegalStateException("We've reached the end. Now Awery 2 must be released!" +
                        " Input: $versionCode : ${a}")
            }
        }*/

        if(versionCode!! < 0 || versionCode!! >= Int.MAX_VALUE) {
            throw IllegalStateException("We've reached the end. " +
                    "Now Awery 2 must be released! Input int: $versionCode")
        }

        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    androidResources {
        generateLocaleConfig = true
    }

    sourceSets {
        get("main").apply {
            java.srcDirs(file("$projectDir/awery_gen/main/java"))
        }
    }

    room {
        schemaDirectory("schemas")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.core.google.shortcuts)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.xcrash.android.lib)
    implementation(libs.deprecated.android.retrostreams)
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

    // Image Loading
    api(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.glide.annotations)
    implementation(libs.glide)
    implementation(libs.glide.okhttp3)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.dnsoverhttps)
    implementation(libs.okhttp.brotli)

    // Aniyomi
    implementation(libs.quickjs.android)
    implementation(libs.rx.java)
    implementation(libs.rx.android)
    implementation(libs.injekt)
    implementation(libs.jsoup)
    implementation(libs.java.nat.sort)

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
        append("import com.mrboomdev.awery.app.data.settings.NicePreferences.*;\n")
        append("import com.mrboomdev.awery.app.data.settings.NicePreferences;\n")
        append("import com.mrboomdev.awery.app.data.settings.SettingsItem;\n")
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