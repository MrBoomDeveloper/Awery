package com.mrboomdev.awery.app.update

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.app.App.Companion.showLoadingWindow
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.generated.Res
import com.mrboomdev.awery.generated.dismiss
import com.mrboomdev.awery.generated.download
import com.mrboomdev.awery.generated.size
import com.mrboomdev.awery.generated.update_available
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.util.ContentType
import com.mrboomdev.awery.util.NiceUtils
import com.mrboomdev.awery.util.extensions.formatFileSize
import com.mrboomdev.awery.util.extensions.removeIndent
import com.mrboomdev.awery.util.io.HttpClient.download
import com.mrboomdev.awery.util.io.HttpClient.fetch
import com.mrboomdev.awery.util.io.HttpRequest
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.utils.startActivityForResult
import com.squareup.moshi.Json
import com.squareup.moshi.adapter
import kotlinx.coroutines.CancellationException
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
				.setTitle(i18n(Res.string.update_available))
				.setMessage("""
					${update.title}
					${i18n(Res.string.size)}: ${update.size.formatFileSize()}
	
					${update.body}
				""".trim().removeIndent())
				.setNeutralButton(i18n(Res.string.dismiss)) { it.dismiss() }
				.setPositiveButton(i18n(Res.string.download)) { dialog ->
					val window = showLoadingWindow()

					CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
						Log.e(TAG, "Failed to download an update!", t)
						window.dismiss()

						CrashHandler.showDialog(
							title = "Failed to download an update",
							throwable = t)
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
		if(BuildConfig.DEBUG) {
			throw ZeroResultsException("Updates in the debug mode are disabled!")
		}

		val response = HttpRequest(UPDATES_ENDPOINT).apply {
			headers["Accept"] = "application/vnd.github+json"
			headers["X-GitHub-Api-Version"] = "2022-11-28"
		}.fetch()

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

		val asset = release.assets.find {
			it.name.contains(
				when(BuildConfig.CHANNEL) {
					UpdatesChannel.STABLE -> "-stable"
					UpdatesChannel.BETA -> "-beta"
					UpdatesChannel.ALPHA -> "-alpha"
				}
			) && it.name.endsWith(".apk")
		}

		if(asset == null) {
			throw ZeroResultsException("No valid assets were found! " +
					"Response text from the server: ${response.text}")
		}

		return Update(release.name, release.body, asset.size, asset.browserDownloadUrl)
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
				throw CancellationException("You're using the latest version already!")
			}

			return
		}

		val compared = NiceUtils.compareVersions(
			NiceUtils.parseVersion(release.tagName),
			NiceUtils.parseVersion(BuildConfig.VERSION_NAME)
		)

		if(compared <= 0) {
			throw CancellationException("You're using the latest version already!")
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