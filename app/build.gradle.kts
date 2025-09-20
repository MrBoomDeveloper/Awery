import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(composeLibs.plugins.multiplatform)
    alias(composeLibs.plugins.compiler)
    alias(composeLibs.plugins.hotReload)
    alias(androidLibs.plugins.app)
}

fun getLocales(): Collection<String> {
    return rootProject.projectDir.resolve(
        "resources/src/commonMain/composeResources"
    ).list()!!
        .filter { it.startsWith("values") }
        .map {
            if(it == "values") "en"
            else it.substringAfter("values-")
        }
}

val generateAndroidLocaleConfig by tasks.registering {
    val outputDir = layout.projectDirectory.dir("src/androidMain/res/xml")
    val outputFile = outputDir.file("awery_generated_locales_config.xml")
    val locales = getLocales()

    // For task caching
    inputs.property("locales", locales)
    outputs.file(outputFile)

    doLast {
        outputDir.asFile.mkdirs()
        outputFile.asFile.writeText(buildString {
            appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
            appendLine("""<locale-config xmlns:android="http://schemas.android.com/apk/res/android">""")

            for(locale in locales) {
                appendLine("""    <locale android:name="$locale" />""")
            }

            appendLine("""</locale-config>""")
        })
    }
}

kotlin {
    jvm("desktop")

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.JVM_11
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core)
                implementation(projects.ui)
                implementation(projects.extension.loaders)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.filekit.coil)
                implementation(composeLibs.coil.compose)

                // IDK why, but gradle don't want to sync
                // if these projects aren't used here ._.
                implementation(projects.data)
                implementation(projects.resources)
                implementation(projects.extension.sdk)
                implementation(projects.extension.loaders.androidCompat)
            }
        }

        androidMain.dependencies {
            implementation(androidLibs.core)
            implementation(androidLibs.appcompat)
            implementation(androidLibs.splashscreen)
            implementation(androidLibs.compose.activity)
            implementation(androidLibs.material)
            implementation("androidx.work:work-runtime-ktx:2.10.4")
        }

        val desktopMain by getting {
            dependencies {
                implementation(composeLibs.navigation.jetpack)
                implementation(libs.kotlinx.coroutines.desktop)

                implementation("org.jetbrains.jewel:jewel-int-ui-standalone:0.30.0-252.26252")
                implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window:0.30.0-252.26252")
                
                implementation(composeLibs.desktop.get().let { 
                    "${it.group}:${it.name}:${it.version}"
                }) {
                    exclude(group = "org.jetbrains.compose.material")
                }

                // Native dialogs
                implementation("com.github.milchreis:uibooster:1.21.1")
                implementation("com.formdev:flatlaf-intellij-themes:3.6")
                
                // Just let's fix it. This is very fucked up.
                runtimeOnly("org.jetbrains.compose.ui:ui-util-desktop:1.10.0-alpha01")

                // For some fucking reason skiko isn't loaded by default
                val osName = System.getProperty("os.name")
                val osArch = System.getProperty("os.arch")

                val targetOs = when {
                    osName == "Mac OS X" -> "macos"
                    osName.startsWith("Win") -> "windows"
                    osName.startsWith("Linux") -> "linux"
                    else -> throw UnsupportedOperationException("Unsupported platform $osName!")
                }

                val targetArch = when(osArch) {
                    "x86_64", "amd64" -> "x64"
                    "aarch64" -> "arm64"
                    else -> throw UnsupportedOperationException("Unsupported cpu acrhitecture $osArch!")
                }

                runtimeOnly("org.jetbrains.skiko:skiko-awt-runtime-$targetOs-$targetArch:0.9.4.2")
            }
        }
    }
}

android {
    namespace = "com.mrboomdev.awery"
    compileSdk = properties["awery.sdk.target"].toString().toInt()

    defaultConfig {
        versionName = properties["awery.app.versionName"].toString()
        versionCode = properties["awery.app.versionCode"].toString().toInt()
        
        targetSdk = properties["awery.sdk.target"].toString().toInt()
        minSdk = properties["awery.sdk.min"].toString().toInt()
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["APP_NAME"] = "Awery Debug"
        }

        release {
            versionNameSuffix = "-release"
            manifestPlaceholders["APP_NAME"] = "Awery"
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        aidl = false
        dataBinding = false
        mlModelBinding = false
        prefab = false
        renderScript = false
        shaders = false
        viewBinding = false
        compose = true

        // Used to check whatever we are in the debug build or not
        buildConfig = true
    }

    @Suppress("UnstableApiUsage")
    androidResources {
        localeFilters += getLocales()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "com.mrboomdev.awery.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Msi)
            packageName = "com.mrboomdev.awery"
            packageVersion = properties["awery.app.versionName"].toString()

            windows {
                console = true
                perUserInstall = true
                menu = true
                menuGroup = "Awery"
                includeAllModules = true
                upgradeUuid = "6a7f9795-c323-4c10-a73a-55ac5506af01"
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn(generateAndroidLocaleConfig)
}