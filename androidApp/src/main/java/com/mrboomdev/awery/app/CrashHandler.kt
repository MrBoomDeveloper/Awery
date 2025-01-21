package com.mrboomdev.awery.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.CrashHandler
import com.mrboomdev.awery.platform.android.AndroidGlobals
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.util.exceptions.OkiThrowableMessage
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.fixAndShow
import com.mrboomdev.awery.util.extensions.toChooser
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.utils.activity
import java.io.File

@Deprecated("Use com.mrboomdev.awery.platform.CrashHandler instead!")
object CrashHandler {

	private val crashLogsDirectory by lazy {
		File(AndroidGlobals.applicationContext.filesDir, "tombstones")
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
	
	private val crashFiles: List<File>
		get() = crashLogsDirectory.listFiles()?.sortedBy { it.lastModified() }?.reversed() ?: emptyList()

	fun showDialogIfCrashHappened(
		context: Context,
		continuationCallback: () -> Unit = {}
	) {
		crashFiles.apply {
			if(isEmpty()) {
				continuationCallback()
				return@showDialogIfCrashHappened
			}
			
			forEachIndexed { index, file ->
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
						val mFile = file ?: File((mContext ?: AndroidGlobals.applicationContext).filesDir, "crash_report.txt").apply {
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
								mContext ?: AndroidGlobals.applicationContext, BuildConfig.FILE_PROVIDER, mFile))
						}

						val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
							data = Uri.parse("mailto:")
						}

						val activityInfo = (mContext ?: AndroidGlobals.applicationContext).packageManager.queryIntentActivities(emailIntent, PackageManager.MATCH_ALL).map {
							it.activityInfo.packageName
						}.toTypedArray()

						val intents = activityInfo.map {
							Intent(intent).setPackage(it)
						}

						emailIntent.toChooser("Share Awery crash report").apply {
							putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
						}.also { (mContext ?: AndroidGlobals.applicationContext).startActivity(it) }
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