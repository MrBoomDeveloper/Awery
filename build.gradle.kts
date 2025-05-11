plugins {
	alias(libs.plugins.android.app) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.multiplatform) apply false
	alias(libs.plugins.android.kotlin) apply false
	alias(libs.plugins.compose.compiler) apply false
	alias(libs.plugins.compose) apply false
	alias(libs.plugins.buildkonfig) apply false
}

fun getGitCommitHash(): String {
	return project.providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

ext["gitCommitHash"] = getGitCommitHash().toString()
ext["versionCode"] = properties["awery.app.versionCode"]!!.toString()
ext["versionName"] = "${properties["awery.app.versionName"]!!}-${getGitCommitHash()}"