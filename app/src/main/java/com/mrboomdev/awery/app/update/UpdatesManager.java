package com.mrboomdev.awery.app.update;

import static com.mrboomdev.awery.app.App.showLoadingWindow;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.Lifecycle.startActivityForResult;
import static com.mrboomdev.awery.util.NiceUtils.formatFileSize;
import static java.util.Objects.requireNonNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.CancelledException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpRequest;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.squareup.moshi.Json;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UpdatesManager {
	private static final String TAG = "UpdatesManager";

	private static final String UPDATES_ENDPOINT = "https://api.github.com/repos/"
			+ BuildConfig.UPDATES_REPOSITORY
			+ "/releases"
			+ (BuildConfig.CHANNEL != UpdatesChannel.BETA ? "/latest" : "");

	public static void showUpdateDialog(Update update) {
		var context = requireNonNull(getAnyActivity(AppCompatActivity.class));

		runOnUiThread(() -> new DialogBuilder(context)
				.setTitle("Update available!")
				.setMessage(update.title() + "\nSize: " + formatFileSize(update.size()) + "\n\n" + update.body())
				.setNeutralButton("Dismiss", DialogBuilder::dismiss)
				.setPositiveButton("Install", dialog -> {
					var window = showLoadingWindow();
					var file = new File(context.getCacheDir(), "download/app_update.apk");

					HttpClient.download(new HttpRequest(update.fileUrl()), file).addCallback(new AsyncFuture.Callback<>() {
						@Override
						public void onSuccess(File result) {
							var intent = new Intent(Intent.ACTION_VIEW);
							intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
							intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
							intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getPackageName());
							intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

							intent.setDataAndType(FileProvider.getUriForFile(context,
									BuildConfig.FILE_PROVIDER, result), MimeTypes.APK.toString());

							runOnUiThread(() -> startActivityForResult(context, intent, (resultCode, data) -> {
								window.dismiss();

								if(resultCode == Activity.RESULT_FIRST_USER) {
									toast("Failed to install an update :(");
								} else if(resultCode == Activity.RESULT_OK) {
									toast("Updated successfully!");
									dialog.dismiss();
								}
							}));
						}

						@Override
						public void onFailure(Throwable t) {
							Log.e(TAG, "Failed to download an update!", t);
							window.dismiss();

							CrashHandler.showErrorDialog(new CrashHandler.CrashReport.Builder()
									.setTitle("Failed to download an update")
									.setThrowable(t)
									.build());
						}
					});
				}).show());
	}

	@NonNull
	@Contract(" -> new")
	public static AsyncFuture<Update> getAppUpdate() {
		return HttpClient.fetch(new HttpRequest(UPDATES_ENDPOINT).setHeaders(Map.of(
				"Accept", "application/vnd.github+json",
				"X-GitHub-Api-Version", "2022-11-28"
		))).then(response -> {
			if(response.getStatusCode() != 200) {
				throw new ZeroResultsException("No releases was found!");
			}

			var release = switch(BuildConfig.CHANNEL) {
				case STABLE, ALPHA -> Parser.fromString(GitHubRelease.class, response.getText());

				case BETA -> {
					var releases = Parser.<List<GitHubRelease>>fromString(
							Parser.getAdapter(List.class, GitHubRelease.class), response.getText());

					yield stream(releases).filter(me -> me.prerelease).findFirst()
							.orElseThrow(() -> new ZeroResultsException("No beta versions was found."));
				}
			};

			checkVersion(release);

			var asset = stream(release.assets)
					.filter(me -> me.name.contains(switch(BuildConfig.CHANNEL) {
						case STABLE -> "-stable-";
						case BETA -> "-beta-";
						case ALPHA -> "-alpha-";
					}) && me.name.endsWith(".apk"))
					.findAny().orElseThrow(() -> new ZeroResultsException("No valid files was found!"));

			return new Update(release.name, release.body, asset.size, asset.browserDownloadUrl);
		});
	}

	@NonNull
	private static String parseAlphaVersion(@NonNull String full) {
		var prodIndex = full.indexOf("-stable-");
		if(prodIndex != -1) return full.substring(0, prodIndex);

		var betaIndex = full.indexOf("-beta-");
		if(betaIndex != -1) return full.substring(0, betaIndex);

		var alphaIndex = full.indexOf("-alpha-");
		if(alphaIndex != -1) return full.substring(0, alphaIndex);

		throw new IllegalStateException("Didn't found the version flavor. Can't decide how to parse an version. " + full);
	}

	private static void checkVersion(@NonNull GitHubRelease release) {
		// We cannot compare semantically versions because they do have random commit hashes in it
		// so instead: did we receive any different version from the server? Then this is a new one!
		if(BuildConfig.CHANNEL == UpdatesChannel.ALPHA) {
			if(parseAlphaVersion(BuildConfig.VERSION_NAME).equals(parseAlphaVersion(release.tagName))) {
				throw new CancelledException("You're using the latest version already!");
			}

			return;
		}

		var compared = NiceUtils.compareVersions(
				NiceUtils.parseVersion(release.tagName),
				NiceUtils.parseVersion(BuildConfig.VERSION_NAME));

		if(compared <= 0) {
			throw new CancelledException("You're using the latest version already!");
		}
	}

	public record Update(String title, String body, long size, String fileUrl) {}

	private static class GitHubRelease {
		@Json(name = "tag_name")
		public String tagName;
		public List<GitHubReleaseAsset> assets;
		public String name, body;
		public boolean prerelease;
	}

	private static class GitHubReleaseAsset {
		@Json(name = "browser_download_url")
		public String browserDownloadUrl;
		public String name;
		public long size;
	}
}