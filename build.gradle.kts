import com.mrboomdev.awery.gradle.ProjectVersion.generateVersionCode
import com.mrboomdev.awery.gradle.ProjectVersion.getGitCommitHash

buildscript {
    repositories {
		mavenCentral()
        google()
    }
}

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

ext["versionCode"] = generateVersionCode().toString()
ext["versionName"] = "${properties["awery.app.version"]}-${getGitCommitHash(project)}"