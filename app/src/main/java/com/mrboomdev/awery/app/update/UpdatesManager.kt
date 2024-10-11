package com.mrboomdev.awery.app.update

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.app.App.getMoshi
import com.mrboomdev.awery.app.App.showLoadingWindow
import com.mrboomdev.awery.app.App.toast
import com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.app.CrashHandler.CrashReport
import com.mrboomdev.awery.util.ContentType
import com.mrboomdev.awery.util.NiceUtils
import com.mrboomdev.awery.util.exceptions.CancelledException
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.util.extensions.formatFileSize
import com.mrboomdev.awery.util.extensions.removeIndent
import com.mrboomdev.awery.util.extensions.startActivityForResult
import com.mrboomdev.awery.util.io.HttpClient.download
import com.mrboomdev.awery.util.io.HttpClient.fetch
import com.mrboomdev.awery.util.io.HttpRequest
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.squareup.moshi.Json
import com.squareup.moshi.adapter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalStdlibApi::class)
object UpdatesManager {
	private const val TAG = "UpdatesManager"

	private val UPDATES_ENDPOINT = ("https://api.github.com/repos/"
			+ BuildConfig.UPDATES_REPOSITORY
			+ "/releases"
			+ (if(BuildConfig.CHANNEL != UpdatesChannel.BETA) "/latest" else ""))

	fun showUpdateDialog(context: Activity, update: Update) {
		runOnUiThread {
			DialogBuilder(context)
				.setTitle("Update available!")
				.setMessage("""
					${update.title}
					Size: ${update.size.formatFileSize()}
	
					${update.body}
				""".trim().removeIndent())
				.setNeutralButton("Dismiss") { it.dismiss() }
				.setPositiveButton("Install") { dialog ->
					val window = showLoadingWindow()

					CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
						Log.e(TAG, "Failed to download an update!", t)
						window.dismiss()

						CrashHandler.showErrorDialog(
							CrashReport.Builder()
								.setTitle("Failed to download an update")
								.setThrowable(t)
								.build())
					}).launch {
						val file = HttpRequest(update.fileUrl).download(
							File(context.cacheDir, "download/app_update.apk"))

						context.startActivityForResult(Intent(Intent.ACTION_VIEW).apply {
							putExtra(Intent.EXTRA_RETURN_RESULT, true)
							putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
							putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
							addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

							setDataAndType(FileProvider.getUriForFile(
								context, BuildConfig.FILE_PROVIDER, file
							), ContentType.APK.mimeType)
						}, { resultCode, _ ->
							window.dismiss()

							if(resultCode == Activity.RESULT_FIRST_USER) {
								toast("Failed to install an update :(")
							} else if(resultCode == Activity.RESULT_OK) {
								toast("Updated successfully!")
								dialog.dismiss()
							}
						})

						window.dismiss()
					}
				}.show()
		}
	}

	suspend fun fetchLatestAppUpdate(): Update {
		val response = HttpRequest(UPDATES_ENDPOINT).setHeaders(mapOf(
			"Accept" to "application/vnd.github+json",
			"X-GitHub-Api-Version" to "2022-11-28"
		)).fetch()

		if(response.statusCode != 200) {
			throw ZeroResultsException("No releases was found!")
		}

		val release = when(BuildConfig.CHANNEL!!) {
			UpdatesChannel.STABLE, UpdatesChannel.ALPHA -> getMoshi()
				.adapter<GitHubRelease>()
				.fromJson(response.text)!!

			UpdatesChannel.BETA -> getMoshi()
				.adapter<List<GitHubRelease>>()
				.fromJson(response.text)!!
				.find { it.prerelease }!!
		}

		checkVersion(release)

		return release.assets
			.find {
				it.name.contains(
					when(BuildConfig.CHANNEL) {
						UpdatesChannel.STABLE -> "-stable-"
						UpdatesChannel.BETA -> "-beta-"
						UpdatesChannel.ALPHA -> "-alpha-"
					}
				) && it.name.endsWith(".apk")
			}!!.let { asset ->
				Update(release.name, release.body, asset.size, asset.browserDownloadUrl)
			}
	}

	private fun parseAlphaVersion(full: String): String {
		val prodIndex = full.indexOf("-stable-")
		if(prodIndex != -1) return full.substring(0, prodIndex)

		val betaIndex = full.indexOf("-beta-")
		if(betaIndex != -1) return full.substring(0, betaIndex)

		val alphaIndex = full.indexOf("-alpha-")
		if(alphaIndex != -1) return full.substring(0, alphaIndex)

		throw IllegalStateException("Didn't found the version flavor. Can't decide how to parse an version. $full")
	}

	private fun checkVersion(release: GitHubRelease) {
		if(BuildConfig.CHANNEL == UpdatesChannel.ALPHA) {
			// We cannot compare semantically versions because they do have random commit hashes in it
			// so instead: did we receive any different version from the server? Then this is a new one!

			if(parseAlphaVersion(BuildConfig.VERSION_NAME) == parseAlphaVersion(release.tagName)) {
				throw CancelledException("You're using the latest version already!")
			}

			return
		}

		val compared = NiceUtils.compareVersions(
			NiceUtils.parseVersion(release.tagName),
			NiceUtils.parseVersion(BuildConfig.VERSION_NAME)
		)

		if(compared <= 0) {
			throw CancelledException("You're using the latest version already!")
		}
	}

	data class Update(
		val title: String,
		val body: String,
		val size: Long,
		val fileUrl: String)

	private data class GitHubRelease(
		@Json(name = "tag_name") val tagName: String,
		val assets: List<GitHubReleaseAsset>,
		val name: String,
		val body: String,
		val prerelease: Boolean)

	private data class GitHubReleaseAsset(
		@Json(name = "browser_download_url") val browserDownloadUrl: String,
		val name: String,
		val size: Long)
}