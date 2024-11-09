plugins {
	id("java-library")
	id("org.jetbrains.kotlin.jvm")
	id("com.google.devtools.ksp") version "2.0.21-1.0.25"
	kotlin("plugin.serialization") version "2.0.20"
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
	// Preprocessing
	ksp(libs.moshi.kotlin.codegen)

	// Coroutines
	api(libs.kotlinx.coroutines.core)

	// Serialization
	api(libs.moshi)
	api(libs.moshi.kotlin)
	api(libs.kotlinx.serialization.json)
}