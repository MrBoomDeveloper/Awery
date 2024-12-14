import com.mrboomdev.awery.gradle.forEachNode
import com.mrboomdev.awery.gradle.toXmlString
import com.mrboomdev.awery.gradle.query
import com.mrboomdev.awery.gradle.queryAll
import com.vanniktech.maven.publish.SonatypeHost

plugins {
	`java-library`
	alias(libs.plugins.kotlin.jvm)
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
	implementation(projects.ext)
}

group = "ru.mrboomdev.awery"
version = properties["awery.ext.version"].toString()

mavenPublishing {
	coordinates(group.toString(), "ext-platform", version.toString())

	pom {
		name = "Awery Extension Platform"
		description = "A bridge between an extension and the client, which may be desktop or mobile."
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

		withXml {
			println("Output pom: " + asElement().toXmlString())
		}
	}

	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, false)
	signAllPublications()
}