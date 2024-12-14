package com.mrboomdev.awery.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class SettingsGenerateTask: DefaultTask() {
	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	@get:InputFiles
	abstract val inputFiles: ListProperty<RegularFile>

	@TaskAction
	fun run() {
		SettingsClassGenerator.generateSettings(
			inputFiles = inputFiles.get().map { it.asFile },
			outputFile = outputFile.get().asFile
		)
	}
}