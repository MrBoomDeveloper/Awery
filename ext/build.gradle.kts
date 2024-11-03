plugins {
	id("java-library")
	id("org.jetbrains.kotlin.jvm")
	kotlin("plugin.serialization") version "2.0.20"
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
	api(libs.kotlinx.coroutines.core)
	api(libs.kotlinx.serialization.json)
}