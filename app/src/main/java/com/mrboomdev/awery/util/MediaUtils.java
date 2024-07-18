package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryApp.fixDialog;
import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.Constants.CATALOG_LIST_BLACKLIST;
import static com.mrboomdev.awery.data.Constants.HIDDEN_LISTS;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.createLinearParams;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ShareCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.db.item.DBCatalogList;
import com.mrboomdev.awery.data.db.item.DBCatalogMedia;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.databinding.PopupMediaActionsBinding;
import com.mrboomdev.awery.databinding.PopupMediaBookmarkBinding;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;
import com.mrboomdev.awery.util.ui.fields.EditTextField;

import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class MediaUtils {
	public static final String ACTION_INFO = "info";
	public static final String ACTION_WATCH = "watch";
	public static final String ACTION_COMMENTS = "comments";
	public static final String ACTION_RELATIONS = "relations";

	public static void launchMediaActivity(Context context, @NonNull CatalogMedia media, String action) {
		var intent = new Intent(context, MediaActivity.class);
		intent.putExtra(MediaActivity.EXTRA_MEDIA, media);
		intent.putExtra(MediaActivity.EXTRA_ACTION, action);
		context.startActivity(intent);
	}

	public static void launchMediaActivity(Context context, CatalogMedia media) {
		launchMediaActivity(context, media, ACTION_INFO);
	}

	@NonNull
	public static Collection<? extends CatalogMedia> filterMediaSync(
			@NonNull Collection<? extends CatalogMedia> items
	) {
		return stream(items)
				.filter(item -> !isMediaFilteredSync(item))
				.toList();
	}

	public static void filterMedia(
			@NonNull Collection<? extends CatalogMedia> items,
			Callbacks.Callback1<Collection<? extends CatalogMedia>> callback
	) {
		thread(() -> callback.run(filterMediaSync(items)));
	}

	public static boolean isMediaFilteredSync(@NonNull CatalogMedia media) {
		var prefs = NicePreferences.getPrefs();
		var badTags = prefs.getStringSet(AwerySettings.GLOBAL_EXCLUDED_TAGS);
		var excludeMediaEntries = AwerySettings.HIDE_LIBRARY_ENTRIES.getValue();
		var saved = AweryApp.getDatabase().getMediaProgressDao().get(media.globalId);

		if(saved != null) {
			if(excludeMediaEntries && saved.getListsCount() > 0) {
				return true;
			}

			if(saved.isInList(CATALOG_LIST_BLACKLIST)) {
				return true;
			}
		}

		return media.tags != null && stream(media.tags)
				.map(CatalogTag::getName)
				.anyMatch(badTags::contains);
	}

	@Contract(pure = true)
	public static void isMediaFiltered(@NonNull CatalogMedia media, Callbacks.Callback1<Boolean> callback) {
		thread(() -> callback.run(isMediaFilteredSync(media)));
	}

	public static void openMediaActionsMenu(Context context, @NonNull CatalogMedia media, Runnable updateCallback) {
		final var dialog = new AtomicReference<Dialog>();
		var inflater = LayoutInflater.from(context);
		var binding = PopupMediaActionsBinding.inflate(inflater);

		if(media.url == null) {
			binding.share.setVisibility(View.GONE);
		}

		binding.title.setText(media.getTitle());

		binding.play.setOnClickListener(v -> {
			launchMediaActivity(context, media, ACTION_WATCH);
			dialog.get().dismiss();
		});

		binding.share.setOnClickListener(v -> {
			shareMedia(context, media);
			dialog.get().dismiss();
		});

		binding.bookmark.setOnClickListener(v -> {
			openMediaBookmarkMenu(context, media);
			dialog.get().dismiss();
		});

		binding.hide.setOnClickListener(v -> {
			blacklistMedia(media, updateCallback);
			dialog.get().dismiss();
		});

		var sheet = new BottomSheetDialog(context);
		dialog.set(sheet);
		sheet.setContentView(binding.getRoot());
		sheet.show();

		fixDialog(sheet);
	}

	public static void blacklistMedia(CatalogMedia media, Runnable callback) {
		thread(() -> {
			var listsDao = getDatabase().getMediaProgressDao();
			var mediaDao = getDatabase().getMediaDao();

			var lists = listsDao.get(media.globalId);
			if(lists == null) lists = new CatalogMediaProgress(media.globalId);
			lists.addToList(CATALOG_LIST_BLACKLIST);

			mediaDao.insert(DBCatalogMedia.fromCatalogMedia(media));
			listsDao.insert(lists);

			runOnUiThread(callback);
		});
	}

	public static void shareMedia(Context context, @NonNull CatalogMedia media) {
		new ShareCompat.IntentBuilder(context)
				.setType("text/plain")
				.setText(media.url)
				.startChooser();
	}

	public static void requestCreateNewList(Context context, OnListCreatedListener callback) {
		var input = new EditTextField(context, "List name");
		input.setLinesCount(1);

		var dialog = new DialogBuilder(context)
				.setTitle("Create new list")
				.addView(input.getView())
				.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
				.setPositiveButton("Create", _dialog -> {
					var text = input.getText().trim();

					if(text.isBlank()) {
						input.setError("List name cannot be empty!");
						return;
					}

					thread(() -> {
						var list = new CatalogList(text);
						var dbList = DBCatalogList.fromCatalogList(list);
						getDatabase().getListDao().insert(dbList);

						runOnUiThread(() -> callback.onListCreated(list));
					});

					_dialog.dismiss();
				}).show();

		input.setCompletionListener(dialog::performPositiveClick);
	}

	public static void requestDeleteList(Context context, @NonNull CatalogList list, Runnable callback) {
		new DialogBuilder(context)
				.setTitle("Delete \"" + list.getTitle() + "\"?")
				.setMessage(R.string.sure_delete_list_description)
				.setNegativeButton(R.string.cancel, DialogBuilder::dismiss)
				.setPositiveButton(R.string.delete, dialog -> {
					thread(() -> {
						var dbList = DBCatalogList.fromCatalogList(list);
						getDatabase().getListDao().delete(dbList);

						runOnUiThread(callback);
					});

					callback.run();
					dialog.dismiss();
				})
				.show();
	}

	public static void requestEditList(Context context, CatalogList list) {
		new DialogBuilder(context);
	}

	public interface OnListCreatedListener {
		void onListCreated(CatalogList list);
	}

	public static void openMediaBookmarkMenu(Context context, CatalogMedia media) {
		thread(() -> {
			var lists = stream(AweryApp.getDatabase().getListDao().getAll())
					.filter(item -> !HIDDEN_LISTS.contains(item.getId()))
					.toList();

			var progressDao = AweryApp.getDatabase().getMediaProgressDao();
			var __progress = progressDao.get(media.globalId);
			if(__progress == null) __progress = new CatalogMediaProgress(media.globalId);
			var progress = __progress;

			runOnUiThread(() -> {
				final var dialog = new AtomicReference<Dialog>();
				var binding = PopupMediaBookmarkBinding.inflate(LayoutInflater.from(context));
				var checked = new HashMap<String, Boolean>();

				var createListView = (Callbacks.Callback1<CatalogList>) item -> {
					var linear = new LinearLayoutCompat(context);
					linear.setGravity(Gravity.CENTER_VERTICAL);
					linear.setOrientation(LinearLayoutCompat.HORIZONTAL);

					var checkbox = new MaterialCheckBox(context);
					checkbox.setText(item.getTitle());
					linear.addView(checkbox, createLinearParams(0, WRAP_CONTENT, 1));

					var removeButton = new ImageView(context);
					removeButton.setImageResource(R.drawable.ic_round_dots_vertical_24);
					var color = resolveAttrColor(context, com.google.android.material.R.attr.colorOnSurface);
					removeButton.setImageTintList(ColorStateList.valueOf(color));
					removeButton.setBackgroundResource(R.drawable.ripple_circle_white);
					linear.addView(removeButton, dpPx(removeButton, 38), dpPx(removeButton, 38));
					setPadding(removeButton, dpPx(removeButton, 8));

					removeButton.setOnClickListener(v -> requestDeleteList(context, item,
							() -> binding.lists.removeView(linear)));

					if(progress.isInList(item.getId())) {
						checked.put(item.getId(), true);
						checkbox.setChecked(true);
					}

					checkbox.setOnCheckedChangeListener((buttonView, isChecked) ->
							checked.put(item.getId(), isChecked));

					binding.lists.addView(linear);
				};

				for(var list : lists) {
					createListView.run(list.toCatalogList());
				}

				binding.create.setOnClickListener(v -> requestCreateNewList(context, createListView::run));

				var sheet = new BottomSheetDialog(context);
				dialog.set(sheet);

				sheet.setOnDismissListener(_dialog -> {
					if(checked.isEmpty()) {
						dialog.get().dismiss();
						return;
					}

					thread(() -> {
						try {
							progress.clearLists();

							for(var entry : checked.entrySet()) {
								if(!entry.getValue()) continue;
								progress.addToList(entry.getKey());
							}

							// Update poster, tags and so on...
							getDatabase().getMediaDao().insert(DBCatalogMedia.fromCatalogMedia(media));

							progressDao.insert(progress);

							// TODO: Need to replace this with something new
							//LibraryFragment.notifyDataChanged();
						} catch(Exception e) {
							AweryApp.toast("Failed to save!");
							Log.e("MediaUtils", "Failed to save bookmark", e);
						}
					});
				});

				sheet.setContentView(binding.getRoot());
				sheet.show();

				fixDialog(sheet);
			});
		});
	}
}