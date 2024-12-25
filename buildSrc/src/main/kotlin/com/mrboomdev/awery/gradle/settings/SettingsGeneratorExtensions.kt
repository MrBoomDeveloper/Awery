package com.mrboomdev.awery.gradle.settings

import org.gradle.api.Project
import org.gradle.api.file.Directory

val Project.generatedSettingsDirectory: Directory
	get() = layout.buildDirectory.dir("generated/com.mrboomdev.settings").get()

val Project.generatedSettingsKotlinDirectory: Directory
	get() = generatedSettingsDirectory.dir("kotlin")

val Project.generatedSettingsResourcesDirectory: Directory
	get() = generatedSettingsDirectory.dir("resources")