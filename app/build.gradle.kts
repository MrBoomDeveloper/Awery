import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.app)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm("desktop")

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.JVM_1_8
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core)
                implementation(projects.ui)
                implementation(projects.data)
                implementation(projects.extension.loaders)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.splashscreen)
            implementation(libs.compose.activity)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

android {
    namespace = properties["awery.packageName"].toString()
    compileSdk = properties["awery.sdk.target"].toString().toInt()

    defaultConfig {
        applicationId = namespace
        targetSdk = properties["awery.sdk.target"].toString().toInt()
        minSdk = properties["awery.sdk.min"].toString().toInt()
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            versionNameSuffix = "-release"
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        dataBinding = false
        mlModelBinding = false
        prefab = false
        renderScript = false
        shaders = false
        viewBinding = false
        compose = true
    }

    flavorDimensions += listOf("platform")

    productFlavors {
        register("mobile") {
            isDefault = true
            dimension = "platform"
        }

        register("tv") {
            dimension = "platform"
        }
    }

    @Suppress("UnstableApiUsage")
    androidResources {
        generateLocaleConfig = true
        localeFilters += rootProject.projectDir.resolve(
            "resources/src/commonMain/composeResources"
        ).list()
            .filter { it.startsWith("values") }
            .map {
                if(it == "values") "en"
                else it.substringAfter("values-")
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

compose.desktop {
    application {
        mainClass = "com.mrboomdev.awery.MainKt"

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