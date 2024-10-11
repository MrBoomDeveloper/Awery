package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.App.enableEdgeToEdge;
import static com.mrboomdev.awery.app.App.getDatabase;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.CrashHandler.reportIfCrashHappened;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.mrboomdev.awery.app.AweryLifecycle;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.databinding.ScreenSplashBinding;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.activity.setup.SetupActivity;
import com.mrboomdev.awery.util.async.AsyncFuture;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
	private static final String TAG = "SplashActivity";
	private ScreenSplashBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		SplashScreen.installSplashScreen(this);

		try {
			ThemeManager.apply(this);
		} catch(Exception e) {
			Log.e(TAG, "Failed to apply an theme!", e);
		}

		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		binding = ScreenSplashBinding.inflate(getLayoutInflater());
		binding.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));
		getWindow().setNavigationBarColor(resolveAttrColor(this, android.R.attr.colorBackground));
		setContentView(binding.getRoot());

		binding.status.setText("Checking the database...");

		reportIfCrashHappened(this, () -> thread(() -> {
			try {
				getDatabase().getListDao().getAll();
			} catch(IllegalStateException e) {
				Log.e(TAG, "Database is corrupted!", e);

				CrashHandler.showErrorDialog(this, new CrashHandler.CrashReport.Builder()
						.setTitle("Database is corrupted!")
						.setThrowable(e)
						.setDismissCallback(AweryLifecycle::exitApp)
						.build());

				return;
			}

			if(AwerySettings.SETUP_VERSION_FINISHED.getValue() < SetupActivity.SETUP_VERSION) {
				var intent = new Intent(this, SetupActivity.class);
				startActivity(intent);
				finish();
				return;
			}

			ExtensionsFactory.getInstance().addCallback(new AsyncFuture.Callback<>() {
				@Override
				public void onSuccess(ExtensionsFactory result) {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					finish();
				}

				@Override
				public void onFailure(Throwable t) {
					Log.e(TAG, "Failed to load an ExtensionsFactory!", t);

					CrashHandler.showErrorDialog(SplashActivity.this, new CrashHandler.CrashReport.Builder()
							.setTitle("Failed to load an ExtensionsFactory")
							.setThrowable(t)
							.setDismissCallback(AweryLifecycle::exitApp)
							.build());
				}
			});

			runOnUiThread(this::update);
		}));
	}

	private void update() {
		if(isDestroyed()) return;
		var factory = ExtensionsFactory.getInstanceNow();

		if(factory == null) {
			binding.status.setText("Loading extensions...");
			return;
		}

		long progress = 0, total = 0;

		for(var manager : factory.getManagers()) {
			var managerProgress = manager.getProgress();
			progress += managerProgress.getProgress();
			total += managerProgress.getMax();
		}

		binding.status.setText("Loading extensions " + progress + "/" + total);
		AweryLifecycle.runDelayed(this::update, 100);
	}
}