package com.mrboomdev.awery.platform

import com.mrboomdev.awery.utils.div
import java.io.File
import java.util.Calendar
import java.util.logging.Logger
import kotlin.system.exitProcess

actual object CrashHandler {
	private val logger = Logger.getLogger("CrashHandler")
	
	actual fun setup() {
		Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
			val date = Calendar.getInstance()
			
			val fileName = buildString { 
				append("AweryCrash-[")
				append(date[Calendar.YEAR])
				append(":")
				append(date[Calendar.MONTH])
				append(":")
				append(date[Calendar.DAY_OF_MONTH])
				append("]-[")
				append(date[Calendar.HOUR_OF_DAY])
				append(":")
				append(date[Calendar.MINUTE])
				append(":")
				append(date[Calendar.SECOND])
				append("].txt")
			}
			
			val content = buildString { 
				append("-".repeat(50))
				append("\nThis is an auto-generated crash report made in Awery.\n")
				append("-".repeat(50))
				append("\n\n")
				
				append(throwable.stackTraceToString())
			}
			
			(crashLogsDirectory / fileName).apply {
				parentFile.mkdirs()
				writeText(content)
			}
			
			throwable.printStackTrace()
			logger.severe("Awery just crashed!")
			
			exitProcess(1)
		}
	}
	
	private val crashLogsDirectory: File
		get() = File("logs/crash")
	
	actual val crashLogs: List<File>
		get() = crashLogsDirectory.listFiles()?.sortedBy { it.lastModified() }?.reversed() ?: emptyList()
}