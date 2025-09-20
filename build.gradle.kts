import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
	alias(libs.plugins.kotlin.multiplatform) apply false
	alias(libs.plugins.kotlin.serialization) apply false
	alias(libs.plugins.kotlin.ksp) apply false

	alias(libs.plugins.room) apply false
	alias(libs.plugins.buildkonfig) apply false
	alias(libs.plugins.maven.publish) apply false

	alias(composeLibs.plugins.compiler) apply false
	alias(composeLibs.plugins.multiplatform) apply false
	alias(composeLibs.plugins.hotReload) apply false

	alias(androidLibs.plugins.app) apply false
	alias(androidLibs.plugins.library) apply false
}

fun getGitCommitHash(): String {
	return project.providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

ext["gitCommitHash"] = getGitCommitHash()
ext["versionCode"] = properties["awery.app.versionCode"]!!.toString()
ext["versionName"] = "${properties["awery.app.versionName"]!!}-${getGitCommitHash()}"