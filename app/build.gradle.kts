import java.util.Locale
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
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
            assets.srcDirs(files("$projectDir/schemas"))
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

    flavorDimensions.add("type")

    productFlavors {
        register("alpha") {
            isDefault = true
            dimension = "type"
            versionNameSuffix = "-alpha"
            applicationIdSuffix = ".alpha"

            buildConfigField("${packageName}.app.update.UpdatesChannel", "CHANNEL", "${packageName}.app.update.UpdatesChannel.ALPHA")
            buildConfigField("String", "FILE_PROVIDER", "\"${packageName}.alpha.FileProvider\"")
            buildConfigField("String", "UPDATES_REPOSITORY", "\"itsmechinmoy/awery-updater\"")

            manifestPlaceholders["fileProvider"] = "${packageName}.alpha.FileProvider"
            manifestPlaceholders["appLabel"] = "Awery Alpha"
        }

        register("beta") {
            dimension = "type"
            versionNameSuffix = "-beta"
            applicationIdSuffix = ".beta"

            buildConfigField("${packageName}.app.update.UpdatesChannel", "CHANNEL", "${packageName}.app.update.UpdatesChannel.BETA")
            buildConfigField("String", "FILE_PROVIDER", "\"${packageName}.beta.FileProvider\"")
            buildConfigField("String", "UPDATES_REPOSITORY", "\"MrBoomDeveloper/Awery\"")

            manifestPlaceholders["fileProvider"] = "${packageName}.beta.FileProvider"
            manifestPlaceholders["appLabel"] = "Awery Beta"
        }

        register("prod") {
            dimension = "type"
            versionNameSuffix = "-stable"

            buildConfigField("${packageName}.app.update.UpdatesChannel", "CHANNEL", "${packageName}.app.update.UpdatesChannel.STABLE")
            buildConfigField("String", "FILE_PROVIDER", "\"${packageName}.FileProvider\"")
            buildConfigField("String", "UPDATES_REPOSITORY", "\"MrBoomDeveloper/Awery\"")

            manifestPlaceholders["fileProvider"] = "${packageName}.FileProvider"
            manifestPlaceholders["appLabel"] = "Awery"
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
        freeCompilerArgs = listOf("-Xcontext-receivers", "-Xmulti-platform")
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.webkit)
    implementation(libs.fragment.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.xcrash.android.lib)
    implementation(libs.android.retrostreams)
    implementation(project(":ext"))

    // Database
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    // UI
    implementation(libs.androidx.core.splashscreen)
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

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.androidx.material)
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
    implementation(libs.glide.annotations)
    annotationProcessor(libs.glide.compiler)
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
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.injekt)
    implementation(libs.jsoup)
    implementation(libs.java.nat.sort)

    // Serialization
    implementation(files("../libs/safe-args-next.aar"))
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.json.okio)
    implementation(libs.kotlinx.serialization.protobuf)
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
    var result = "    public static final "

    if(o.type == "select" && o.items != null) {
        val enumName = o.key!!.uppercase(Locale.ENGLISH).toCamelCase() + "_Values"

        result = "\n" + result
        result += "EnumSetting<$enumName> ${o.key.uppercase(Locale.ENGLISH)} = new EnumSetting<>(\"${o.key}\", $enumName.class);\n\n"

        result += "    public enum $enumName implements EnumWithKey {\n        "

        val iterator = o.items.iterator()

        while(iterator.hasNext()) {
            val item = iterator.next()

            if(item.key != null && !usedKeys.add(item.key)) {
                throw IllegalStateException("Duplicate keys \"${item.key}\" were found in settings.json!" +
                        "\nYou have to remove one of them for app to work properly.")
            }

            result += item.key!!.uppercase(Locale.ENGLISH) + "(\"${item.key}\")"

            if(iterator.hasNext()) {
                result += ", "
            }
        }

        result += ";\n\n        private final String key;\n\n        ${enumName}(String key) {\n"
        result += "            this.key = key;\n        }\n\n        @Override\n        public String getKey() {\n"
        result += "            return key;\n        }\n    }\n"

        return result
    }

    if(listOf("action", "select", "multiselect").contains(o.type)) {
        return result + "String ${o.key!!.uppercase(Locale.ENGLISH)} = \"${o.key}\";\n"
    }

    when(o.type) {
        "string" -> result += "String"
        "integer", "select_integer" -> result += "Integer"
        "boolean", "screen_boolean" -> result += "Boolean"
    }

    result += "Setting ${o.key!!.uppercase(Locale.ENGLISH)} = () -> \"${o.key}\";\n"
    return result
}

fun collectKeys(from: Setting, usedKeys: MutableSet<String>): String {
    if(from.key != null && !usedKeys.add(from.key)) {
        throw IllegalStateException("Duplicate keys \"${from.key}\" were found in settings.json!" +
                "\nYou have to remove one of them for app to work properly.")
    }

    val builder = StringBuilder()

    when(from.type) {
        "screen" -> {
            if(from.items != null) {
                for(item in from.items) {
                    builder.append(collectKeys(item, usedKeys))
                }
            }
        }

        "string", "integer", "boolean", "action",
        "select", "select_integer", "multiselect" -> builder.append(formatKey(from, usedKeys))
    }

    return builder.toString()
}

data class Setting(val key: String?, val type: String?, val items: List<Setting>?)

@OptIn(ExperimentalStdlibApi::class)
fun generateSettingsClass(dir: File) {
    val settings = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter<Setting>().fromJson(
        file("$projectDir/src/main/assets/settings.json").readText())!!

    val builder = StringBuilder()
        .append("package com.mrboomdev.awery.generated;\n")
        .append("\n")
        .append("import com.mrboomdev.awery.app.data.settings.NicePreferences.*;\n")
        .append("\n")
        .append("/**\n")
        .append(" * Auto-generated class created during the compilation. Please, do not edit it.\n")
        .append(" * @author MrBoomDev\n")
        .append(" */\n")
        .append("public class AwerySettings {\n")
        .append(collectKeys(settings, HashSet()))
        .append("}")

    File(dir, "AwerySettings.java").writeText(builder.toString())
}

tasks.register("generateClasses") {
    file("$projectDir/awery_gen/main/java/com/mrboomdev/awery/generated").apply {
        mkdirs()
        generateSettingsClass(this)
    }
}

tasks["preBuild"].dependsOn(tasks["generateClasses"])