package com.mrboomdev.awery.ui.mobile.screens.settings;

import static com.mrboomdev.awery.app.App.isLandscape;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.App.setContentViewCompat;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.app.theme.ThemeManager;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.data.settings.SettingsData;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.databinding.ScreenSettingsBinding;
import com.mrboomdev.awery.platform.android.AndroidGlobals;
import com.mrboomdev.awery.ui.mobile.components.EmptyStateView;
import com.mrboomdev.awery.util.exceptions.OkiThrowableMessageKt;
import com.mrboomdev.awery.util.extensions.ActivityExtensionsKt;
import com.mrboomdev.awery.utils.UniqueIdGenerator;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements SettingsDataHandler {
	public static final String EXTRA_JSON = "item";
	public static final String EXTRA_PAYLOAD_ID = "payload_id";
	private static final String TAG = "SettingsActivity";
	private static final Map<Long, SettingsItem> payloads = new HashMap<>();
	private static final UniqueIdGenerator payloadIdGenerator = new UniqueIdGenerator();
	private static WeakReference<RecyclerView.RecycledViewPool> viewPool;
	private ScreenSettingsBinding binding;
	private EmptyStateView emptyView;

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

	public static void start(Context context, SettingsItem item) {
		var id = payloadIdGenerator.getLong();
		payloads.put(id, item);

		var intent = new Intent(context, SettingsActivity.class);
		intent.putExtra(EXTRA_PAYLOAD_ID, id);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.INSTANCE.applyTheme(this);
		ActivityExtensionsKt.enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var itemJson = getIntent().getStringExtra(EXTRA_JSON);
		var payloadId = getIntent().getLongExtra(EXTRA_PAYLOAD_ID, -1);

		SettingsItem item = null;

		if(payloadId != -1) {
			item = payloads.get(payloadId);

			if(item == null) {
				// The system has restarted an app :(
				finish();
				return;
			}
		}

		if(item == null) {
			if(itemJson == null) {
				item = NicePreferences.getSettingsMap();
			} else {
				var moshi = new Moshi.Builder().build();
				var adapter = moshi.adapter(SettingsItem.class);

				try {
					item = adapter.fromJson(itemJson);
					if(item == null) throw new IllegalArgumentException("Failed to parse settings");
				} catch(IOException e) {
					Log.e(TAG, "Failed to parse settings", e);
					AndroidGlobals.INSTANCE.toast("Failed to get settings", 0);
					finish();
					return;
				}
			}
		}

		doIfNotNull(createView(item), view -> {
			view.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));
			setContentViewCompat(this, view.getRoot());
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

	private void setupHeader(@NonNull ScreenSettingsBinding binding, @NonNull SettingsItem item) {
		binding.title.setText(item.getTitle(this));
		binding.actions.removeAllViews();

		var headerItems = item.getHeaderItems();

		if(headerItems != null && !headerItems.isEmpty()) {
			for(var headerItem : headerItems) {
				var view = new ImageView(this);
				view.setOnClickListener(v -> headerItem.onClick(this));
				setPadding(view, dpPx(view, 10));

				view.setForeground(AppCompatResources.getDrawable(this, R.drawable.ripple_circle_white));
				view.setClickable(true);
				view.setFocusable(true);

				view.setScaleType(ImageView.ScaleType.FIT_CENTER);
				view.setImageDrawable(headerItem.getIcon(this));

				if(headerItem.tintIcon()) {
					var context = binding.getRoot().getContext();
					var colorAttr = com.google.android.material.R.attr.colorOnSurface;
					var color = App.resolveAttrColor(context, colorAttr);
					view.setImageTintList(ColorStateList.valueOf(color));
				} else {
					view.setImageTintList(null);
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
	private ScreenSettingsBinding createView(@NonNull SettingsItem item) {
		binding = ScreenSettingsBinding.inflate(getLayoutInflater());
		emptyView = new EmptyStateView(binding.progressIndicator);

		binding.recycler.setRecycledViewPool(getViewPool());
		binding.back.setOnClickListener(v -> finish());

		setOnApplyUiInsetsListener(binding.header, insets -> {
			setTopPadding(binding.header, insets.top + dpPx(binding.header, 8));
			setHorizontalPadding(binding.header, insets.left, insets.right);

			setRightMargin(binding.actions, isLandscape()
					? dpPx(binding.actions, 18) : 0);

			return true;
		});

		if(item.getItems() != null) {
			if(item.getItems().isEmpty()) {
				finish();
				return null;
			}

			var recyclerAdapter = new SettingsAdapter(item,
					(item instanceof SettingsDataHandler handler) ? handler : this) {

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
		} else {
			SettingsData.getScreen(this, item, (screen, e) -> {
				if(e != null) {
					Log.e(TAG, "Failed to get settings", e);
					AndroidGlobals.INSTANCE.toast(OkiThrowableMessageKt.explain(e).getTitle(), 0);
					finish();
					return;
				}

				var recyclerAdapter = new SettingsAdapter(screen, (screen instanceof SettingsDataHandler handler) ? handler : this) {
					@Override
					public void onEmptyStateChanged(boolean isEmpty) {
						if(isEmpty) {
							emptyView.setInfo("Here's nothing", "Yup, this screen is completely empty. You won't see anything here.");
						} else {
							emptyView.hideAll();
						}
					}
				};

				TransitionManager.beginDelayedTransition(binding.getRoot(), new Fade());
				setupHeader(binding, screen);
				finishLoading(binding, recyclerAdapter);

				if(screen.getItems().isEmpty()) {
					emptyView.setInfo("Here's nothing", "Yup, this screen is completely empty. You won't see anything here.");
				}
			});
		}

		return binding;
	}

	@Override
	public void onScreenLaunchRequest(@NonNull SettingsItem item) {
		item.restoreSavedValues();

		var moshi = new Moshi.Builder().add(new SettingsItem.Adapter()).build();
		var jsonAdapter = moshi.adapter(SettingsItem.class);

		var intent = new Intent(this, SettingsActivity.class);
		intent.putExtra("item", jsonAdapter.toJson(item));
		startActivity(intent);
	}

	@Override
	public void saveValue(@NonNull SettingsItem item, Object newValue) {
		getPrefs().saveValue(item, newValue);
	}

	@Override
	public Object restoreValue(SettingsItem item) {
		return getPrefs().restoreValue(item);
	}
}