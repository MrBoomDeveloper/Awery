package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.showLoadingWindow;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivities;
import static com.mrboomdev.awery.util.NiceUtils.cleanUrl;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.services.BackupService;
import com.mrboomdev.awery.data.db.item.DBRepository;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.io.FileUtil;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java9.util.Objects;

public class IntentHandlerActivity extends AppCompatActivity {
	private static final String TAG = "IntentHandlerActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		try {
			SplashScreen.installSplashScreen(this);
		} catch(Exception e) {
			Log.e(TAG, "Failed to install an splash screen!", e);
		}

		ThemeManager.apply(this);
		super.onCreate(savedInstanceState);

		var uri = getIntent().getData();

		if(uri == null) {
			toast("No uri was specified!", 1);
			finish();
			return;
		}

		var scheme = uri.getScheme();
		var host = uri.getHost();
		var path = uri.getPath();

		if(scheme != null && scheme.equals("aniyomi")) {
			if(host != null && host.equals("add-repo")) {
				var loadingWindow = showLoadingWindow();
				var repo = cleanUrl(uri.getQueryParameter("url"));
				var manager = ExtensionsFactory.getManager(AniyomiManager.class);

				thread(() -> {
					var dao = getDatabase().getRepositoryDao();
					var repos = dao.getRepositories(manager.getId());

					if(find(repos, item -> Objects.equals(item.url, repo)) != null) {
						runOnUiThread(() -> {
							toast("Repository already exists!");
							loadingWindow.dismiss();
							finish();
						});

						return;
					}

					var dbRepo = new DBRepository(repo, manager.getId());
					dao.add(dbRepo);

					runOnUiThread(() -> {
						toast("Repository added successfully");
						loadingWindow.dismiss();
						finish();
					});
				});
			} else {
				finish();
			}
		} else if(path != null && path.startsWith("/awery/app-login/")) {
			for(var activity : getActivities(LoginActivity.class)) {
				activity.completionUrl = uri.toString();
			}

			var intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			finish();
		} else if(returnWith(FileUtil.getUriFileName(uri), name -> (name != null && name.endsWith(".awerybck")))) {
			new DialogBuilder(this)
					.setTitle(R.string.restore_backup)
					.setMessage("Are you sure want to restore an saved backup? All your current data will be erased!")
					.setCancelable(false)
					.setNegativeButton(R.string.cancel, dialog -> finish())
					.setPositiveButton(R.string.confirm, dialog -> {
						var restoreIntent = new Intent(this, BackupService.class);
						restoreIntent.setAction(BackupService.ACTION_RESTORE);
						restoreIntent.setData(uri);
						startService(restoreIntent);
						dialog.dismiss();
					}).show();
		} else {
			toast("Unknown intent!", 1);
			finish();
		}
	}
}