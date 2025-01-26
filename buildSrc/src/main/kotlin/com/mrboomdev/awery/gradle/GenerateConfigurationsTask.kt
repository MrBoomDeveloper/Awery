package com.mrboomdev.awery.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

val Project.generatedConfigurationsDirectory: Directory
	get() = layout.buildDirectory.dir("generated/com.mrboomdev.configurations").get()

abstract class GenerateConfigurationsTask: DefaultTask() {
	@get:OutputDirectory
	val outputDirectory: DirectoryProperty = project.objects.directoryProperty().apply {
		set(project.generatedConfigurationsDirectory)
	}
	
	@get:InputDirectory
	abstract val inputDirectory: DirectoryProperty
	
	@TaskAction
	fun run() {
		val path = "kotlin/com.mrboomdev.awery.generated/AweryConfigurations.kt"
		outputDirectory.file(path).get().asFile.apply {
			parentFile!!.mkdirs()
			
			writeText(buildString { 
				append("package com.mrboomdev.awery.generated\n")
				append("\n")
				append("object AweryConfigurations {\n")
				
				val locales = buildList {
					// Default locale. All source strings are being written in it.
					add("en-US")
					
					// Iterate through resources and collect all qualifiers
					inputDirectory.get().asFile.listFiles()?.map { it.name }?.forEach {
						if(it.startsWith("values-")) {
							add(it.substringAfter("-"))
						}
					}
				}
				
				append("\tval locales = listOf(\n")
				for(locale in locales) {
					append("\t\t\"")
					append(locale)
					append("\",\n")
				}
				append("\t)\n")
				
				append("}")
			})
		}
	}
}