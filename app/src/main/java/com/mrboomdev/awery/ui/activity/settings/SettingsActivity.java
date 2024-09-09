package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.App.enableEdgeToEdge;
import static com.mrboomdev.awery.app.App.isLandscape;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.App.setContentViewCompat;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getContext;
import static com.mrboomdev.awery.util.ArgUtils.getLongExtra;
import static com.mrboomdev.awery.util.ArgUtils.getStringExtra;
import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setImageTintAttr;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.data.AndroidImage;
import com.mrboomdev.awery.app.data.settings.NicePreferences;
import com.mrboomdev.awery.app.data.settings.base.LazySetting;
import com.mrboomdev.awery.app.data.settings.base.ParsedSetting;
import com.mrboomdev.awery.databinding.ScreenSettingsBinding;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.ScreenLauncher {
	public static final String EXTRA_JSON = "item";
	public static final String EXTRA_PAYLOAD_ID = "payload_id";
	private static final String TAG = "SettingsActivity";
	private static final Map<Long, Setting> payloads = new HashMap<>();
	private static final UniqueIdGenerator payloadIdGenerator = new UniqueIdGenerator();
	private static WeakReference<RecyclerView.RecycledViewPool> viewPool;
	private ScreenSettingsBinding binding;
	private EmptyView emptyView;

	private static RecyclerView.RecycledViewPool getViewPool() {
		RecyclerView.RecycledViewPool pool;

		if(viewPool == null || viewPool.get() == null) {
			pool = new RecyclerView.RecycledViewPool();
			viewPool = new WeakReference<>(pool);
		} else {
			pool = viewPool.get();
		}

		return pool;
	}

	public static void openSettingScreen(Context context, Setting item) {
		var id = payloadIdGenerator.getLong();
		payloads.put(id, item);

		var intent = new Intent(context, SettingsActivity.class);
		intent.putExtra(EXTRA_PAYLOAD_ID, id);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var itemJson = getStringExtra(this, EXTRA_JSON);
		var payloadId = getLongExtra(this, EXTRA_PAYLOAD_ID);

		Setting item = null;

		if(payloadId != null) {
			item = payloads.get(payloadId);

			if(item == null) {
				Log.e(TAG, "Failed to find an setting. Probably the system has restarted an app :(");
				finish();
				return;
			}
		}

		if(item == null) {
			if(itemJson == null) {
				item = NicePreferences.getSettingsMap();
			} else {
				try {
					var parsed = requireNonNull(new Moshi.Builder().build()
							.adapter(ParsedSetting.class)
							.fromJson(itemJson));

					parsed.restoreSavedValues();
					item = parsed;
				} catch(IOException e) {
					Log.e(TAG, "Failed to parse settings", e);
					toast("Failed to get settings", 0);
					finish();
					return;
				}
			}
		}

		doIfNotNull(createView(item), view -> {
			view.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));
			setContentViewCompat(this, view);
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(isFinishing()) {
			var payloadId = getIntent().getLongExtra(EXTRA_PAYLOAD_ID, -1);

			if(payloadId != -1) {
				payloads.remove(payloadId);
			}
		}
	}

	private void setupHeader(@NonNull ScreenSettingsBinding binding, @NonNull Setting item) {
		binding.title.setText(item.getTitle());
		binding.actions.removeAllViews();

		if(item.getHeaderItems() != null) {
			for(var headerItem : item.getHeaderItems()) {
				var view = new ImageView(this);
				view.setOnClickListener(v -> headerItem.onClick());
				setPadding(view, dpPx(view, 10));

				view.setForeground(AppCompatResources.getDrawable(this, R.drawable.ripple_circle_white));
				view.setClickable(true);
				view.setFocusable(true);

				if(headerItem.getIcon() instanceof AndroidImage androidImage) {
					androidImage.applyTo(view);
					view.setScaleType(ImageView.ScaleType.FIT_CENTER);

					if(view.getDrawable() instanceof VectorDrawable) {
						setImageTintAttr(view, com.google.android.material.R.attr.colorOnSurface);
					} else {
						view.setImageTintList(null);
					}
				} else {
					view.setImageDrawable(null);
				}

				binding.actions.addView(view, dpPx(binding.actions, 48), dpPx(binding.actions, 48));
				setLeftMargin(view, dpPx(view, 10));
			}
		}

		setOnApplyUiInsetsListener(binding.recycler, insets -> {
			setHorizontalPadding(binding.recycler, insets.left, insets.right);
			return false;
		});
	}

	private void setupReordering(RecyclerView recycler, SettingsAdapter adapter) {
		var directions = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

		new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(directions, 0) {

			@Override
			public int getDragDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current) {
				if(adapter.getItems().get(current.getBindingAdapterPosition()).isDraggable()) {
					return directions;
				}

				return 0;
			}

			@Override
			public boolean onMove(
					@NonNull RecyclerView recyclerView,
					@NonNull RecyclerView.ViewHolder current,
					@NonNull RecyclerView.ViewHolder target
			) {
				var from = current.getBindingAdapterPosition();
				var to = target.getBindingAdapterPosition();

				if(!adapter.getItems().get(from).isDraggableInto(adapter.getItems().get(to))) {
					return false;
				}

				if(!adapter.getItems().get(from).onDragged(from, to)) {
					return false;
				}

				Collections.swap(adapter.getItems(), from, to);
				adapter.notifyItemMoved(from, to);

				return true;
			}

			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

		}).attachToRecyclerView(recycler);
	}

	private void finishLoading(
			@NonNull ScreenSettingsBinding binding,
			@NonNull SettingsAdapter settingsAdapter
	) {
		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		binding.recycler.setAdapter(new ConcatAdapter(config, settingsAdapter));
		setupReordering(binding.recycler, settingsAdapter);
		emptyView.hideAll();
	}

	@Nullable
	private ScreenSettingsBinding createView(@NonNull Setting item) {
		binding = ScreenSettingsBinding.inflate(getLayoutInflater());
		emptyView = new EmptyView(binding.progressIndicator);

		binding.recycler.setRecycledViewPool(getViewPool());
		binding.back.setOnClickListener(v -> finish());

		setOnApplyUiInsetsListener(binding.header, insets -> {
			setTopPadding(binding.header, insets.top + dpPx(binding.header, 8));
			setHorizontalPadding(binding.header, insets.left, insets.right);

			setRightMargin(binding.actions, isLandscape(getContext(binding))
					? dpPx(binding.actions, 18) : 0);

			return true;
		});

		if(item.getItems() != null) {
			if(item.getItems().isEmpty()) {
				finish();
				return null;
			}

			var recyclerAdapter = new SettingsAdapter(item, this) {
				@Override
				public void onEmptyStateChanged(boolean isEmpty) {
					if(isEmpty) {
						emptyView.setInfo("Here's nothing", "Yup, this screen is completely empty. You won't see anything here.");
					} else {
						emptyView.hideAll();
					}
				}
			};

			setupHeader(binding, item);
			finishLoading(binding, recyclerAdapter);
		} else if(item instanceof LazySetting lazySetting) {
			thread(() -> {
				try {
					var screen = lazySetting.getLazySetting();

					runOnUiThread(() -> {
						var recyclerAdapter = new SettingsAdapter(screen, SettingsActivity.this) {
							@Override
							public void onEmptyStateChanged(boolean isEmpty) {
								if(isEmpty) {
									emptyView.setInfo("Here's nothing",
											"Yup, this screen is completely empty. You won't see anything here.");
								} else {
									emptyView.hideAll();
								}
							}
						};

						TransitionManager.beginDelayedTransition(binding.getRoot(), new Fade());
						setupHeader(binding, screen);
						finishLoading(binding, recyclerAdapter);

						if(screen.getItems() == null || screen.getItems().isEmpty()) {
							emptyView.setInfo("Here's nothing", "Yup, this screen is completely empty. You won't see anything here.");
						}
					});
				} catch(Throwable t) {
					Log.e(TAG, "Failed to get settings", t);
					toast(ExceptionDescriptor.getTitle(ExceptionDescriptor.unwrap(t)), 1);
					finish();
				}
			});
		}

		return binding;
	}

	@Override
	public void launchScreen(Setting setting) {
		if(setting instanceof ParsedSetting parsedSetting) {
			parsedSetting.restoreSavedValues();
		}

		openSettingScreen(this, setting);
	}
}