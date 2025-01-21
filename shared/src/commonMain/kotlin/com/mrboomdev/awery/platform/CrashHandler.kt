package com.mrboomdev.awery.platform

import java.io.File

expect object CrashHandler {
	fun setup()
	val crashLogs: List<File>
}