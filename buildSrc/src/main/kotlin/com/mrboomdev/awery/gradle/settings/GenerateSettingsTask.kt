package com.mrboomdev.awery.gradle.settings

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

val Project.generatedSettingsDirectory: Directory
	get() = layout.buildDirectory.dir("generated/com.mrboomdev.settings").get()

abstract class GenerateSettingsTask: DefaultTask() {
	@get:OutputDirectory
	val outputDirectory: DirectoryProperty = project.objects.directoryProperty().apply {
		set(project.generatedSettingsDirectory)
	}
	
	@get:InputFiles
	abstract val inputFiles: ListProperty<RegularFile>
	
	@get:Input
	abstract val className: Property<String>
	
	@TaskAction
	fun run() {
		outputDirectory.asFile.get().apply {
			deleteRecursively()
			mkdirs()
		}
		
		GenerateSettingsImpl.generate(this)
	}
}