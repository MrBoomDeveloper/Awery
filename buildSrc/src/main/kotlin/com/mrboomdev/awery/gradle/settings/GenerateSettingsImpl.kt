package com.mrboomdev.awery.gradle.settings

import com.mrboomdev.awery.gradle.util.isNull
import com.mrboomdev.awery.gradle.util.items
import com.mrboomdev.awery.gradle.util.textContent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import java.io.File
import java.util.Locale

private const val packageName = "com.mrboomdev.awery.generated"

internal object GenerateSettingsImpl {
	
	fun generate(task: GenerateSettingsTask) {
		task.outputDirectory.also { output ->
			val path = packageName.replace(".", "/")
			val files = task.inputFiles.get().map { it.asFile }
			createKotlin(files, output.file("kotlin/$path/${task.className.get()}.kt").get().asFile)
		}
	}
	
	@Suppress("JSON_FORMAT_REDUNDANT")
	@OptIn(ExperimentalSerializationApi::class)
	private fun createKotlin(inputFiles: List<File>, outputFile: File) {
		outputFile.parentFile.mkdirs()
		
		outputFile.writeText(buildString {
			append("package $packageName\n")
			append("\n")
			append("// This class has been auto-generated, so please, don't edit it.\n")
			append("object ")
			append(outputFile.nameWithoutExtension)
			append(" {\n")
			
			for(file in inputFiles) {
				val text = file.readText()
				
				val settings = Json {
					decodeEnumsCaseInsensitive = true
					isLenient = true
					ignoreUnknownKeys = true
				}.parseToJsonElement(text) as JsonObject
				
				appendSetting(file.nameWithoutExtension, settings, this)
			}
			
			append("}")
		})
	}

	private fun String.camelCase(): String {
		var result = ""

		for(s in split("_")) {
			result += Character.toUpperCase(s.toCharArray()[0])

			if(s.length > 1) {
				result += s.substring(1, s.length).lowercase(Locale.ENGLISH)
			}
		}

		return result
	}

	private fun constructSetting(
		originalFile: String,
		setting: JsonObject, 
		output: StringBuilder
	) {
		with(output) {
			append("GeneratedSetting.")
			
			append(setting["type"]!!.textContent!!.let {
				it[0].uppercase() + it.substring(1)
			})
			
			if(!setting["value"].isNull) {
				append(".NotNull")
			}

			append("(\"")
			append(setting["key"]!!.textContent)
			append("\"")
			
			when(setting["type"]?.textContent?.lowercase()) {
				"screen" -> {
					append(", arrayOf(")
					
					setting["items"]?.items?.forEach { item ->
						require(item is JsonObject) { "An item is required to be an object!" }
						
						if(!item["key"].isNull && !item["type"].isNull) {
							constructSetting(originalFile, item, output)
						}
						
						output.append(", ")
					}
					
					append(")")
				}
				
				"select" -> {
					append(", ")
					append(setting["key"]!!.textContent!!.camelCase())
					append("Value::class.java")
				}
			}
			
			when(val it = setting["value"]) {
				is JsonPrimitive -> {
					append(", ")
					
					when(setting["type"]!!.textContent!!.lowercase()) {
						"string" -> {
							append("\"")
							append(it.textContent!!)
							append("\"")
						}
						
						"select" -> {
							append(setting["key"]!!.textContent!!.camelCase())
							append("Value.")
							
							if(it.textContent?.get(0)?.isDigit() == true) {
								append("NUMBER_")
							}
							
							append(it.textContent!!.uppercase())
						}
						
						"boolean" -> append(it.textContent!!.toBoolean())
						"integer" -> append(it.textContent!!.toInt())
						else -> throw UnsupportedOperationException("Setting type \"${setting["type"]}\" doesn't support default values!")
					}
				}
				
				JsonNull, null -> {}
				else -> throw UnsupportedOperationException("Unsupported default value type at \"${setting["key"]}\"!")
			}
			
			append(")")
		}
	}

	private fun appendSetting(originalFile: String, setting: JsonObject, output: StringBuilder) {
		if(!setting["key"].isNull && !setting["type"].isNull) {
			with(output) {
				append("\tval ")
				append(setting["key"]!!.textContent!!.uppercase())
				append(" = ")
				constructSetting(originalFile, setting, output)
				append("\n")

				if(setting["type"]?.textContent?.lowercase() == "select" && !setting["items"].isNull) {
					append("\tenum class ")
					append(setting["key"]!!.textContent!!.camelCase())
					append("Value(val key: String) { ")

					for(item in setting["items"]!!.jsonArray) {
						require(item is JsonObject) { "An item is required to be an object!" }
						
						if(item["key"]?.textContent?.get(0)?.isDigit() == true) {
							append("NUMBER_")
						}

						append(item["key"]!!.textContent!!.uppercase())
						append("(\"")
						append(item["key"]!!.textContent)
						append("\"), ")
					}

					append(" }\n")
				}
			}
		}
		
		setting["items"]?.items?.forEach { item ->
			require(item is JsonObject) { "An item is required to be an object!" }
			appendSetting(originalFile, item, output)
		}
	}
}