import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(composeLibs.plugins.multiplatform)
	alias(composeLibs.plugins.compiler)
	alias(androidLibs.plugins.library)
}

fun getLocales(): Collection<String> {
	return projectDir.resolve(
		"src/commonMain/composeResources"
	).list()!!
		.filter { it.startsWith("values") }
		.map {
			if(it == "values") "en"
			else it.substringAfter("values-")
		}
}

val generateLocalesConstant by tasks.registering {
	val outputDir = layout.projectDirectory.dir("src/commonMain/kotlin/com/mrboomdev/awery/resources")
	val outputFile = outputDir.file("AweryLocales.awerygen.kt")
	val locales = getLocales()

	// For task caching
	inputs.property("locales", locales)
	outputs.file(outputFile)

	doLast {
		outputDir.asFile.mkdirs()
		outputFile.asFile.writeText(buildString {
			appendLine("""package com.mrboomdev.awery.resources""")
			appendLine()
			appendLine("// AUTO-GENERATED VALUE. DO NOT CHANGE IT DIRECTLY!")
			appendLine("""val AweryLocales = listOf(""")

			for(locale in locales) {
				appendLine("""    "$locale",""")
			}

			appendLine(""")""")
		})
	}
}

kotlin {
	jvmToolchain(properties["awery.java.desktop"].toString().toInt())
	jvm("desktop")

	@Suppress("UnstableApiUsage")
	androidLibrary {
		namespace = "com.mrboomdev.awery.resources"
		compileSdk = properties["awery.sdk.target"].toString().toInt()
		minSdk = properties["awery.sdk.min"].toString().toInt()
		experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

		compilations.all {
			compileTaskProvider.configure {
				compilerOptions {
					jvmTarget = JvmTarget.fromTarget(properties["awery.java.android"].toString())
				}
			}
		}
	}
	
	sourceSets {
		commonMain.dependencies {
			implementation(composeLibs.runtime)
			implementation(composeLibs.foundation)
			api(composeLibs.resources)
		}
	}
}

compose.resources {
	packageOfResClass = "com.mrboomdev.awery.resources"
	generateResClass = always
	publicResClass = true
}

tasks.named("generateComposeResClass") {
	dependsOn(generateLocalesConstant)
}