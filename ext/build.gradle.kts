import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.vanniktech.maven.publish.SonatypeHost

plugins {
	`java-library`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.maven.publish)
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

group = "ru.mrboomdev.awery"
version = properties["awery.ext.version"].toString()

mavenPublishing {
	coordinates(group.toString(), "ext-lib", version.toString())

	pom {
		name = "Awery Extension Library"
		description = "Core stuff to develop extensions without some fancy stuff."
		url = "https://github.com/MrBoomDeveloper/Awery"
		inceptionYear = "2024"

		licenses {
			license {
				name = "The Apache Licence, Version 2.0"
				url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
				description = url
			}
		}

		developers {
			developer {
				id = "mrboomdev"
				name = "MrBoomDev"
				url = "https://github.com/MrBoomDeveloper"
			}
		}

		scm {
			url = "https://github.com/MrBoomDeveloper/Awery"
			connection = "scm:git:git://github.com/MrBoomDeveloper/Awery.git"
			developerConnection = "scm:git:ssh://git@github.com/MrBoomDeveloper/Awery.git"
		}
	}

	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, false)
	signAllPublications()
}