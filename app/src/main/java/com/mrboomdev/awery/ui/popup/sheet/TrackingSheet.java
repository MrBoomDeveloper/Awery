package com.mrboomdev.awery.ui.popup.sheet;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.databinding.ItemListDropdownBinding;
import com.mrboomdev.awery.databinding.LayoutTrackingOptionsBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogTrackingOptions;
import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.adapter.ArrayListAdapter;
import com.mrboomdev.awery.util.ui.dialog.DialogUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrackingSheet extends BottomSheetDialogFragment {
	public static final String TAG = "TrackingSheet";
	private final Map<String, ExtensionProvider> mappedIds = new HashMap<>();
	private final CatalogMedia media;
	private final List<CatalogFilter> filters;
	private final CatalogFilter pageFilter, queryFilter;
	private LayoutTrackingOptionsBinding binding;
	private ArrayListAdapter<String> sourcesAdapter;
	private ArrayListAdapter<CatalogList> listsAdapter;
	private ExtensionProvider selectedProvider;
	private CatalogTrackingOptions trackingOptions;
	private boolean haveToDismiss, autoSelectNext = true;
	private long loadId;

	public TrackingSheet(@NonNull CatalogMedia media) {
		this.media = media;

		queryFilter = new CatalogFilter(CatalogFilter.Type.STRING, "query");
		pageFilter = new CatalogFilter(CatalogFilter.Type.NUMBER, "page");
		filters = List.of(queryFilter, pageFilter);

		queryFilter.setValue(media.getTitle());
	}

	/**
	 * DO NOT USE THIS CONSTRUCTOR!
	 * It was made only to prevent crashes while trying to restore the dialog.
	 * The dialog will be automatically dismissed once created.
	 */
	public TrackingSheet() {
		this.media = null;
		this.haveToDismiss = true;

		this.filters = null;
		this.pageFilter = null;
		this.queryFilter = null;
	}

	@Override
	public void onStart() {
		if(haveToDismiss) dismiss();
		else super.onStart();

		var dialog = Objects.requireNonNull(getDialog());
		DialogUtils.fixDialog(dialog);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(haveToDismiss) return null;

		binding = LayoutTrackingOptionsBinding.inflate(inflater, null, false);
		binding.source.input.setText("Select a tracker", false);
		binding.title.input.setText("Select a title", false);

		binding.delete.setOnClickListener(v -> new Thread(() -> {
			if(selectedProvider == null) return;

			var dao = getDatabase().getMediaProgressDao();
			var progress = dao.get(media.globalId);

			if(progress != null) {
				progress.trackers.remove(selectedProvider.getGlobalId());
				dao.insert(progress);
			}

			toast("Deleted successfully");
		}).start());

		binding.startDate.setOnTouchListener((e, a) -> {
			if(a.getAction() == MotionEvent.ACTION_UP) {
				var dialog = MaterialDatePicker.Builder.datePicker()
						.setSelection(trackingOptions.startDate != null ? trackingOptions.startDate.getTimeInMillis() : null)
						.build();

				dialog.addOnPositiveButtonClickListener(date -> {
					trackingOptions.startDate = Calendar.getInstance();
					trackingOptions.startDate.setTimeInMillis(date);

					binding.startDate.setText(DateFormat.getDateInstance(DateFormat.LONG)
							.format(trackingOptions.startDate.getTime()));
				});

				dialog.show(getChildFragmentManager(), "date_picker");
			}

			return true;
		});

		binding.endDate.setOnTouchListener((e, a) -> {
			if(a.getAction() == MotionEvent.ACTION_UP) {
				var dialog = MaterialDatePicker.Builder.datePicker()
						.setSelection(trackingOptions.endDate != null ? trackingOptions.endDate.getTimeInMillis() : null)
						.build();

				dialog.addOnPositiveButtonClickListener(date -> {
					trackingOptions.endDate = Calendar.getInstance();
					trackingOptions.endDate.setTimeInMillis(date);

					binding.endDate.setText(DateFormat.getDateInstance(DateFormat.LONG)
							.format(trackingOptions.endDate.getTime()));
				});

				dialog.show(getChildFragmentManager(), "date_picker");
			}

			return true;
		});

		binding.startDate.setInputType(0);
		binding.endDate.setInputType(0);

		binding.isPrivateWrapper.setOnClickListener(
				v -> binding.isPrivate.toggle());

		binding.confirm.setOnClickListener(v -> new Thread(() -> {
			if(selectedProvider == null || trackingOptions == null) return;

			binding.confirm.setEnabled(false);
			binding.delete.setEnabled(false);

			if(trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_LISTS)) {
				trackingOptions.currentList = trackingOptions.lists.get(0);
			}

			selectedProvider.trackMedia(media, trackingOptions, new ExtensionProvider.ResponseCallback<>() {
				@Override
				public void onSuccess(CatalogTrackingOptions catalogTrackingOptions) {
					var context = getContext();
					if(context == null) return;

					runOnUiThread(() -> {
						binding.confirm.setEnabled(true);
						binding.delete.setEnabled(true);

						toast("Saved successfully");
					});
				}

				@Override
				public void onFailure(Throwable e) {
					var context = getContext();
					if(context == null) return;

					runOnUiThread(() -> {
						binding.confirm.setEnabled(true);
						binding.delete.setEnabled(true);

						CrashHandler.showErrorDialog(context, "Failed to save!", e);
					});
				}
			});

			var dao = getDatabase().getMediaProgressDao();
			var progress = dao.get(media.globalId);

			if(progress == null) {
				progress = new CatalogMediaProgress(media.globalId);
			}

			progress.trackers.put(
					selectedProvider.getGlobalId(),
					trackingOptions.id);

			dao.insert(progress);
		}).start());

		sourcesAdapter = new ArrayListAdapter<>((id, recycled, parent) -> {
			var item = mappedIds.get(id);

			if(item == null) {
				throw new IllegalStateException("Invalid provider id: " + id);
			}

			if(recycled == null) {
				var itemBinding = ItemListDropdownBinding.inflate(inflater, parent, false);
				recycled = itemBinding.getRoot();
			}

			TextView title = recycled.findViewById(R.id.title);
			ImageView icon = recycled.findViewById(R.id.icon);

			title.setText(item.getName());

			return recycled;
		});

		binding.source.input.setOnItemClickListener((parent, view, position, id) -> {
			autoSelectNext = false;

			var itemId = sourcesAdapter.getItem(position);
			var item = mappedIds.get(itemId);

			if(item == null) {
				throw new IllegalStateException("Invalid provider id: " + itemId);
			}

			binding.source.input.setText(item.getName(), false);

			toast("This functionality isn't done yet!");
			//updateTrackingDialogState(null, null);
		});

		var titles = new ArrayList<>(media.titles);
		var more = requireContext().getString(R.string.manual_search);
		titles.add(more);

		var titlesAdapter = new ArrayListAdapter<>(titles, (item, recycled, parent) -> {
			if(recycled == null) {
				var itemBinding = ItemListDropdownBinding.inflate(inflater, parent, false);
				recycled = itemBinding.getRoot();
			}

			TextView title = recycled.findViewById(R.id.title);
			title.setText(item);

			return recycled;
		});

		binding.title.input.setOnItemClickListener((parent, view, position, id) -> {
			autoSelectNext = false;

			var item = titles.get(position);
			if(item == null) return;

			if(item.equals(more)) {
				binding.title.input.setText(queryFilter.getStringValue(), false);
				toast("Manual search isn't currently available :(");
				//TODO: Launch a SearchActivity
			} else {
				queryFilter.setValue(item);

				toast("This functionality isn't done yet!");
				//searchMedia(Objects.requireNonNull(mappedIds.get(item)));
			}
		});

		binding.source.input.setAdapter(sourcesAdapter);
		binding.title.input.setAdapter(titlesAdapter);

		updateTrackingDialogState(null, null);
		loadMediaFromDB();

		return binding.getRoot();
	}

	private void loadMediaFromDB() {
		binding.searchStatus.setText("Loading...");

		new Thread(() -> {
			var progressDao = getDatabase().getMediaProgressDao();

			var __progress = progressDao.get(media.globalId);
			if(__progress == null) __progress = new CatalogMediaProgress(media.globalId);
			var progress = __progress;

			var availableSources = stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
					.map(ext -> ext.getProviders(ExtensionProvider.FEATURE_TRACK))
					.flatMap(AweryApp::stream)
					.toList();

			for(var provider : availableSources) {
				mappedIds.put(provider.getId(), provider);
			}

			runOnUiThread(() -> {
				if(availableSources.isEmpty()) {
					binding.searchStatus.setText("Nothing selected");

					updateTrackingDialogState(null,
							new ZeroResultsException("No trackable sources available", R.string.no_tracker_extensions));
				} else {
					var foundTracked = stream(availableSources)
							.filter(provider -> progress.trackers.containsKey(provider.getGlobalId()))
							.findFirst();

					var defaultTracked = foundTracked.isPresent()
							? foundTracked.get() : availableSources.get(0);

					binding.source.input.setText(defaultTracked.getName(), false);

					if(foundTracked.isPresent()) {
						var query = progress.trackers.get(foundTracked.get().getGlobalId());
						queryFilter.setValue(query);
						binding.title.input.setText(query, false);

						searchMedia(foundTracked.get());
					} else {
						var title = media.titles.get(0);
						binding.title.input.setText(title, false);

						queryFilter.setValue(title);
						searchMedia(defaultTracked);
					}
				}

				sourcesAdapter.setItems(mappedIds.keySet());
			});
		}).start();
	}

	private void searchMedia(@NonNull ExtensionProvider source) {
		runOnUiThread(() -> binding.searchStatus.setText("Searching \"" + queryFilter.getStringValue() + "\"..."));

		var myId = ++loadId;

		source.searchMedia(requireContext(), filters, new ExtensionProvider.ResponseCallback<>() {
			@Override
			public void onSuccess(CatalogSearchResults<? extends CatalogMedia> results) {
				var context = getContext();
				if(myId != loadId || context == null) return;

				var media = results.get(0);

				source.trackMedia(media, null, new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(CatalogTrackingOptions catalogTrackingOptions) {
						TrackingSheet.this.trackingOptions = catalogTrackingOptions;

						runOnUiThread(() -> {
							var context = getContext();
							if(myId != loadId || context == null) return;

							updateTrackingDialogState(catalogTrackingOptions, null);

							binding.confirm.setEnabled(true);
							binding.delete.setEnabled(true);

							binding.searchStatus.setText("Found \"" + media.getTitle() + "\"");
							binding.searchStatus.setOnClickListener(v -> MediaUtils.launchMediaActivity(context, media));

							binding.searchStatus.setClickable(true);
							binding.searchStatus.setFocusable(true);
						});
					}

					@Override
					public void onFailure(Throwable e) {
						var context = getContext();
						if(myId != loadId || context == null) return;

						Log.e(TAG, "Failed to get tracking options", e);
						CrashHandler.showErrorDialog(context, "Failed to get tracking options", e);

						binding.searchStatus.setClickable(false);
						binding.searchStatus.setFocusable(false);
					}
				});
			}

			@Override
			public void onFailure(Throwable e) {
				var context = getContext();
				if(myId != loadId || context == null) return;

				Log.e(TAG, "Failed to load items for a tracker", e);
				CrashHandler.showErrorDialog(context, "Failed to load items for a tracker", e);

				binding.searchStatus.setClickable(false);
				binding.searchStatus.setFocusable(false);
			}
		});
	}

	private void updateTrackingDialogState(
			CatalogTrackingOptions trackingOptions,
			Throwable throwable
	) {
		if(getContext() == null) return;

		if(trackingOptions == null) {
			binding.loadingState.getRoot().setVisibility(View.VISIBLE);

			if(throwable != null) {
				var description = new ExceptionDescriptor(throwable);

				binding.loadingState.title.setText(description.getTitle(requireContext()));
				binding.loadingState.message.setText(description.getMessage(requireContext()));

				binding.loadingState.progressBar.setVisibility(View.GONE);
				binding.loadingState.info.setVisibility(View.VISIBLE);
			} else {
				binding.loadingState.info.setVisibility(View.GONE);
				binding.loadingState.progressBar.setVisibility(View.VISIBLE);
			}
		} else {
			binding.loadingState.getRoot().setVisibility(View.GONE);
		}

		binding.progressIncrement.setOnClickListener(v -> {
			if(trackingOptions == null) return;

			trackingOptions.progress++;
			binding.progress.setText(String.valueOf(trackingOptions.progress));
		});

		binding.progress.setText(String.valueOf(trackingOptions != null ? trackingOptions.progress : 0));

		binding.progressWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_PROGRESS) ? View.VISIBLE : View.GONE);

		binding.statusWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_LISTS) ? View.VISIBLE : View.GONE);

		binding.isPrivateWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_PRIVATE) ? View.VISIBLE : View.GONE);

		binding.scoreWrapper.setVisibility(trackingOptions != null &&
				trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_SCORE) ? View.VISIBLE : View.GONE);

		binding.dateWrapper.setVisibility(trackingOptions != null &&
				(trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_DATE_START)
						|| trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_DATE_END)) ? View.VISIBLE : View.GONE);
	}
}