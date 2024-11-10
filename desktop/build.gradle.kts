import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	kotlin("jvm")
	id("org.jetbrains.compose") version "1.6.10"
	id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.mrboomdev.awery.desktop"
version = "1.0.0"

dependencies {
	// Core
	implementation(project(":ext"))
	implementation(kotlin("stdlib-jdk8"))
    implementation(libs.moshi)

	// Ui
	implementation(compose.desktop.currentOs)
	implementation(project(":ui"))

    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.dnsoverhttps)
    implementation(libs.okhttp.brotli)
}

compose.desktop {
	application {
		mainClass = "Main"

		nativeDistributions {
			targetFormats(TargetFormat.Exe, TargetFormat.Deb)
			packageName = "com.mrboomdev.awery"
			packageVersion = "1.0.0"
		}
	}
}