buildscript {
    repositories {
        google()
        mavenCentral()
		maven("https://jitpack.io")
    }

    dependencies {
		classpath(libs.moshi)
		classpath(libs.moshi.kotlin)
    }
}

plugins {
	alias(libs.plugins.android.app) apply false
	alias(libs.plugins.android.kotlin) apply false
	alias(libs.plugins.compose.compiler) apply false
	alias(libs.plugins.compose) apply false
	alias(libs.plugins.kotlin.multiplatform) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.jvm) apply false
}