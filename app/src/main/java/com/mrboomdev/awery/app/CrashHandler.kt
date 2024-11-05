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
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.i18n
import com.mrboomdev.awery.app.App.Companion.isTv
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLifecycle.Companion.restartApp
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.util.exceptions.OkiThrowableMessage
import com.mrboomdev.awery.util.extensions.activity
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.fixAndShow
import com.mrboomdev.awery.util.extensions.setMargin
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
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

			// This library doesn't check if an ANR has happened
			// properly on Android TV, so we disable it.
			setAnrCheckProcessState(!isTv)

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
			Errno.CONTEXT_IS_NULL -> "XCrash context is null!"
			else -> ""
		}.let {
			if(it.isBlank()) return
			toast(it, Toast.LENGTH_LONG)
			Log.e(TAG, it)
		}
	}

	private fun handleError(type: CrashType, message: String?) {
		toast(appContext.getString(when(type) {
			CrashType.ANR -> {
				Log.e(TAG, "ANR error has happened. $message")
				R.string.app_not_responding_restart
			}

			CrashType.JAVA -> R.string.app_crash
			CrashType.NATIVE -> R.string.something_terrible_happened
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
				titleRes = if(files.size == 1) R.string.app_crash else null,
				title = if(files.size == 1) null else "Awery crash report #${index + 1}",
				messagePrefixRes = R.string.please_report_bug_app,
				file = file,
				dismissCallback = {
					crashLogsDirectory.delete()
					continuationCallback()
				}
			)
		}
	}

	fun showDialog(
		context: Context? = null,
		title: String? = null,
		@StringRes titleRes: Int? = null,
		message: String? = null,
		@StringRes messageRes: Int? = null,
		messagePrefix: String? = null,
		@StringRes messagePrefixRes: Int? = null,
		throwable: Throwable? = null,
		file: File? = null,
		dismissCallback: () -> Unit = {}
	) {
		val oki = throwable?.let { OkiThrowableMessage(it) }
		val activity by lazy { getAnyActivity<AppCompatActivity>() }
		val mContext = context ?: activity

		runOnUiThread {
			DialogBuilder(mContext?.activity ?: activity!!).apply {
				setTitle(title ?: titleRes?.let { i18n(it) } ?: oki?.title)

				setMessage(buildString {
					if(messagePrefix != null) {
						append(messagePrefix)
						append("\n\n")
					}

					if(messagePrefixRes != null) {
						append(i18n(messagePrefixRes))
						append("\n\n")
					}

					if(message != null) {
						append(message.trim())
						append("\n\n")
					}

					if(messageRes != null) {
						append(i18n(messageRes))
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

				setPositiveButton(R.string.ok) {
					it.dismiss()
				}

				if(file != null || message != null || messageRes != null) {
					setNegativeButton(R.string.share) {
						val mFile = file ?: File((mContext ?: appContext).filesDir, "crash_report.txt").apply {
							delete()
							createNewFile()
							writeText(message ?: i18n(messageRes!!))
						}

						(mContext ?: appContext).startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
							type = "*/*"
							putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
								mContext ?: appContext, BuildConfig.FILE_PROVIDER, mFile))
						}, "Share crash report"))
					}

					setNeutralButton("See error") {
						BottomSheetDialog(mContext?.activity ?: activity!!).apply {
							setContentView(NestedScrollView(mContext!!).apply {
								addView(MaterialTextView(mContext).apply {
									text = (file?.readText() ?: message ?: i18n(messageRes!!)).trim()
									setTextIsSelectable(true)
									setPadding(dpPx(16f))
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