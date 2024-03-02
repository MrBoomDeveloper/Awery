package com.mrboomdev.awery.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ShareCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.data.db.DBCatalogMedia;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.concurrent.atomic.AtomicReference;

import ani.awery.R;
import ani.awery.databinding.PopupMediaActionsBinding;

public class MediaUtils {
	public static final String ACTION_INFO = "info";
	public static final String ACTION_WATCH = "watch";
	public static final String ACTION_COMMENTS = "comments";
	public static final String ACTION_RELATIONS = "relations";

	public static void launchMediaActivity(Context context, @NonNull CatalogMedia media, String action) {
		var intent = new Intent(context, MediaActivity.class);
		intent.putExtra("media", media.toString());
		intent.putExtra("action", action);
		context.startActivity(intent);
	}

	public static void launchMediaActivity(Context context, CatalogMedia media) {
		launchMediaActivity(context, media, ACTION_INFO);
	}

	public static void openMediaActionsMenu(Context context, @NonNull CatalogMedia media) {
		final var dialog = new AtomicReference<Dialog>();
		var inflater = LayoutInflater.from(context);
		var binding = PopupMediaActionsBinding.inflate(inflater);

		binding.title.setText(media.title);

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

		var sheet = new BottomSheetDialog(context);
		dialog.set(sheet);

		sheet.getBehavior().setPeekHeight(1000);
		sheet.setContentView(binding.getRoot());
		sheet.show();

		limitDialogSize(sheet.getWindow());
	}

	private static void limitDialogSize(Window window) {
		if(AweryApp.getConfiguration().screenWidthDp > 400) {
			window.setLayout(ViewUtil.dpPx(400), ViewGroup.LayoutParams.MATCH_PARENT);
		}
	}

	public static void shareMedia(Context context, @NonNull CatalogMedia media) {
		new ShareCompat.IntentBuilder(context)
				.setType("text/plain")
				.setText("https://anilist.co/anime/" + media.id)
				.startChooser();
	}

	public static void openMediaBookmarkMenu(Context context, CatalogMedia media) {
		final var dialog = new AtomicReference<Dialog>();
		final var margin = ViewUtil.dpPx(4);

		var actionsView = new LinearLayout(context);
		actionsView.setLayoutParams(new RecyclerView.LayoutParams(ViewUtil.MATCH_PARENT, ViewUtil.WRAP_CONTENT));
		ViewUtil.setPadding(actionsView, ViewUtil.dpPx(16), ViewUtil.dpPx(9));
		var actionsAdapter = SingleViewAdapter.fromView(actionsView);

		var saveLayoutParams = new LinearLayout.LayoutParams(0, ViewUtil.WRAP_CONTENT);
		saveLayoutParams.rightMargin = margin;
		saveLayoutParams.weight = 1;

		var saveButton = new MaterialButton(context);
		saveButton.setText(R.string.save);
		actionsView.addView(saveButton, saveLayoutParams);

		saveButton.setOnClickListener(v -> {
			new Thread(() -> {
				try {
					AweryApp.getDatabase().getMediaDao().insert(DBCatalogMedia.fromCatalogMedia(media));
					AweryApp.toast("Saved successfully!");
				} catch(IllegalStateException e) {
					AweryApp.toast("Failed to save!");
					e.printStackTrace();
				}
			}).start();

			dialog.get().dismiss();
		});

		var cancelLayoutParams = new LinearLayout.LayoutParams(0, ViewUtil.WRAP_CONTENT);
		cancelLayoutParams.leftMargin = margin;
		cancelLayoutParams.weight = 1;

		var cancelButton = new MaterialButton(context);
		cancelButton.setText(R.string.cancel);
		actionsView.addView(cancelButton, cancelLayoutParams);
		cancelButton.setOnClickListener(v -> dialog.get().dismiss());

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), actionsAdapter);

		var recycler = new RecyclerView(context);
		recycler.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
		recycler.setAdapter(concatAdapter);

		var sheet = new BottomSheetDialog(context);
		dialog.set(sheet);

		sheet.setContentView(recycler);
		sheet.getBehavior().setPeekHeight(1000);
		sheet.show();

		limitDialogSize(sheet.getWindow());
	}
}