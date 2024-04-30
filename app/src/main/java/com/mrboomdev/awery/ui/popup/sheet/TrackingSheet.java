package com.mrboomdev.awery.ui.popup.sheet;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.mrboomdev.awery.R;
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
import com.mrboomdev.awery.ui.popup.dialog.SelectionDialog;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.adapter.ArrayListAdapter;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.dialog.DialogUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingSheet {

	@NonNull
	public static Dialog create(Context context, FragmentManager fragmentManager, CatalogMedia media) {
		return isLandscape() ? new Side(context,  fragmentManager, media) : new Bottom(context, fragmentManager, media);
	}

	public static class Side extends SideSheetDialog {
		private final Controller controller;

		public Side(@NonNull Context context, FragmentManager fragmentManager, CatalogMedia media) {
			super(context);
			this.controller = new Controller(fragmentManager, this, media);
		}

		@Override
		public void onStart() {
			super.onStart();
			controller.onStart();
		}
	}

	public static class Bottom extends BottomSheetDialog {
		private final Controller controller;

		public Bottom(Context context, FragmentManager fragmentManager, @NonNull CatalogMedia media) {
			super(context);
			this.controller = new Controller(fragmentManager, this, media);
		}

		@Override
		public void onStart() {
			super.onStart();
			controller.onStart();
		}
	}

	private static class Controller {
		public static final String TAG = "TrackingSheet";
		private final Map<String, ExtensionProvider> mappedIds = new HashMap<>();
		private final List<ExtensionProvider> sources;
		private final CatalogMedia media;
		private final List<CatalogFilter> filters;
		private final CatalogFilter pageFilter, queryFilter;
		private final FragmentManager fragmentManager;
		private final Dialog dialog;
		private LayoutTrackingOptionsBinding binding;
		private ArrayListAdapter<String> sourcesAdapter;
		private ExtensionProvider selectedSource;
		private CatalogTrackingOptions trackingOptions;
		private boolean autoSelectNext = true;
		private long loadId;

		public Controller(FragmentManager fragmentManager, @NonNull Dialog dialog, @NonNull CatalogMedia media) {
			this.fragmentManager = fragmentManager;
			this.dialog = dialog;
			this.media = media;

			queryFilter = new CatalogFilter(CatalogFilter.Type.STRING, "query");
			pageFilter = new CatalogFilter(CatalogFilter.Type.NUMBER, "page");
			filters = List.of(queryFilter, pageFilter);

			queryFilter.setValue(media.getTitle());

			this.sources = stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
					.map(ext -> ext.getProviders(ExtensionProvider.FEATURE_TRACK))
					.flatMap(NiceUtils::stream)
					.toList();

			dialog.setContentView(onCreateView(
					dialog.getLayoutInflater()));
		}

		public void onStart() {
			DialogUtils.fixDialog(dialog);
		}

		@NonNull
		@SuppressLint("ClickableViewAccessibility")
		public View onCreateView(@NonNull LayoutInflater inflater) {
			binding = LayoutTrackingOptionsBinding.inflate(inflater, null, false);
			binding.source.input.setText("Select a tracker", false);
			binding.title.input.setText("Select a title", false);

			binding.status.input.setOnClickListener(v -> {
				binding.status.input.dismissDropDown();

				new SelectionDialog<Selection.Selectable<CatalogList>>(dialog.getContext(), SelectionDialog.Mode.SINGLE)
						.setTitle("Select a status")
						.setItems(trackingOptions == null ? null : stream(trackingOptions.lists)
								.map(item -> {
									var isSelected = (trackingOptions.currentLists != null) &&
											(trackingOptions.currentLists.contains(item.getId()));

									return new Selection.Selectable<>(item, item.getId(), item.getTitle(),
											(isSelected ? Selection.State.SELECTED : Selection.State.UNSELECTED));
								}).collect(Selection.collect()))
						.setCancelButton(R.string.cancel, DialogBuilder::dismiss)
						.setPositiveButton(R.string.ok, (dialog, selection) -> {
							var selected = selection.get(Selection.State.SELECTED);
							if(selected == null) return;

							trackingOptions.currentLists = Collections.singletonList(selected.getItem().getId());
							binding.status.input.setText(selected.getItem().getTitle());
							dialog.dismiss();
						}).show();
			});

			binding.status.getRoot().setEndIconOnClickListener(v ->
					binding.status.input.performClick());

			binding.delete.setOnClickListener(v -> new Thread(() -> {
				if(selectedSource == null) return;

				var dao = getDatabase().getMediaProgressDao();
				var progress = dao.get(media.globalId);

				if(progress != null) {
					progress.trackers.remove(selectedSource.getGlobalId());
					dao.insert(progress);
				}

				toast("Deleted successfully");
			}).start());

			binding.startDate.setOnTouchListener((e, a) -> {
				if(a.getAction() == MotionEvent.ACTION_UP) {
					var dateDialog = MaterialDatePicker.Builder.datePicker()
							.setSelection(trackingOptions.startDate != null ? trackingOptions.startDate.getTimeInMillis() : null)
							.build();

					dateDialog.addOnPositiveButtonClickListener(date -> {
						trackingOptions.startDate = Calendar.getInstance();
						trackingOptions.startDate.setTimeInMillis(date);

						binding.startDate.setText(DateFormat.getDateInstance(DateFormat.LONG)
								.format(trackingOptions.startDate.getTime()));
					});

					dateDialog.show(fragmentManager, "date_picker");
				}

				return true;
			});

			binding.endDate.setOnTouchListener((e, a) -> {
				if(a.getAction() == MotionEvent.ACTION_UP) {
					var dateDialog = MaterialDatePicker.Builder.datePicker()
							.setSelection(trackingOptions.endDate != null ? trackingOptions.endDate.getTimeInMillis() : null)
							.build();

					dateDialog.addOnPositiveButtonClickListener(date -> {
						trackingOptions.endDate = Calendar.getInstance();
						trackingOptions.endDate.setTimeInMillis(date);

						binding.endDate.setText(DateFormat.getDateInstance(DateFormat.LONG)
								.format(trackingOptions.endDate.getTime()));
					});

					dateDialog.show(fragmentManager, "date_picker");
				}

				return true;
			});

			binding.startDate.setInputType(0);
			binding.endDate.setInputType(0);

			binding.isPrivateWrapper.setOnClickListener(
					v -> binding.isPrivate.toggle());

			binding.isPrivate.setOnCheckedChangeListener((buttonView, isChecked) ->
					trackingOptions.isPrivate = isChecked);

			binding.confirm.setOnClickListener(v -> {
				if(selectedSource == null || trackingOptions == null) return;

				binding.confirm.setEnabled(false);
				binding.delete.setEnabled(false);

				progress:
				if(trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_PROGRESS)) {
					var text = binding.progress.getText();

					if(text == null) {
						break progress;
					}

					var textString = text.toString();

					if(!textString.isBlank()) {
						try {
							trackingOptions.progress = Float.parseFloat(textString);
						} catch(NumberFormatException e) {
							binding.progress.setError("Invalid number!");
							return;
						}
					}
				}

				score:
				if(trackingOptions.hasFeatures(CatalogTrackingOptions.FEATURE_SCORE)) {
					var text = binding.score.getText();

					if(text == null) {
						break score;
					}

					var textString = text.toString();

					if(!textString.isBlank()) {
						try {
							trackingOptions.score = Float.parseFloat(textString);
						} catch(NumberFormatException e) {
							binding.score.setError("Invalid number!");
							return;
						}
					}
				}

				selectedSource.trackMedia(media, trackingOptions, new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(CatalogTrackingOptions catalogTrackingOptions) {
						var context = dialog.getContext();

						runOnUiThread(() -> {
							binding.confirm.setEnabled(true);
							binding.delete.setEnabled(true);

							toast("Saved successfully");
						});

						new Thread(() -> {
							var dao = getDatabase().getMediaProgressDao();
							var progress = dao.get(media.globalId);

							if(progress == null) {
								progress = new CatalogMediaProgress(media.globalId);
							}

							progress.trackers.put(
									selectedSource.getGlobalId(),
									trackingOptions.id);

							dao.insert(progress);
						}).start();
					}

					@Override
					public void onFailure(Throwable e) {
						var context = dialog.getContext();

						runOnUiThread(() -> {
							binding.confirm.setEnabled(true);
							binding.delete.setEnabled(true);

							Log.e(TAG, "Failed to save", e);
							updateTrackingDialogState(null, e);
						});
					}
				});
			});

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

				updateTrackingDialogState(null, null);
				selectedSource = item;

				queryFilter.setValue(media.getTitle());
				searchMedia(selectedSource);
			});

			var titles = new ArrayList<>(media.titles);
			var more = dialog.getContext().getString(R.string.manual_search);
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
					updateTrackingDialogState(null, null);
					queryFilter.setValue(item);
					searchMedia(selectedSource);
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

				for(var provider : sources) {
					mappedIds.put(provider.getId(), provider);
				}

				runOnUiThread(() -> {
					if(sources.isEmpty()) {
						binding.searchStatus.setText("Nothing selected");

						updateTrackingDialogState(null, new ZeroResultsException(
								"No trackable sources available",
								R.string.no_tracker_extensions));
					} else {
						var foundTracked = stream(sources)
								.filter(provider -> progress.trackers.containsKey(provider.getGlobalId()))
								.findFirst();

						selectedSource = foundTracked.isPresent()
								? foundTracked.get() : sources.get(0);

						binding.source.input.setText(selectedSource.getName(), false);

						if(foundTracked.isPresent()) {
							var query = progress.trackers.get(foundTracked.get().getGlobalId());
							queryFilter.setValue(query);
							binding.title.input.setText(query, false);

							searchMedia(foundTracked.get());
						} else {
							var title = media.titles.get(0);
							binding.title.input.setText(title, false);

							queryFilter.setValue(title);
							searchMedia(selectedSource);
						}
					}

					sourcesAdapter.setItems(mappedIds.keySet());
				});
			}).start();
		}

		private void searchMedia(@NonNull ExtensionProvider source) {
			runOnUiThread(() -> binding.searchStatus.setText("Searching \"" + queryFilter.getStringValue() + "\"..."));

			var myId = ++loadId;

			source.searchMedia(dialog.getContext(), filters, new ExtensionProvider.ResponseCallback<>() {
				@Override
				public void onSuccess(CatalogSearchResults<? extends CatalogMedia> results) {
					var context = dialog.getContext();
					if(myId != loadId) return;

					var media = results.get(0);

					source.trackMedia(media, null, new ExtensionProvider.ResponseCallback<>() {
						@Override
						public void onSuccess(CatalogTrackingOptions catalogTrackingOptions) {
							trackingOptions = catalogTrackingOptions;

							runOnUiThread(() -> {
								var context = dialog.getContext();
								if(myId != loadId) return;

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
							var context = dialog.getContext();
							if(myId != loadId) return;

							Log.e(TAG, "Failed to get tracking options", e);
							updateTrackingDialogState(null, e);

							binding.searchStatus.setClickable(false);
							binding.searchStatus.setFocusable(false);
						}
					});
				}

				@Override
				public void onFailure(Throwable e) {
					var context = dialog.getContext();
					if(myId != loadId) return;

					Log.e(TAG, "Failed to load items for a tracker", e);
					updateTrackingDialogState(null, e);

					binding.searchStatus.setClickable(false);
					binding.searchStatus.setFocusable(false);
				}
			});
		}

		private void updateTrackingDialogState(
				CatalogTrackingOptions trackingOptions,
				Throwable throwable
		) {
			if(trackingOptions == null) {
				binding.loadingState.getRoot().setVisibility(View.VISIBLE);

				if(throwable != null) {
					var description = new ExceptionDescriptor(throwable);

					binding.loadingState.title.setText(description.getTitle(dialog.getContext()));
					binding.loadingState.message.setText(description.getMessage(dialog.getContext()));

					binding.loadingState.progressBar.setVisibility(View.GONE);
					binding.loadingState.info.setVisibility(View.VISIBLE);
				} else {
					binding.loadingState.info.setVisibility(View.GONE);
					binding.loadingState.progressBar.setVisibility(View.VISIBLE);
				}
			} else {
				binding.loadingState.getRoot().setVisibility(View.GONE);

				binding.progress.setText(String.valueOf(trackingOptions.progress));
				binding.score.setText(String.valueOf(trackingOptions.score));
				binding.isPrivate.setChecked(trackingOptions.isPrivate);

				if(trackingOptions.currentLists != null) {
					var first = trackingOptions.currentLists.get(0);

					var found = stream(trackingOptions.lists)
							.filter(item -> item.getId().equals(first))
							.findFirst().orElse(null);

					if(found != null) {
						binding.status.input.setText(found.getTitle(), false);
					}
				}

				if(trackingOptions.startDate != null) {
					binding.startDate.setText(DateFormat.getDateInstance(DateFormat.LONG)
							.format(trackingOptions.startDate.getTime()));
				}

				if(trackingOptions.endDate != null) {
					binding.endDate.setText(DateFormat.getDateInstance(DateFormat.LONG)
							.format(trackingOptions.endDate.getTime()));
				}
			}

			binding.progressIncrement.setOnClickListener(v -> {
				if(trackingOptions == null) return;

				trackingOptions.progress++;
				binding.progress.setText(String.valueOf(trackingOptions.progress));
			});

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
}