package com.mrboomdev.awery.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.app.App.Companion.isTv
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLifecycle.Companion.restartApp
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.util.exceptions.OkiThrowableMessage
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.fixAndShow
import com.mrboomdev.awery.util.extensions.toChooser
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.utils.activity
import xcrash.Errno
import xcrash.XCrash
import xcrash.XCrash.InitParameters
import java.io.File

object CrashHandler {
	private const val TAG = "CrashHandler"

	private val crashLogsDirectory by lazy {
		File(appContext.filesDir, "tombstones")
	}

	private enum class CrashType {
		ANR, JAVA, NATIVE
	}

	internal fun setupCrashListener(context: Context) {
		when(XCrash.init(context, InitParameters().apply {
			setAppVersion(BuildConfig.VERSION_NAME)

			// Sometimes exoplayer does throw some native exceptions in the background
			// and XCrash catches it for no reason, so we don't catch any native exceptions.
			disableNativeCrashHandler()

			// This library doesn't check if an ANR has happened
			// properly on Android TV, so we disable it.
			// Also while debugging an ANR may be triggered, so we disable it in the dev build.
			setAnrCheckProcessState(!isTv && !BuildConfig.DEBUG)

			// Crash logs are too long so we do strip all non-relevant dumps.
			setJavaDumpNetworkInfo(false)
			setJavaDumpFds(false)
			setJavaDumpAllThreads(false)

			setAnrDumpFds(false)
			setAnrDumpNetwork(false)

			setNativeDumpFds(false)
			setNativeDumpMap(false)
			setNativeDumpNetwork(false)
			setNativeDumpElfHash(false)
			setNativeDumpAllThreads(false)

			// Logcat? There is only some shit...
			setJavaLogcatMainLines(0)
			setJavaLogcatEventsLines(0)
			setJavaLogcatSystemLines(0)

			setAnrLogcatMainLines(0)
			setAnrLogcatEventsLines(0)
			setAnrLogcatSystemLines(0)

			setNativeLogcatMainLines(0)
			setNativeLogcatEventsLines(0)
			setNativeLogcatSystemLines(0)

			// Setup crash handlers
			setJavaCallback { _, message -> handleError(CrashType.JAVA, message) }
			setNativeCallback { _, message -> handleError(CrashType.NATIVE, message) }
			setAnrCallback { _, message -> handleError(CrashType.ANR, message) }
		})) {
			Errno.INIT_LIBRARY_FAILED -> "Failed to initialize XCrash library!"
			Errno.LOAD_LIBRARY_FAILED -> "Failed to load XCrash library!"
			else -> ""
		}.let {
			if(it.isBlank()) return
			toast(it, Toast.LENGTH_LONG)
			Log.e(TAG, it)
		}
	}

	private fun handleError(type: CrashType, message: String?) {
		toast(i18n(when(type) {
			CrashType.ANR -> {
				Log.e(TAG, "ANR error has happened. $message")
				Res.string.app_not_responding_restart
			}

			CrashType.JAVA -> Res.string.app_crash
			CrashType.NATIVE -> Res.string.something_terrible_happened
		}), 1)

		restartApp()
	}

	@JvmStatic
	@Deprecated(message = "old java shit")
	fun showErrorDialog(report: CrashReport) {
		showErrorDialog(getAnyActivity<AppCompatActivity>()!!, report)
	}

	@JvmStatic
	@Deprecated(message = "old java shit")
	fun showErrorDialog(context: Context, report: CrashReport) {
		showDialog(
			context = context,
			title = report.title,
			message = report.message,
			throwable = report.throwable,
			file = report.file,
			dismissCallback = { report.dismissCallback?.run() },
			messagePrefix = report.prefix
		)
	}

	fun showDialogIfCrashHappened(
		context: Context,
		continuationCallback: () -> Unit = {}
	) {
		val files = crashLogsDirectory.listFiles()?.sortedBy { it.lastModified() }?.reversed()

		if(files.isNullOrEmpty()) {
			continuationCallback()
			return
		}

		files.forEachIndexed { index, file ->
			showDialog(
				context = context,
				title = i18n(Res.string.app_crash),
				messagePrefix = i18n(Res.string.please_report_bug_app),
				file = file,
				dismissCallback = if(index == 0) {{
					crashLogsDirectory.delete()
					continuationCallback()
				}} else {{}}
			)
		}
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
		val activity by lazy { getAnyActivity<AppCompatActivity>() }
		val mContext = context ?: activity

		runOnUiThread {
			DialogBuilder(mContext?.activity ?: activity!!).apply {
				setTitle(title ?: oki?.title)

				setMessage(buildString {
					if(messagePrefix != null) {
						append(messagePrefix)
						append("\n\n")
					}

					if(message != null) {
						append(message.trim())
						append("\n\n")
					}

					if(oki != null) {
						append(oki.message.trim())
					}
				})

				setOnDismissListener {
					file?.deleteRecursively()
					dismissCallback()
				}

				setPositiveButton(i18n(Res.string.ok)) {
					it.dismiss()
				}

				if(file?.exists() == true || message != null || oki != null) {
					setNegativeButton(i18n(Res.string.share)) {
						val mFile = file ?: File((mContext ?: appContext).filesDir, "crash_report.txt").apply {
							delete()
							createNewFile()

							if(message != null) {
								appendText(message)
							}

							if(oki != null) {
								appendText(oki.print())
							}
						}

						val intent = Intent(Intent.ACTION_SEND).apply {
							type = "vnd.android.cursor.dir/email"
							addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
							putExtra(Intent.EXTRA_SUBJECT, "Awery Crashed")
							putExtra(Intent.EXTRA_EMAIL, arrayOf("awery-support@mrboomdev.ru"))
							putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
								mContext ?: appContext, BuildConfig.FILE_PROVIDER, mFile))
						}

						val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
							data = Uri.parse("mailto:")
						}

						val activityInfo = (mContext ?: appContext).packageManager.queryIntentActivities(emailIntent, PackageManager.MATCH_ALL).map {
							it.activityInfo.packageName
						}.toTypedArray()

						val intents = activityInfo.map {
							Intent(intent).setPackage(it)
						}

						emailIntent.toChooser("Share Awery crash report").apply {
							putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
						}.also { (mContext ?: appContext).startActivity(it) }
					}

					setNeutralButton(i18n(Res.string.see_error)) {
						BottomSheetDialog(mContext?.activity ?: activity!!).apply {
							setContentView(NestedScrollView(mContext!!).apply {
								addView(MaterialTextView(mContext).apply {
									setTextIsSelectable(true)
									setPadding(dpPx(16f))

									text = if(file?.exists() == true) {
										file.readText()
									} else (oki?.print() ?: message!!).trim()
								})
							})
						}.fixAndShow()
					}
				}

				show()
			}
		}
	}

	@Deprecated(message = "old java shit")
	class CrashReport {
		var title: String? = null
		var message: String? = null
		var prefix: String? = null
		var throwable: Throwable? = null
		var file: File? = null
		var dismissCallback: Runnable? = null

		class Builder {
			private val report = CrashReport()

			fun setMessage(message: String?): Builder {
				report.message = message
				return this
			}

			fun setThrowable(throwable: Throwable?): Builder {
				report.throwable = throwable
				return this
			}

			fun setPrefix(prefix: String?): Builder {
				report.prefix = prefix
				return this
			}

			fun setTitle(title: String?): Builder {
				report.title = title
				return this
			}

			fun build(): CrashReport {
				return report
			}
		}
	}
}