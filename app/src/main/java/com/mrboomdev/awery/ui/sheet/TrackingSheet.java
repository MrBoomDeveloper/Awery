package com.mrboomdev.awery.ui.sheet;

import static com.mrboomdev.awery.app.App.addOnBackPressedListener;
import static com.mrboomdev.awery.app.App.getInflater;
import static com.mrboomdev.awery.app.App.removeOnBackPressedListener;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getActivity;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.data.db.AweryDB.getDatabase;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setWeight;
import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Space;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.data.settings.base.ProxySettingsDataHandler;
import com.mrboomdev.awery.app.data.settings.base.SettingsItem;
import com.mrboomdev.awery.app.data.settings.base.SettingsItemType;
import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.databinding.WidgetDropdownBinding;
import com.mrboomdev.awery.ext.data.Media;
import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.__ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.ui.activity.settings.SettingsAdapter;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.EmptyFuture;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ExtensionComponentMissingException;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.SheetDialog;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingSheet extends SheetDialog {
	private static final String TAG = "TrackingSheet";
	private final Map<String, Object> pendingChanges = new HashMap<>();
	private final Map<String, Media> tracked = new HashMap<>();
	private final ArrayDeque<SettingsItem> path = new ArrayDeque<>();
	private final Media media;
	private Media currentMedia;
	private __ExtensionProvider currentProvider;
	private List<__ExtensionProvider> providers;
	private WidgetDropdownBinding title, ext;
	private LinearLayoutCompat actions;
	private CatalogMediaProgress progress;
	private SettingsAdapter adapter;
	private MaterialButton saveButton, cancelButton;
	private EmptyView emptyView;
	private boolean autoSelectNextProvider = true;
	private boolean autoSelectNextTitle = true;
	private int autoSelectNextProviderIndex;
	private int autoSelectNextTitleIndex;
	private long operationId;

	private final Runnable backListener = () -> {
		if(!path.isEmpty()) {
			var item = requireNonNull(path.pollLast());
			adapter.setScreen(item);
			return;
		}

		if(pendingChanges.isEmpty()) {
			dismiss();
			return;
		}

		new DialogBuilder(getContext())
				.setTitle("You have unsaved changes")
				.setMessage("Do you want to discard all changes or save them?")
				.setNegativeButton("Discard", dialog -> {
					dialog.dismiss();
					dismiss();
				})
				.setPositiveButton(R.string.save, dialog -> {
					save();
					dialog.dismiss();
				}).show();
	};

	public TrackingSheet(Activity context, Media media) {
		super(context);
		this.media = media;
	}

	private void handleFail(Throwable t) {
		var descriptor = new ExceptionDescriptor(t);

		saveButton.setEnabled(true);
		cancelButton.setEnabled(true);

		emptyView.setInfo(
				descriptor.getTitle(getContext()),
				descriptor.getMessage(getContext()));
	}

	private void save() {
		if(pendingChanges.isEmpty()) {
			dismiss();
			return;
		}

		emptyView.startLoading();
		saveButton.setEnabled(false);
		cancelButton.setEnabled(false);

		thread(() -> {
			if(progress == null) {
				progress = new CatalogMediaProgress(media.getGlobalId());
			}

			var dao = getDatabase().getMediaProgressDao();
			dao.insert(progress);

			dismiss();
			toast("Saved successfully!");
		}).addCallback(new EmptyFuture.Callback() {
			@Override
			public void onFailure(Throwable t) {
				runOnUiThread(() -> handleFail(t));
			}
		});
	}

	@Override
	public View getContentView(Context context) {
		var scroller = new NestedScrollView(context);

		var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);
		setPadding(linear, dpPx(linear, 16));
		scroller.addView(linear);

		var header = new LinearLayoutCompat(context);
		header.setOrientation(LinearLayoutCompat.HORIZONTAL);
		linear.addView(header);

		ext = WidgetDropdownBinding.inflate(getInflater(header), header, true);
		ext.input.setHint("Select an source");
		setWeight(ext.getRoot(), 1);

		header.addView(new Space(context), dpPx(header, 8), 0);

		title = WidgetDropdownBinding.inflate(getInflater(header), header, true);
		title.input.setText(media.getTitle(), false);
		title.input.setHint("Search query");
		setWeight(title.getRoot(), 1);

		emptyView = new EmptyView(context);
		emptyView.startLoading();
		linear.addView(emptyView.getRoot());

		var recycler = new RecyclerView(context);
		recycler.setLayoutManager(new LinearLayoutManager(context));

		adapter = new SettingsAdapter(new ProxySettingsDataHandler() {
			@Override
			public void onScreenLaunchRequest(SettingsItem item) {
				path.add(item);
				adapter.setScreen(item);
			}
		});

		recycler.setAdapter(adapter);
		linear.addView(recycler);

		actions = new LinearLayoutCompat(context);
		actions.setOrientation(LinearLayoutCompat.HORIZONTAL);
		actions.setVisibility(View.GONE);
		linear.addView(actions);
		setTopMargin(actions, dpPx(actions, 8));

		cancelButton = new MaterialButton(new ContextThemeWrapper(context,
				com.google.android.material.R.style.Widget_Material3_Button_TonalButton));

		cancelButton.setText(R.string.cancel);
		actions.addView(cancelButton, 0, WRAP_CONTENT);
		setWeight(cancelButton, 1);
		cancelButton.setOnClickListener(v -> dismiss());

		actions.addView(new Space(context), dpPx(actions, 8), 0);

		saveButton = new MaterialButton(new ContextThemeWrapper(context,
				com.google.android.material.R.style.Widget_Material3_Button_TonalButton));

		saveButton.setText(R.string.save);
		actions.addView(saveButton, 0, WRAP_CONTENT);
		setWeight(saveButton, 1);
		saveButton.setOnClickListener(v -> save());

		ExtensionsFactory.getInstance().addCallback(new AsyncFuture.Callback<>() {
			@Override
			public void onSuccess(ExtensionsFactory result) {
				providers = stream(result.getExtensions(__Extension.FLAG_WORKING))
						.map(__Extension::getProviders)
						.flatMap(NiceUtils::stream)
						.sorted().toList();

				thread(() -> {
					progress = getDatabase().getMediaProgressDao().get(media.getGlobalId());

					if(progress == null) {
						runOnUiThread(() -> search(currentProvider, media.getTitle()));
					} else {
						for(var id : progress.trackers.entrySet()) {
							tracked.put(id.getKey(), getDatabase().getMediaDao().get(id.getValue()));
						}

						for(var tracked : tracked.entrySet()) {
							try {
								currentProvider = __ExtensionProvider.forGlobalId(tracked.getKey());

								runOnUiThread(() -> {
									TrackingSheet.this.ext.input.setText(currentProvider.getName());
									TrackingSheet.this.title.input.setText(tracked.getValue().getTitle(), false);
									emptyView.hideAll();
									actions.setVisibility(View.VISIBLE);
								});

								return;
							} catch(ExtensionComponentMissingException | ExtensionNotInstalledException ignored) {}
						}

						// Used provider probably has been deleted
						runOnUiThread(() -> search(currentProvider, media.getTitle()));
					}
				});
			}

			@Override
			public void onFailure(Throwable t) {
				runOnUiThread(() -> handleFail(t));
			}
		});

		return scroller;
	}

	private void search(@Nullable __ExtensionProvider provider, @Nullable String title) throws NullPointerException {
		var originalProvider = provider;
		var originalTitle = title;
		var currentOperationId = ++operationId;

		if(provider == null) {
			if(!autoSelectNextProvider) {
				throw new NullPointerException("provider cannot be null if autoSelectNextProvider is disabled!");
			}

			if(autoSelectNextProviderIndex >= providers.size()) {
				emptyView.setInfo(R.string.nothing_found, R.string.no_tracker_extensions);
				return;
			}

			provider = providers.get(autoSelectNextProviderIndex);
		}

		if(title == null) {
			if(!autoSelectNextTitle) {
				throw new NullPointerException("title cannot be null if autoSelectNextTitle is disabled!");
			}

			if(media.getTitles() == null) {
				throw new NullPointerException("media.titles cannot be null!");
			}

			// We have ran from available titles.
			// switch to next provider and reset autoSelectNextTitleIndex.
			if(autoSelectNextTitleIndex >= media.getTitles().length) {
				autoSelectNextTitleIndex = 0;
				autoSelectNextProviderIndex++;
				search(null, null);
				return;
			}

			title = media.getTitles()[autoSelectNextTitleIndex];
		}

		currentProvider = provider;
		emptyView.startLoading();
		setCanceledOnTouchOutside(true);

		var finalTitle = title;
		var finalProvider = provider;

		runOnUiThread(() -> {
			this.title.input.setText(finalTitle, false);
			this.ext.input.setText(finalProvider.getName(), false);
			actions.setVisibility(View.GONE);
		});

		provider.searchMedia(new SettingsList(
				new SettingsItem(SettingsItemType.INTEGER, ExtensionConstants.FILTER_PAGE, 0),
				new SettingsItem(SettingsItemType.STRING, ExtensionConstants.FILTER_QUERY, title),
				new SettingsItem(ExtensionConstants.FILTER_MEDIA, media)
		)).addCallback(new AsyncFuture.Callback<>() {
			@Override
			public void onSuccess(CatalogSearchResults<? extends CatalogMedia> result) {
				if(operationId != currentOperationId || getContext() == null) return;

				if(result.isEmpty()) {
					onFailure(new ZeroResultsException("Zero results was returned", R.string.no_media_found));
					return;
				}

				runOnUiThread(() -> {
					currentMedia = result.get(0);
					actions.setVisibility(View.VISIBLE);
					TrackingSheet.this.title.input.setText(currentMedia.getTitle(), false);
					getTrackingOptions();
				});
			}

			@Override
			public void onFailure(Throwable t) {
				if(operationId != currentOperationId || getContext() == null) return;
				Log.e(TAG, "Failed to search for media!", t);

				if(autoSelectNextTitle) {
					autoSelectNextTitleIndex++;
					search(originalProvider, originalTitle);
					return;
				}

				if(autoSelectNextProvider) {
					autoSelectNextTitleIndex = 0;
					autoSelectNextProviderIndex++;
					search(originalProvider, originalTitle);
					return;
				}

				emptyView.setInfo(getContext(), t);
			}
		});
	}

	private void getTrackingOptions() {
		var currentOperationId = ++operationId;

		currentProvider.getTrackingFilters().addCallback(new AsyncFuture.Callback<>() {
			@Override
			public void onSuccess(SettingsList result) {
				var context = getContext();
				if(currentOperationId != operationId || context == null) return;

				runOnUiThread(() -> {
					setCanceledOnTouchOutside(false);
					adapter.setItems(result, true);
					emptyView.hideAll();
				});
			}

			@Override
			public void onFailure(Throwable t) {
				var context = getContext();
				if(currentOperationId != operationId || context == null) return;

				Log.e(TAG, "Failed to get tracking filters!", t);

				runOnUiThread(() -> {
					emptyView.setInfo(context, t);
				});
			}
		});
	}

	@Override
	public void show() {
		super.show();

		var activity = requireNonNull(getActivity(getContext()));
		addOnBackPressedListener(activity, backListener);

		setOnDismissListener(d -> {
			removeOnBackPressedListener(activity, backListener);

			//All finished operations won't try to update ui
			operationId++;
		});
	}
}