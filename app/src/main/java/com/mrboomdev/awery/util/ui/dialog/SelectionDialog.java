package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.AweryApp.getString;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Objects;

public final class SelectionDialog<T> extends BaseDialogBuilder<SelectionDialog<T>> {
	private Selection<T> items = Selection.empty();
	private final Mode mode;
	private RadioGroup radioGroup;
	private ChipGroup chipGroup;
	private boolean isChecking;

	public SelectionDialog(Context context, Mode mode) {
		super(context);
		this.mode = mode;

		var contentView = new LinearLayoutCompat(context);
		contentView.setOrientation(LinearLayoutCompat.VERTICAL);

		if(mode == Mode.SINGLE) {
			radioGroup = new RadioGroup(context);
			radioGroup.setOrientation(LinearLayout.VERTICAL);
			contentView.addView(radioGroup);
		} else if(mode == Mode.MULTI) {
			chipGroup = new ChipGroup(context);
			chipGroup.setChipSpacingVertical(dpPx(-4));

			var chipsParams = new LinearLayoutCompat.LayoutParams(ViewUtil.MATCH_PARENT, ViewUtil.MATCH_PARENT);
			contentView.addView(chipGroup, chipsParams);
		}

		addView(contentView);
	}

	public SelectionDialog<T> setItems(Selection<T> items) {
		this.items = items;

		if(mode == Mode.SINGLE) {
			radioGroup.removeAllViews();

			for(var item : items) {
				var originalTitle = Selection.Selectable.getTitle(item.getKey());
				var title = Objects.requireNonNullElse(getString(getContext(), originalTitle), originalTitle);

				var radio = new MaterialRadioButton(getContext());
				radio.setText(title);
				radio.setChecked(item.getValue() == Selection.State.SELECTED);
				radio.setEnabled(item.getValue() != Selection.State.DISABLED);

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
		} else if(mode == Mode.MULTI) {
			chipGroup.removeAllViews();

			for(var item : items) {
				var style = com.google.android.material.R.style.Widget_Material3_Chip_Filter;
				var contextWrapper = new ContextThemeWrapper(getContext(), style);

				var originalTitle = Selection.Selectable.getTitle(item.getKey());
				var title = Objects.requireNonNullElse(getString(getContext(), originalTitle), originalTitle);

				var chip = new Chip(contextWrapper);
				chip.setCheckable(true);
				chip.setText(title);
				chip.setChecked(item.getValue() == Selection.State.SELECTED);
				chip.setEnabled(item.getValue() != Selection.State.DISABLED);

				chip.setOnCheckedChangeListener((_view, isChecked) ->
						items.setState(item.getKey(), isChecked ?
								Selection.State.SELECTED : Selection.State.UNSELECTED));

				chipGroup.addView(chip);
			}
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
		return items;
	}

	public interface SelectionListener<T> {
		void onSelected(SelectionDialog<T> dialog, Selection<T> data);
	}

	public enum Mode {
		MULTI, SINGLE
	}
}