package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.AweryApp.getResourceId;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryLifecycle.postRunnable;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalPadding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.util.IconStateful;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class IconPickerDialog<T> extends BaseDialogBuilder<IconPickerDialog<T>> {
	private final Callbacks.Result1<IconStateful, T> iconExtractor;
	private final int iconSize = dpPx(38), iconSpacing = dpPx(16);
	private Callbacks.Callback1<T> selectCallback;
	private RecyclerView recycler;
	private List<T> items;

	public IconPickerDialog(Context context, Callbacks.Result1<IconStateful, T> iconExtractor) {
		super(context);
		this.iconExtractor = iconExtractor;

		setNegativeButton(R.string.cancel, IconPickerDialog::dismiss);
	}

	public IconPickerDialog<T> setSelectionListener(Callbacks.Callback1<T> selectCallback) {
		this.selectCallback = selectCallback;
		return this;
	}

	public IconPickerDialog<T> setItems(Collection<T> items) {
		this.items = new ArrayList<>(items);
		return this;
	}

	@NonNull
	@Override
	protected View getContentView(View parentView) {
		if(recycler != null) {
			return recycler;
		}

		var columnsCount = new AtomicInteger(5);
		var layoutManager = new GridLayoutManager(getContext(), columnsCount.get());
		recycler = new RecyclerView(getContext());

		// TODO: Uncomment when there will be a lot of icons and the search bar will be implemented
		/*layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				if(position == 0) {
					return columnsCount.get();
				}

				return 1;
			}
		});*/

		postRunnable(() -> {
			setOnApplyUiInsetsListener(recycler, insets -> {
				columnsCount.set(recycler.getMeasuredWidth() / (iconSize + iconSpacing));
				layoutManager.setSpanCount(columnsCount.get());
				return true;
			}, parentView);

			recycler.setAdapter(new Adapter());
		});

		recycler.setLayoutManager(layoutManager);
		return recycler;
	}

	private class Adapter extends RecyclerView.Adapter<ViewHolder> {

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new ViewHolder(new AppCompatImageView(parent.getContext()));
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			if(items == null) {
				return 0;
			}

			return items.size();
		}
	}

	private class ViewHolder extends RecyclerView.ViewHolder {
		private final ImageView view;
		private T item;

		public ViewHolder(@NonNull ImageView view) {
			super(view);

			view.setClickable(true);
			view.setFocusable(true);

			view.setOnClickListener(v -> {
				if(selectCallback != null) {
					selectCallback.run(item);
				}

				dismiss();
			});

			view.setBackgroundResource(R.drawable.ripple_circle_white);
			setPadding(view, iconSpacing / 2);

			view.setLayoutParams(new RecyclerView.LayoutParams(
					iconSize + (iconSpacing / 2),
					iconSize + (iconSpacing / 2)));

			this.view = view;
		}

		@SuppressLint("DiscouragedApi")
		public void bind(T item) {
			this.item = item;
			var icon = iconExtractor.run(item);

			var id = getResourceId(R.drawable.class, icon.getActive());
			view.setImageResource(id);

			view.setImageTintList(ColorStateList.valueOf(resolveAttrColor(view.getContext(),
					com.google.android.material.R.attr.colorOnBackground)));
		}
	}
}