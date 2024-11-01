buildscript {
    repositories {
        google()
        mavenCentral()
		maven("https://jitpack.io")
    }

    dependencies {
		classpath(libs.moshi)
		classpath(libs.moshi.kotlin)
        //classpath 'com.android.tools.build:gradle:8.5.2'
        //classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.jetbrains.kotlin.android) apply false
	alias(libs.plugins.compose.compiler) apply false
	alias(libs.plugins.jetbrainsCompose) apply false
	alias(libs.plugins.kotlinMultiplatform) apply false
}