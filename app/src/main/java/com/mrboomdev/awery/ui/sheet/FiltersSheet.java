package com.mrboomdev.awery.ui.sheet;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.ui.activity.settings.SettingsAdapter;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.dialog.SheetDialog;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FiltersSheet extends SheetDialog implements SettingsDataHandler {
	private static final String TAG = "FiltersSheet";
	private static final List<String> HIDDEN_FILTERS = List.of(
			ExtensionProvider.FILTER_QUERY, ExtensionProvider.FILTER_PAGE);

	private final List<SettingsItem> filters;
	@Nullable
	private final Callbacks.Callback1<List<SettingsItem>> applyCallback;
	@Nullable
	private final ExtensionProvider provider;

	public FiltersSheet(
			Context context,
			List<SettingsItem> filters,
			@NonNull ExtensionProvider provider,
			@NonNull Callbacks.Callback1<List<SettingsItem>> applyCallback
	) {
		super(context);
		this.provider = provider;
		this.applyCallback = applyCallback;

		this.filters = new ArrayList<>(stream(filters)
				.filter(filter -> !HIDDEN_FILTERS.contains(filter.getKey()))
				.map(item -> new CustomSettingsItem(item) {})
				.toList());
	}

	private FiltersSheet(Context context, List<SettingsItem> filters) {
		super(context);
		this.provider = null;
		this.applyCallback = null;
		this.filters = filters;
	}

	@Override
	public View getContentView(Context context) {
		var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);

		var recycler = new RecyclerView(context);
		recycler.setLayoutManager(new LinearLayoutManager(context));
		recycler.setClipToPadding(false);
		setTopPadding(recycler, dpPx(recycler, 8));
		setHorizontalPadding(recycler, dpPx(recycler, 8));
		linear.addView(recycler);

		useLayoutParams(recycler, params -> {
			params.weight = 1;
			params.height = 0;
		}, LinearLayoutCompat.LayoutParams.class);

		var progressBarAdapter = SingleViewAdapter.fromViewDynamic(parent -> {
			var progressBar = new CircularProgressIndicator(context);
			progressBar.setIndeterminate(true);
			setVerticalPadding(progressBar, dpPx(progressBar, 8));

			var params = new RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
			progressBar.setLayoutParams(params);

			return progressBar;
		});

		var screenAdapter = new SettingsAdapter(new SettingsItem() {
			@Override
			public List<? extends SettingsItem> getItems() {
				return filters;
			}
		}, this);

		var adapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), screenAdapter, progressBarAdapter);

		recycler.setAdapter(adapter);

		var actions = new LinearLayoutCompat(context);
		setPadding(actions, dpPx(actions, 16), dpPx(actions, 8));
		linear.addView(actions, MATCH_PARENT, WRAP_CONTENT);

		if(applyCallback != null) {
			var cancel = new MaterialButton(context);
			cancel.setText(R.string.cancel);
			actions.addView(cancel, 0, WRAP_CONTENT);
			cancel.setOnClickListener(v -> dismiss());
			useLayoutParams(cancel, params -> params.weight = 1, LinearLayoutCompat.LayoutParams.class);
			setRightMargin(cancel, dpPx(cancel, 6));

			var save = new MaterialButton(context);
			save.setText(R.string.save);
			actions.addView(save, 0, WRAP_CONTENT);
			useLayoutParams(save, params -> params.weight = 1, LinearLayoutCompat.LayoutParams.class);
			setLeftMargin(cancel, dpPx(cancel, 6));

			save.setOnClickListener(v -> {
				applyCallback.run(filters);
				dismiss();
			});
		} else {
			var done = new MaterialButton(context);
			done.setText(R.string.done);
			actions.addView(done, MATCH_PARENT, WRAP_CONTENT);
			done.setOnClickListener(v -> dismiss());
		}

		if(provider == null) {
			progressBarAdapter.setEnabled(false);
		} else {
			provider.getFilters().addCallback(new AsyncFuture.Callback<>() {
				@Override
				public void onSuccess(SettingsList result) {
					if(!isShown()) return;

					mergeFilters(result, filters);
					filters.clear();
					filters.addAll(result);

					runOnUiThread(() -> {
						progressBarAdapter.setEnabled(false);
						screenAdapter.setItems(result, true);
					}, recycler);
				}

				@Override
				public void onFailure(Throwable t) {
					Log.e(TAG, "Failed to load filters!", t);
					toast("Failed to load filters");

					runOnUiThread(() -> progressBarAdapter.setEnabled(false), recycler);
				}
			});
		}

		return linear;
	}

	@Contract(pure = true)
	private void mergeFilters(@NonNull List<? extends SettingsItem> original, List<? extends SettingsItem> values) {
		for(var originalItem : original) {
			var found = find(values, value -> Objects.equals(originalItem.getKey(), value.getKey()));
			if(found == null) continue;

			if(originalItem.getItems() != null && found.getItems() != null) {
				mergeFilters(originalItem.getItems(), found.getItems());
			}

			if(originalItem.getType() != null) {
				switch(originalItem.getType()) {
					case BOOLEAN, SCREEN_BOOLEAN -> originalItem.setValue(requireNonNullElse(
							found.getBooleanValue(), originalItem.getBooleanValue()));

					case STRING, SELECT, JSON, SERIALIZABLE -> originalItem.setValue(requireNonNullElse(
							found.getStringValue(), originalItem.getStringValue()));

					case INTEGER, SELECT_INTEGER, COLOR -> originalItem.setValue(
							requireNonNullElse(found.getIntegerValue(), originalItem.getIntegerValue()));

					case EXCLUDABLE -> originalItem.setValue(requireNonNullElse(
							found.getExcludableValue(), originalItem.getExcludableValue()));

					case MULTISELECT -> originalItem.setValue(requireNonNullElse(
							found.getStringSetValue(), originalItem.getStringSetValue()));

					case DATE -> originalItem.setValue(requireNonNullElse(
							found.getLongValue(), originalItem.getLongValue()));
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onScreenLaunchRequest(@NonNull SettingsItem item) {
		new FiltersSheet(getContext(), (List<SettingsItem>) item.getItems()).show();
	}

	@Override
	public void saveValue(SettingsItem item, Object newValue) {}

	@Override
	public Object restoreValue(@NonNull SettingsItem item) {
		return switch(item.getType()) {
			case INTEGER, SELECT_INTEGER -> item.getIntegerValue();
			case BOOLEAN, SCREEN_BOOLEAN -> item.getBooleanValue();
			case STRING, SELECT -> item.getStringValue();
			case DATE -> item.getLongValue();
			case EXCLUDABLE -> item.getExcludableValue();
			default -> null;
		};
	}
}