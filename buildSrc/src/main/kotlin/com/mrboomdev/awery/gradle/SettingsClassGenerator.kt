package com.mrboomdev.awery.gradle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import java.io.File
import java.util.Locale

object SettingsClassGenerator {

	@Suppress("MemberVisibilityCanBePrivate")
	val Project.generatedSettingsDir: File
		get() = file("$projectDir/generatedSettings/main/kotlin")

	@Suppress("JSON_FORMAT_REDUNDANT")
	@OptIn(ExperimentalSerializationApi::class)
	fun generateSettings(
		inputFiles: Collection<File>,
		outputFile: File
	) {
		outputFile.apply {
			parentFile.mkdirs()
		}.writeText(buildString {
			append("package com.mrboomdev.awery\n")
			append("\n")
			append("// This class has been auto-generated, so please, don't edit it.\n")
			append("@Suppress(\"ClassName\")\n")
			append("object AwerySettings {\n")

			for(file in inputFiles) {
				val settings = Json {
					decodeEnumsCaseInsensitive = true
					isLenient = true
					ignoreUnknownKeys = true
				}.decodeFromString<Setting>(file.readText())

				appendSetting(settings, this)
				append("\n")
			}

			append("}")
		})
	}

	private fun String.toCamelCase(): String {
		var result = ""

		for(s in split("_")) {
			result += Character.toUpperCase(s.toCharArray()[0])

			if(s.length > 1) {
				result += s.substring(1, s.length).lowercase(Locale.ENGLISH)
			}
		}

		return result
	}

	private fun constructSetting(setting: Setting, output: StringBuilder) {
		with(output) {
			append("GeneratedSetting.")

			append(when(setting.type!!.lowercase()) {
				"screen" -> "Screen"
				"action" -> "Action"
				"integer" -> "Integer"
				"string" -> "String"
				"boolean" -> "Boolean"

				// If we don't declare any items at compile-time, then we can't generate any type-safe enums,
				// so instead use regular String in such cases.
				"select" -> if(setting.items != null) "Select" else "String"

				else -> throw IllegalArgumentException(
					"Unsupported setting type \"${setting.type}\" at \"${setting.key}\"!")
			})

			append("(\"")
			append(setting.key)
			append("\"")

			if(setting.type.lowercase() == "screen") {
				append(", arrayOf(")

				if(setting.items != null) {
					for(item in setting.items) {
						if(item.key != null && item.type != null) {
							constructSetting(item, output)
						}

						output.append(", ")
					}
				}

				append(")")
			}

			if(setting.type.lowercase() == "select") {
				append(", ")
				append(setting.key!!.toCamelCase())
				append("Value::class.java")
			}

			append(")")
		}
	}

	private fun appendSetting(setting: Setting, output: StringBuilder) {
		if(setting.key != null && setting.type != null) {
			with(output) {
				append("\tval ")
				append(setting.key.uppercase())
				append(" = ")
				constructSetting(setting, output)
				append("\n")

				if(setting.type.lowercase() == "select" && setting.items != null) {
					append("\tenum class ")
					append(setting.key.toCamelCase())
					append("Value(val key: String) { ")

					for(item in setting.items) {
						if(item.key?.get(0)?.isDigit() == true) {
							append("NUMBER_")
						}

						append(item.key!!.uppercase())
						append("(\"")
						append(item.key)
						append("\"), ")
					}

					append(" }\n")
				}
			}
		}

		if(setting.items != null) {
			for(item in setting.items) {
				appendSetting(item, output)
			}
		}
	}

	@Serializable
	private data class Setting(
		val key: String? = null,
		val type: String? = null,
		val items: List<Setting>? = null
	)
}