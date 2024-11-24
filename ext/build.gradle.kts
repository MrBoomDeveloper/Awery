import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("java-library")
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
	jvmToolchain(17)
}

dependencies {
	// Coroutines
	api(libs.kotlinx.coroutines.core)

	// Serialization
	api(libs.moshi)
	api(libs.moshi.kotlin)
	api(libs.kotlinx.serialization.json)
}