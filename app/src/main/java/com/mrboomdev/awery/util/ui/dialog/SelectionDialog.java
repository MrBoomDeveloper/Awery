package com.mrboomdev.awery.util.ui.dialog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.mrboomdev.awery.app.App.i18n;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.util.Selection;

import java.util.Objects;

public final class SelectionDialog<T> extends BaseDialogBuilder<SelectionDialog<T>> {
	private final Mode mode;
	private final LinearLayoutCompat contentView;
	@Nullable
	private Selection<T> items;
	private RadioGroup radioGroup;
	private ChipGroup chipGroup;
	private boolean isChecking;
	
	@NonNull
	public static <T> SelectionDialog<T> single(Context context, Selection<T> items) {
		var dialog = new SelectionDialog<T>(context, Mode.SINGLE);
		dialog.setItems(items);
		return dialog;
	}
	
	@NonNull
	public static <T> SelectionDialog<T> multi(Context context, Selection<T> items) {
		var dialog = new SelectionDialog<T>(context, Mode.MULTI);
		dialog.setItems(items);
		return dialog;
	}

	public SelectionDialog(Context context, Mode mode) {
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

			var chipsParams = new LinearLayoutCompat.LayoutParams(MATCH_PARENT, MATCH_PARENT);
			contentView.addView(chipGroup, chipsParams);
		}
	}

	private void setRadioItems(@NonNull Selection<T> items) {
		radioGroup.removeAllViews();

		for(var item : items) {
			var originalTitle = Selection.Selectable.getTitle(item.getKey());
			var title = Objects.requireNonNullElse(i18n(R.string.class, originalTitle), originalTitle);

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

			radioGroup.addView(radio, MATCH_PARENT, WRAP_CONTENT);
		}
	}

	private void setChipItems(@NonNull Selection<T> items) {
		chipGroup.removeAllViews();

		for(var item : items) {
			var style = com.google.android.material.R.style.Widget_Material3_Chip_Filter;
			var contextWrapper = new ContextThemeWrapper(getContext(), style);

			var originalTitle = Selection.Selectable.getTitle(item.getKey());
			var title = Objects.requireNonNullElse(i18n(R.string.class, originalTitle), originalTitle);

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

	public SelectionDialog<T> setItems(Selection<T> items) {
		this.items = items;

		if(mode == Mode.SINGLE) {
			setRadioItems(items);
		} else if(mode == Mode.MULTI) {
			setChipItems(items);
		} else {
			throw new IllegalArgumentException("Unknown mode! " + mode);
		}

		return this;
	}

	public SelectionDialog<T> setPositiveButton(String label, SelectionListener<T> listener) {
		return setPositiveButton(label, dialog -> {
			if(listener != null) listener.onSelected(this, getSelection());
		});
	}

	public SelectionDialog<T> setPositiveButton(@StringRes int label, SelectionListener<T> listener) {
		return setPositiveButton(label, dialog -> {
			if(listener != null) listener.onSelected(this, getSelection());
		});
	}

	public Selection<T> getSelection() {
		if(items == null) {
			return Selection.empty();
		}
		
		return items;
	}

	@Nullable
	@Override
	protected View getContentView(View parentView) {
		return contentView;
	}

	public interface SelectionListener<T> {
		void onSelected(SelectionDialog<T> dialog, Selection<T> data);
	}

	public enum Mode {
		MULTI, SINGLE
	}
}