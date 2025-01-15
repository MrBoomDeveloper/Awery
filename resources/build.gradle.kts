plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
	jvmToolchain(17)
	applyDefaultHierarchyTemplate()
	
	jvm {
		withJava()
	}
	
	sourceSets {
		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.components.resources)
		}
	}
}

compose.resources {
	packageOfResClass = "com.mrboomdev.awery.generated"
	publicResClass = true
}