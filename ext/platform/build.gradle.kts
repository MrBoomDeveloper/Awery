import com.mrboomdev.awery.gradle.util.toXmlString
import com.vanniktech.maven.publish.SonatypeHost

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.maven.publish)
}

group = "ru.mrboomdev.awery"
version = properties["awery.ext.version"].toString()

kotlin {
	jvmToolchain(17)
}

dependencies {
	api(projects.ext)
}

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