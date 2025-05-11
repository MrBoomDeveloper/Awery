plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.compose)
	alias(libs.plugins.compose.compiler)
}

kotlin {
	applyDefaultHierarchyTemplate()
	jvm()
	
	sourceSets {
		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.components.resources)
		}
	}
}

compose.resources {
	packageOfResClass = "com.mrboomdev.awery.resources"
	publicResClass = true
}