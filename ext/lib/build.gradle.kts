plugins {
	`maven-publish`
	id("java-library")
	alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17

	withJavadocJar()
	withSourcesJar()
}

dependencies {
	implementation(project(":ext"))
}

group = "com.github.MrBoomDeveloper"
version = "1.0.0"

publishing {
	publications {
		register<MavenPublication>("maven") {
			from(components["java"])
		}
	}
}