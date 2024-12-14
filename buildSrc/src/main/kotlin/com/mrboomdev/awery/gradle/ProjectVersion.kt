package com.mrboomdev.awery.gradle

import org.gradle.api.Project

// Note: Please, don't edit it if you don't know what it does
private const val START_VERSION_CODE = 2808
private const val START_MILLIS = 1719658313080L

object ProjectVersion {

	fun getGitCommitHash(project: Project): String {
		return project.providers.exec {
			commandLine("git", "rev-parse", "--short", "HEAD")
		}.standardOutput.asText.get().trim()
	}

	fun generateVersionCode(): Int {
		return (START_VERSION_CODE + (System.currentTimeMillis() - START_MILLIS) / 1000).also {
			if(it.toInt() <= 0 || it >= Int.MAX_VALUE) {
				throw IllegalStateException("We've reached an Integer limit! " +
						"Now Awery 2 must be released! Generated version code: $it")
			}
		}.toInt()
	}
}