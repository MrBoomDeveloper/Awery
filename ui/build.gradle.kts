plugins {
	kotlin("jvm")
	id("org.jetbrains.compose") version "1.6.10"
	id("org.jetbrains.kotlin.plugin.compose")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
	implementation(project(":ext"))
	implementation(compose.material)
	implementation(compose.material3)
}