package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.data.Constants.alwaysTrue;
import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsData;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.databinding.LayoutHeaderSettingsBinding;
import com.mrboomdev.awery.databinding.ScreenSettingsBinding;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsDataHandler {
	private static final String TAG = "SettingsActivity";
	private final List<ActivityResultCallback<ActivityResult>> callbacks = new ArrayList<>();
	private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
	private ActivityResultLauncher<Intent> activityResultLauncher;
	private AwerySettings settings;
	private boolean isMain;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var itemJson = getIntent().getStringExtra("item");
		var path = getIntent().getStringExtra("path");

		if(itemJson == null && path == null) {
			isMain = true;
		}

		settings = AwerySettings.getInstance(this);
		SettingsItem item = null;

		if(path != null) {
			item = AwerySettings.getCached(path);

			if(item == null) {
				toast(this, "Failed to get settings", 0);
				finish();
				return;
			}
		}

		if(item == null) {
			if(itemJson == null) {
				item = AwerySettings.getSettingsMap(this);
			} else {
				var moshi = new Moshi.Builder().build();
				var adapter = moshi.adapter(SettingsItem.class);

				try {
					item = adapter.fromJson(itemJson);
					if(item == null) throw new IllegalArgumentException("Failed to parse settings");
				} catch(IOException e) {
					Log.e(TAG, "Failed to parse settings", e);
					toast(this, "Failed to get settings", 0);
					finish();
					return;
				}
			}
		}

		doIfNotNull(createView(item), view -> {
			setContentView(view);

			activityResultLauncher = registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(), result -> {
						for(var callback : callbacks) {
							callback.onActivityResult(result);
						}
					});
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(isMain) {
			AwerySettings.clearCache();
		}

		viewPool.clear();
		callbacks.clear();

		viewPool = null;
		settings = null;
		activityResultLauncher = null;
	}

	public void addActivityResultCallback(
			ActivityResultCallback<ActivityResult> callback
	) {
		callbacks.add(callback);
	}

	public void removeActivityResultCallback(
			ActivityResultCallback<ActivityResult> callback
	) {
		callbacks.remove(callback);
	}

	public ActivityResultLauncher<Intent> getActivityResultLauncher() {
		return activityResultLauncher;
	}

	private void setupHeader(@NonNull LayoutHeaderSettingsBinding binding, @NonNull SettingsItem item, View parent) {
		binding.back.setOnClickListener(v -> finish());
		binding.title.setText(item.getTitle(this));
		binding.actions.removeAllViews();

		var headerItems = item.getHeaderItems();

		if(headerItems != null && !headerItems.isEmpty()) {
			for(var headerItem : headerItems) {
				var view = new ImageView(this);
				view.setOnClickListener(v -> headerItem.onClick(this));
				setPadding(view, dpPx(10));

				view.setForeground(AppCompatResources.getDrawable(this, R.drawable.ripple_circle_white));
				view.setClickable(true);
				view.setFocusable(true);

				view.setScaleType(ImageView.ScaleType.FIT_CENTER);
				view.setImageDrawable(headerItem.getIcon(this));

				if(headerItem.tintIcon()) {
					var context = binding.getRoot().getContext();
					var colorAttr = com.google.android.material.R.attr.colorOnSurface;
					var color = AweryApp.resolveAttrColor(context, colorAttr);
					view.setImageTintList(ColorStateList.valueOf(color));
				} else {
					view.setImageTintList(null);
				}

				binding.actions.addView(view, dpPx(48), dpPx(48));
				setLeftMargin(view, dpPx(10));
			}
		}

		setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
			setHorizontalPadding(binding.getRoot(), insets.left, insets.right);
			return false;
		}, parent);
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

	@Nullable
	private View createView(@NonNull SettingsItem item) {
		item.restoreValues(AwerySettings.getInstance(this));

		var binding = ScreenSettingsBinding.inflate(getLayoutInflater());
		binding.recycler.setRecycledViewPool(viewPool);

		setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
			setTopPadding(binding.recycler, insets.top + dpPx(12));
			return false;
		});

		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		if(item.getItems() != null) {
			if(item.getItems().isEmpty()) {
				finish();
				return null;
			}

			var recyclerAdapter = new SettingsAdapter(item,
					(item instanceof SettingsDataHandler handler) ? handler : this);

			var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
				var headerBinding = LayoutHeaderSettingsBinding.inflate(
						getLayoutInflater(), parent, false);

				setupHeader(headerBinding, item, parent);
				return headerBinding;
			});

			binding.recycler.setAdapter(new ConcatAdapter(config, headerAdapter, recyclerAdapter));
			setupReordering(binding.recycler, recyclerAdapter);
			binding.progressIndicator.setVisibility(View.GONE);
		} else if(item.getBehaviour() != null) {
			SettingsData.getScreen(this, item.getBehaviour(), (screen, e) -> {
				if(e != null) {
					Log.e(TAG, "Failed to get settings", e);
					toast(this, e.getMessage(), 0);
					finish();
					return;
				}

				var recyclerAdapter = new SettingsAdapter(screen, (screen instanceof SettingsDataHandler handler)
						? handler : new SettingsDataHandler() {
					@Override
					public void onScreenLaunchRequest(SettingsItem item) {
						toast("Currently not supported", 1);
					}

					@Override
					public void save(SettingsItem item, Object newValue) {
						toast("Currently not supported", 1);
					}
				});

				var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
					var headerBinding = LayoutHeaderSettingsBinding.inflate(
							getLayoutInflater(), parent, false);

					setupHeader(headerBinding, screen, parent);
					return headerBinding;
				});

				TransitionManager.beginDelayedTransition(binding.getRoot());
				binding.recycler.setAdapter(new ConcatAdapter(config, headerAdapter, recyclerAdapter));
				setupReordering(binding.recycler, recyclerAdapter);
				binding.progressIndicator.setVisibility(View.GONE);
			});
		} else {
			Log.w(TAG, "Screen has no items, finishing.");
			finish();
		}

		return binding.getRoot();
	}

	@Override
	public void onScreenLaunchRequest(@NonNull SettingsItem item) {
		item.restoreValues();

		var moshi = new Moshi.Builder().add(new SettingsItem.Adapter()).build();
		var jsonAdapter = moshi.adapter(SettingsItem.class);

		var intent = new Intent(this, SettingsActivity.class);
		intent.putExtra("item", jsonAdapter.toJson(item));
		startActivity(intent);
	}

	@Override
	public void save(@NonNull SettingsItem item, Object newValue) {
		if(item instanceof CustomSettingsItem custom) {
			custom.saveValue(newValue);
			return;
		}

		(switch(item.getType()) {
			case BOOLEAN -> settings.setBoolean(item.getFullKey(), (boolean) newValue);
			case SELECT -> settings.setString(item.getFullKey(), (String) newValue);
			case SELECT_INT -> settings.setInt(item.getFullKey(), (int) newValue);
			default -> throw new IllegalArgumentException("Unsupported type!");
		}).saveAsync();
	}
}