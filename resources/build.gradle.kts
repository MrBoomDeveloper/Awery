
import com.mrboomdev.awery.gradle.GenerateConfigurationsTask
import com.mrboomdev.awery.gradle.generatedConfigurationsDirectory
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
		commonMain {
			kotlin.srcDir("$generatedConfigurationsDirectory/kotlin")
			
			dependencies {
				implementation(compose.runtime)
				implementation(compose.foundation)
				implementation(compose.components.resources)
			}
		}
	}
}

compose.resources {
	packageOfResClass = "com.mrboomdev.awery.generated"
	publicResClass = true
}

tasks.register<GenerateConfigurationsTask>("generateConfigurations") {
	inputDirectory = layout.projectDirectory.dir("src/commonMain/composeResources")
}.let { generateConfigurations ->
	tasks.withType<KotlinCompile>().configureEach { 
		dependsOn(generateConfigurations) 
	}
}