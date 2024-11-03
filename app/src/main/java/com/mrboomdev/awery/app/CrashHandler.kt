package com.mrboomdev.awery.app

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.FileProvider
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.google.android.material.textview.MaterialTextView
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.copyToClipboard
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.app.App.Companion.isTv
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLifecycle.Companion.restartApp
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.util.ParserAdapter
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor
import com.mrboomdev.awery.util.exceptions.OkiThrowableMessage
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.setMargin
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.squareup.moshi.adapter
import xcrash.Errno
import xcrash.XCrash
import xcrash.XCrash.InitParameters
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object CrashHandler {
	private const val TAG = "CrashHandler"

	internal fun setupCrashListener(context: Context?) {
		val xCrashParams = InitParameters()
			.enableNativeCrashHandler()
			.enableAnrCrashHandler()
			.enableJavaCrashHandler()
			.setAnrCheckProcessState(!isTv)
			.setJavaDumpNetworkInfo(false)
			.setJavaDumpAllThreads(false)
			.setNativeDumpNetwork(false)
			.setNativeDumpAllThreads(false)
			.setJavaDumpFds(false)
			.setNativeDumpElfHash(false)
			.setAnrDumpFds(false)
			.setNativeDumpFds(false)
			.setNativeDumpMap(false)
			.setAppVersion(BuildConfig.VERSION_NAME)
			.setJavaCallback { s, _ -> handleError(CrashType.JAVA, s) }
			.setNativeCallback { s, _ -> handleError(CrashType.NATIVE, s) }
			.setAnrCallback { s, s1 ->
				Log.e(TAG, "$s\n\n$s1".trimIndent())
				handleError(CrashType.ANR, s)
			}

		val result = when(XCrash.init(context, xCrashParams)) {
			Errno.INIT_LIBRARY_FAILED -> "Failed to initialize XCrash library!"
			Errno.LOAD_LIBRARY_FAILED -> "Failed to load XCrash library!"
			Errno.CONTEXT_IS_NULL -> "XCrash context is null!"
			else -> null
		}

		if(result != null) {
			toast(result, Toast.LENGTH_LONG)
			Log.e(TAG, result)
		}
	}

	private fun handleError(type: CrashType, message: String) {
		toast(
			appContext.getString(
				when(type) {
					CrashType.ANR -> {
						Log.e(TAG, "ANR error has happened. $message")
						R.string.app_not_responding_restart
					}

					CrashType.JAVA -> R.string.app_crash
					CrashType.NATIVE -> R.string.something_terrible_happened
				}
			), 1
		)

		if(type != CrashType.ANR) {
			val crashFile = File(anyContext.filesDir, "crash.txt")

			try {
				if(!crashFile.exists()) crashFile.createNewFile()

				FileWriter(crashFile).use { writer ->
					writer.write(message)
					Log.i(TAG, "Crash file saved successfully: " + crashFile.absolutePath)
				}
			} catch(e: Throwable) {
				Log.e(TAG, "Failed to write crash file!", e)
			}
		}

		restartApp()
	}

	@JvmStatic
	fun showErrorDialog(report: CrashReport) {
		showErrorDialog(getAnyActivity<AppCompatActivity>()!!, report)
	}

	private fun createExpandable(context: Context, message: String?): View {
		val linear = LinearLayoutCompat(context)
		linear.orientation = LinearLayoutCompat.VERTICAL

		val expander = MaterialTextView(context)
		expander.setBackgroundResource(R.drawable.ripple_round_you)
		expander.text = "Click to see the exception"
		expander.isClickable = true
		expander.isFocusable = true
		linear.addView(expander)

		expander.setPadding(expander.dpPx(16f))
		expander.setMargin {
			setMargins(expander.dpPx(-16f))
			bottomMargin = 0
		}

		val content = MaterialTextView(context)
		content.setTextIsSelectable(true)
		content.text = message
		content.visibility = View.GONE
		linear.addView(content)

		expander.setOnClickListener {
			val makeVisible = content.visibility != View.VISIBLE
			expander.text = if(makeVisible) "Click to hide the exception" else "Click to see the exception"
			content.visibility = if(makeVisible) View.VISIBLE else View.GONE
		}

		return linear
	}

	@JvmStatic
	fun showErrorDialog(context: Context, report: CrashReport) {
		runOnUiThread {
			var contentView: View? = null
			check(!(report.throwable != null && report.pohuiStringThrowable != null)) { "You can't use both things!" }

			if(report.throwable != null) {
				val unwrapped = ExceptionDescriptor.unwrap(report.throwable)
				val title = ExceptionDescriptor.getTitle(unwrapped, context)
				val message = ExceptionDescriptor.getMessage(unwrapped, context)

				if(!ExceptionDescriptor.isUnknownException(unwrapped)) {
					if(report.title == null) {
						report.title = title
					}

					if(report.message == null) {
						report.message = message
					}
				}

				contentView = createExpandable(context, message)
			}

			if(report.pohuiStringThrowable != null) {
				contentView = createExpandable(context, report.pohuiStringThrowable)
			}

			if(report.prefix != null) {
				report.message = if(report.message == null) report.prefix
				else """
 	${report.message}
 	
 	${report.prefix}
 	""".trimIndent()
			}

			val builder = DialogBuilder(context)
				.setCancelable(false)
				.setOnDismissListener {
					if(report.file != null) {
						report.file!!.delete()
					}
					if(report.dismissCallback != null) {
						report.dismissCallback!!.run()
					}
				}
				.setNeutralButton(R.string.copy) { dialog: DialogBuilder? -> copyToClipboard(report.message!!) }
				.setNegativeButton(R.string.share) { dialog: DialogBuilder? ->
					val newFile = File(context.filesDir, "crash_report.txt")
					val intent = Intent(Intent.ACTION_SEND)
					intent.setType("*/*")

					try {
						newFile.delete()
						newFile.createNewFile()

						FileWriter(newFile).use { writer ->
							writer.write(report.message)
						}
						intent.putExtra(
							Intent.EXTRA_STREAM, FileProvider.getUriForFile(
								context, BuildConfig.FILE_PROVIDER, newFile
							)
						)
					} catch(e: IOException) {
						Log.e(TAG, "Failed to write a file!", e)
						intent.putExtra(Intent.EXTRA_TEXT, report.message)
					}
					context.startActivity(Intent.createChooser(intent, "Share crash report"))
				}
				.setPositiveButton("OK") { obj: DialogBuilder -> obj.dismiss() }

			if(contentView != null) {
				builder.addView(contentView)
			}

			if(report.title != null) {
				builder.setTitle(report.title!!.trim { it <= ' ' })
			}

			if(report.message != null) {
				builder.setMessage(report.message!!.trim { it <= ' ' })
			}
			builder.show()
		}
	}

	@OptIn(ExperimentalStdlibApi::class)
	fun reportIfCrashHappened(context: Context, dismissCallback: Runnable?) {
		val crashFile = File(context.filesDir, "crash.txt")

		try {
			if(!crashFile.exists()) {
				dismissCallback?.run()

				return
			}

			BufferedReader(FileReader(crashFile)).use { reader ->
				val result = StringBuilder()
				var descriptor: ExceptionDescriptor? = null
				var nextLine: String?
				var message: String? = null

				while((reader.readLine().also { nextLine = it }) != null) {
					result.append(nextLine).append("\n")
				}

				try {
					descriptor = getMoshi(ParserAdapter()).adapter<ExceptionDescriptor>().fromJson(result.toString())!!
				} catch(e: IOException) {
					try {
						val file = File(result.toString().trim { it <= ' ' })

						BufferedReader(FileReader(file)).use { reader1 ->
							val result1 = StringBuilder()
							var nextLine1: String

							while((reader1.readLine().also { nextLine1 = it }) != null) {
								if(nextLine1.contains("ClassLoaderContext parent mismatch.")) continue
								result1.append(nextLine1).append("\n")
							}

							message = result1.toString()
						}
					} catch(ex: Exception) {
						message = result.toString()
					}
				}

				val report = CrashReport.Builder()
					.setTitle(R.string.app_crash)
					.setDismissCallback(dismissCallback)
					.setFile(crashFile)

				if(descriptor != null) {
					report.setPrefix(R.string.please_report_bug_app)
					report.setThrowable(descriptor.throwable)
				}

				if(message != null) {
					report.setPrefix(R.string.please_report_bug_app)
					report.setPohuiStringThrowable(message)
				}

				showErrorDialog(report.build())
			}
		} catch(e: Throwable) {
			Log.e(TAG, "Failed to read a crash file!", e)
			dismissCallback?.run()
		}
	}

	private enum class CrashType {
		ANR, JAVA, NATIVE
	}

	fun showDialogIfCrashHappened(
		context: Context,
		continuationCallback: () -> Unit = {}
	) {

	}

	fun showDialog(
		context: Context? = null,
		title: String? = null,
		message: String? = null,
		messagePrefix: String? = null,
		throwable: Throwable? = null,
		file: File? = null,
		dismissCallback: () -> Unit = {}
	) {
		val oki = throwable?.let { OkiThrowableMessage(it) }

		runOnUiThread {
			DialogBuilder(context ?: getAnyActivity<AppCompatActivity>()).apply {
				setTitle(title ?: oki?.title)

				setMessage(buildString {
					if(messagePrefix != null) {
						append(messagePrefix)
						append("\n\n")
					}

					if(message != null) {
						append(message)
						append("\n\n")
					}

					if(oki != null) {
						append(oki.message)
					}
				})

				setOnDismissListener {
					file?.deleteRecursively()
					dismissCallback()
				}

				setPositiveButton(R.string.ok) {
					it.dismiss()
				}

				setNegativeButton(R.string.share) {

				}

				setNeutralButton(R.string.copy) {
					copyToClipboard(oki?.message ?: message!!)
				}

				show()
			}
		}
	}

	class CrashReport {
		var title: String? = null
		var message: String? = null
		var prefix: String? = null
		var pohuiStringThrowable: String? = null
		var throwable: Throwable? = null
		var file: File? = null
		var dismissCallback: Runnable? = null

		class Builder {
			private val report = CrashReport()

			fun setMessage(message: String?): Builder {
				report.message = message
				return this
			}

			fun setPohuiStringThrowable(t: String?): Builder {
				report.pohuiStringThrowable = t
				return this
			}

			fun setMessage(@StringRes prefix: Int): Builder {
				report.message = anyContext.getString(prefix)
				return this
			}

			fun setPrefix(@StringRes prefix: Int): Builder {
				report.prefix = anyContext.getString(prefix)
				return this
			}

			fun setThrowable(throwable: Throwable?): Builder {
				report.throwable = throwable
				return this
			}

			fun setDismissCallback(dismissCallback: Runnable?): Builder {
				report.dismissCallback = dismissCallback
				return this
			}

			fun setFile(file: File?): Builder {
				report.file = file
				return this
			}

			fun setTitle(title: String?): Builder {
				report.title = title
				return this
			}

			fun setTitle(@StringRes title: Int): Builder {
				report.title = anyContext.getString(title)
				return this
			}

			fun build(): CrashReport {
				return report
			}
		}
	}
}