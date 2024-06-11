package com.mrboomdev.awery.ui.activity.setup;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.ScreenSetupBinding;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.activity.SplashActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;

public class SetupActivity extends AppCompatActivity {
	public static final String EXTRA_STEP = "step";
	private static final int STEP_WELCOME = 0;
	private static final int STEP_FINISH = 1;
	private static final int STEP_TEMPLATE = 2;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		super.onCreate(savedInstanceState);

		var binding = ScreenSetupBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		switch(getIntent().getIntExtra("step", 0)) {
			case STEP_WELCOME -> {
				binding.restoreButton.setOnClickListener(v -> toast("This functionality isn't done yet."));

				binding.continueButton.setOnClickListener(v ->
						startStep(STEP_FINISH));
			}

			case STEP_TEMPLATE -> {
				// TODO
			}

			case STEP_FINISH -> {
				binding.konfetti.start(
						createParty(250, 200, 0, 360, 0, .5),
						createParty(275, 160, 300, 200, 150, .4),
						createParty(275, 160, 300, 200, 330, .6));

				binding.title.setText("We've done!");
				binding.message.setText("Now you can go watch your favourite shows. We hope you enjoy this app! If you want, you can send us a review with what you liked :)");

				binding.restoreButton.setText(R.string.back);
				binding.restoreButton.setOnClickListener(v -> finish());

				binding.continueButton.setText("Finish");
				binding.continueButton.setOnClickListener(v -> {
					var intent = new Intent(this, SplashActivity.class);
					startActivity(intent);

					for(var activity : getActivities(SetupActivity.class)) {
						activity.finish();
					}
				});
			}
		}
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
				.shapes(List.of(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
				.setSpeedBetween(0, 30)
				.position(new Position.Relative(x, .3))
				.build();
	}
}