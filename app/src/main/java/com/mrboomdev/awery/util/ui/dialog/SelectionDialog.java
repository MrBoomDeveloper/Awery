package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.App.getString;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.ext.data.Selection;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Objects;

public class SelectionDialog<T, E extends SelectionDialog<T, E>> extends BaseDialogBuilder<E> {
	private final Mode mode;
	private final LinearLayoutCompat contentView;
	private Selection<T> items = Selection.empty();
	private RadioGroup radioGroup;
	private ChipGroup chipGroup;
	private boolean isChecking;

	public static class Single<T> extends SelectionDialog<T, Single<T>> {

		public Single(Context context) {
			super(context, Mode.SINGLE);
		}

		public Single<T> setPositiveButton(String label, SelectionListener<T> listener) {
			return setPositiveButton(label, dialog -> {
				if(listener != null) listener.onSelected(this, getSelection().get(Selection.State.SELECTED));
			});
		}

		public Single<T> setPositiveButton(int label, SelectionListener<T> listener) {
			return setPositiveButton(label, dialog -> {
				if(listener != null) listener.onSelected(this, getSelection().get(Selection.State.SELECTED));
			});
		}

		public interface SelectionListener<T> {
			void onSelected(Single<T> dialog, @Nullable T value);
		}
	}

	public static class Multi<T> extends SelectionDialog<T, Multi<T>> {

		public Multi(Context context) {
			super(context, Mode.MULTI);
		}

		public Multi<T> setPositiveButton(String label, SelectionListener<T> listener) {
			return setPositiveButton(label, dialog -> {
				if(listener != null) listener.onSelected(this, getSelection());
			});
		}

		public Multi<T> setPositiveButton(int label, SelectionListener<T> listener) {
			return setPositiveButton(label, dialog -> {
				if(listener != null) listener.onSelected(this, getSelection());
			});
		}

		public interface SelectionListener<T> {
			void onSelected(Multi<T> dialog, Selection<T> value);
		}
	}

	private SelectionDialog(Context context, Mode mode) {
		super(context);
		this.mode = mode;

		contentView = new LinearLayoutCompat(context);
		contentView.setOrientation(LinearLayoutCompat.VERTICAL);

		if(mode == Mode.SINGLE) {
			radioGroup = new RadioGroup(context);
			radioGroup.setOrientation(LinearLayout.VERTICAL);
			contentView.addView(radioGroup);
		} else if(mode == Mode.MULTI) {
			chipGroup = new ChipGroup(context);
			chipGroup.setChipSpacingVertical(dpPx(chipGroup, -4));

			var chipsParams = new LinearLayoutCompat.LayoutParams(ViewUtil.MATCH_PARENT, ViewUtil.MATCH_PARENT);
			contentView.addView(chipGroup, chipsParams);
		}
	}

	private void setRadioItems(@NonNull Selection<T> items) {
		radioGroup.removeAllViews();

		for(var item : items) {
			var originalTitle = Selection.Selectable.getTitle(item.getKey());
			var title = Objects.requireNonNullElse(getString(R.string.class, originalTitle), originalTitle);

			var radio = new MaterialRadioButton(getContext());
			radio.setText(title);
			radio.setChecked(item.getValue() == Selection.State.SELECTED);

			radio.setOnCheckedChangeListener((v, isChecked) -> {
				items.setState(item.getKey(), isChecked ?
						Selection.State.SELECTED : Selection.State.UNSELECTED);

				if(isChecking) return;
				isChecking = true;

				for(int i = 0; i < radioGroup.getChildCount(); i++) {
					var child = radioGroup.getChildAt(i);
					if(child == radio) continue;

					if(child instanceof MaterialRadioButton materialRadio) {
						materialRadio.setChecked(false);
					} else {
						throw new IllegalStateException("Unexpected child type: " + child);
					}
				}

				isChecking = false;
			});

			radioGroup.addView(radio, ViewUtil.MATCH_PARENT, ViewUtil.WRAP_CONTENT);
		}
	}

	private void setChipItems(@NonNull Selection<T> items) {
		chipGroup.removeAllViews();

		for(var item : items) {
			var style = com.google.android.material.R.style.Widget_Material3_Chip_Filter;
			var contextWrapper = new ContextThemeWrapper(getContext(), style);

			var originalTitle = Selection.Selectable.getTitle(item.getKey());
			var title = Objects.requireNonNullElse(getString(R.string.class, originalTitle), originalTitle);

			var chip = new Chip(contextWrapper);
			chip.setCheckable(true);
			chip.setText(title);
			chip.setChecked(item.getValue() == Selection.State.SELECTED);

			chip.setOnCheckedChangeListener((_view, isChecked) ->
					items.setState(item.getKey(), isChecked ?
							Selection.State.SELECTED : Selection.State.UNSELECTED));

			chipGroup.addView(chip);
		}
	}

	@SuppressWarnings("unchecked")
	public E setItems(Selection<T> items) {
		this.items = items;

		if(mode == Mode.SINGLE) {
			setRadioItems(items);
		} else if(mode == Mode.MULTI) {
			setChipItems(items);
		} else {
			throw new IllegalArgumentException("Unknown mode! " + mode);
		}

		return (E) this;
	}

	public Selection<T> getSelection() {
		return items;
	}

	@Nullable
	@Override
	protected View getContentView(View parentView) {
		return contentView;
	}

	private enum Mode {
		MULTI, SINGLE
	}
}