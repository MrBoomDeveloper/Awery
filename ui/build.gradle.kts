plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
	jvmToolchain(17)
}

dependencies {
	implementation(project(":ext"))
	implementation(compose.material)
	implementation(compose.material3)
	implementation(libs.coil.compose)
}