package com.mrboomdev.awery.ui.activity.setup;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.ScreenSetupBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.activity.SplashActivity;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;

public class SetupActivity extends AppCompatActivity {
	/**
	 * Note: Please increment this value by one every time when a new step is being added
	 */
	public static final int SETUP_VERSION = 1;

	public static final String EXTRA_STEP = "step";
	public static final String EXTRA_FINISH_ON_COMPLETE = "finish_on_complete";
	public static final int STEP_WELCOME = 0;
	public static final int STEP_FINISH = 1;
	public static final int STEP_TEMPLATE = 2;
	private static final int STEP_THEMING = 3;
	private static final int STEP_SOURCES = 4;
	private ScreenSetupBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		super.onCreate(savedInstanceState);

		binding = ScreenSetupBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.continueButton.setOnClickListener(v -> tryToStartNextStep());
		binding.backButton.setOnClickListener(v -> finish());

		switch(getIntent().getIntExtra("step", STEP_WELCOME)) {
			case STEP_WELCOME -> {
				if(AwerySettings.SETUP_VERSION_FINISHED.getValue() != 0) {
					binding.title.setText("Awery has been updated!");
					binding.message.setText("We have some new awesome things to setup!");
					binding.backButton.setVisibility(View.GONE);
				}

				binding.backButton.setText(R.string.restore_backup);
				binding.continueButton.setText(R.string.lets_begin);

				binding.backButton.setOnClickListener(v ->
						toast("This functionality isn't done yet."));
			}

			case STEP_TEMPLATE -> {
				binding.title.setText(R.string.select_template);
				binding.message.setText("Sorry, but this thing isn't done yet.");
			}

			case STEP_FINISH -> {
				binding.konfetti.start(
						createParty(250, 300, 0, 360, 0, .5),
						createParty(275, 225, 300, 200, 150, .4),
						createParty(275, 225, 300, 200, 330, .6));

				binding.title.setText("We've done!");
				binding.message.setText("Now you can go watch your favourite shows. We hope you enjoy this app! If you want, you can send us a review with what you liked :)");
				binding.backButton.setText(R.string.back);
				binding.continueButton.setText(R.string.finish);
			}
		}
	}

	private void tryToStartNextStep() {
		switch(getIntent().getIntExtra(EXTRA_STEP, STEP_WELCOME)) {
			case STEP_TEMPLATE -> {
				// TODO: Check if template was selected

				binding.continueButton.setEnabled(false);
				binding.backButton.setEnabled(false);

				new Thread(() -> {
					var tabsDao = getDatabase().getTabsDao();
					var feedsDao = getDatabase().getFeedsDao();

					var tabs = tabsDao.getAllTabs();

					if(!tabs.isEmpty()) {
						var didDeleted = new AtomicBoolean();

						runOnUiThread(() -> new DialogBuilder(this)
								.setTitle("Existing custom tabs found")
								.setMessage("To use an template we have to delete all your custom tabs with feeds. If you don't want that, then simply select \"None\".")
								.setOnDismissListener(dialog -> {
									if(!didDeleted.get()) {
										binding.backButton.setEnabled(true);
										binding.continueButton.setEnabled(true);
									}
								})
								.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
								.setPositiveButton(R.string.delete, dialog -> {
									didDeleted.set(true);
									dialog.dismiss();

									new Thread(() -> {
										for(var tab : tabs) {
											for(var feed : feedsDao.getAllFromTab(tab.id)) {
												feedsDao.delete(feed);
											}

											tabsDao.delete(tab);
										}

										tryToStartNextStep();
									}).start();
								}).show());

						return;
					}

					runOnUiThread(() -> {
						startNextStep();
						binding.continueButton.setEnabled(true);
						binding.backButton.setEnabled(true);
					});
				}).start();

				return;
			}
		}

		startNextStep();
	}

	private void startNextStep() {
		if(getIntent().getBooleanExtra(EXTRA_FINISH_ON_COMPLETE, false)) {
			setResult(SetupActivity.RESULT_OK);
			finish();
			return;
		}

		int nextStep;

		switch(getIntent().getIntExtra(EXTRA_STEP, STEP_WELCOME)) {
			case STEP_WELCOME -> nextStep = STEP_TEMPLATE;
			case STEP_TEMPLATE -> nextStep = STEP_FINISH;

			case STEP_FINISH -> {
				getPrefs().setValue(AwerySettings.SETUP_VERSION_FINISHED, SETUP_VERSION).saveSync();
				finishAffinity();

				var intent = new Intent(this, SplashActivity.class);
				startActivity(intent);

				return;
			}

			default -> throw new IllegalArgumentException("Unknown step!");
		}

		startStep(nextStep);
	}

	private void startStep(int step) {
		var intent = new Intent(this, SetupActivity.class);
		intent.putExtra(EXTRA_STEP, step);
		startActivity(intent);
	}

	@NonNull
	private Party createParty(int durationMs, int amountPerSec, int delayMs, int spread, int angle, double x) {
		return new PartyFactory(new Emitter(durationMs, TimeUnit.MILLISECONDS).perSecond(amountPerSec))
				.delay(delayMs)
				.spread(spread)
				.angle(angle)
				.timeToLive(4000)
				.shapes(List.of(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
				.setSpeedBetween(0, 30)
				.position(new Position.Relative(x, .3))
				.build();
	}
}