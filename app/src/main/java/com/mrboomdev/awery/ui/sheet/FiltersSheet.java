package com.mrboomdev.awery.ui.sheet;

import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.getTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.util.Selection;
import com.mrboomdev.awery.util.ui.dialog.SheetDialog;
import com.mrboomdev.awery.util.ui.fields.EditTextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FiltersSheet extends SheetDialog {
	private static final List<String> HIDDEN_FILTERS = List.of(
			ExtensionProvider.FILTER_QUERY, ExtensionProvider.FILTER_PAGE);

	private final Map<SettingsItem, Object> pendingChanges = new HashMap<>();
	private final List<SettingsItem> newFilters = new ArrayList<>();
	private final Runnable applyCallback;
	private final List<SettingsItem> filters;
	private final ExtensionProvider provider;

	public FiltersSheet(
			Context context,
			List<SettingsItem> filters,
			ExtensionProvider provider,
			Runnable applyCallback
	) {
		super(context);
		this.filters = filters;
		this.provider = provider;
		this.applyCallback = applyCallback;
	}

	@SuppressLint("ResourceType")
	private void createFilterView(
			@NonNull SettingsItem filter,
			@NonNull ViewGroup into
	) {
		if(HIDDEN_FILTERS.contains(filter.getKey())) {
			return;
		}

		var holder = new CardView(getContext());
		holder.setBackgroundTintList(ColorStateList.valueOf(0));
		holder.setCardElevation(2);

		switch(filter.getType()) {
			case STRING -> {
				var field = new EditTextField(getContext());
				field.setHint(filter.getTitle(getContext()));
				field.setText(filter.getStringValue());
				holder.addView(field.getView(), MATCH_PARENT, WRAP_CONTENT);
				setMargin(field.getView(), dpPx(24), dpPx(6));

				field.setEditListener(text -> pendingChanges.put(filter, text));
			}

			case INTEGER -> {
				var field = new EditTextField(getContext());
				field.setType(EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
				field.setHint(filter.getTitle(getContext()));
				field.setText(filter.getIntegerValue());
				holder.addView(field.getView(), MATCH_PARENT, WRAP_CONTENT);
				setMargin(field.getView(), dpPx(24), dpPx(6));

				field.setEditListener(text -> pendingChanges.put(filter, text));
			}

			case BOOLEAN -> {
				var toggle = new MaterialSwitch(getContext());
				toggle.setText(filter.getTitle(getContext()));
				toggle.setChecked(Objects.requireNonNullElse(filter.getBooleanValue(), false));
				holder.addView(toggle, MATCH_PARENT, WRAP_CONTENT);
				setMargin(toggle, dpPx(24), dpPx(6));

				toggle.setOnCheckedChangeListener((v, checked) -> pendingChanges.put(filter, checked));
			}

			case CATEGORY -> {
				var text = new AppCompatTextView(getContext());
				text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				text.setTypeface(text.getTypeface(), Typeface.BOLD);
				text.setText(filter.getTitle(getContext()));
				holder.addView(text, MATCH_PARENT, WRAP_CONTENT);
				setMargin(text, dpPx(24), dpPx(6));
				setVerticalPadding(text, dpPx(16));

				text.setTextColor(resolveAttrColor(getContext(),
						com.google.android.material.R.attr.colorOnBackground));
			}

			case EXCLUDABLE -> {
				var checkbox = new MaterialCheckBox(getContext());
				checkbox.setText(filter.getTitle(getContext()));
				holder.addView(checkbox, MATCH_PARENT, WRAP_CONTENT);
				setMargin(checkbox, dpPx(24), dpPx(2));

				if(filter.getExcludableValue() == Selection.State.DISABLED) {
					checkbox.setEnabled(false);
				}

				var initialState = switch(Objects.requireNonNullElse(filter.getExcludableValue(), Selection.State.UNSELECTED)) {
					case SELECTED -> MaterialCheckBox.STATE_CHECKED;
					case EXCLUDED -> MaterialCheckBox.STATE_INDETERMINATE;
					case DISABLED, UNSELECTED -> MaterialCheckBox.STATE_UNCHECKED;
				};

				checkbox.setCheckedState(initialState);

				class Listener implements CompoundButton.OnCheckedChangeListener {
					public boolean isSwitching;
					private int state;

					Listener(int state) {
						this.state = state;
					}

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isSwitching) return;
						isSwitching = true;

						state = switch(state) {
							case MaterialCheckBox.STATE_UNCHECKED -> MaterialCheckBox.STATE_CHECKED;
							case MaterialCheckBox.STATE_CHECKED -> MaterialCheckBox.STATE_INDETERMINATE;
							case MaterialCheckBox.STATE_INDETERMINATE -> MaterialCheckBox.STATE_UNCHECKED;
							default -> throw new IllegalStateException("Unknown state! " + state);
						};

						pendingChanges.put(filter, switch(state) {
							case MaterialCheckBox.STATE_UNCHECKED -> Selection.State.UNSELECTED;
							case MaterialCheckBox.STATE_CHECKED -> Selection.State.SELECTED;
							case MaterialCheckBox.STATE_INDETERMINATE -> Selection.State.EXCLUDED;
							default -> throw new IllegalStateException("Unknown state! " + state);
						});

						checkbox.setCheckedState(state);
						isSwitching = false;
					}
				}

				checkbox.setOnCheckedChangeListener(new Listener(initialState));
			}

			case SCREEN -> {
				var newInto = new LinearLayoutCompat(getContext());
				newInto.setOrientation(LinearLayoutCompat.VERTICAL);
				holder.addView(newInto, MATCH_PARENT, WRAP_CONTENT);
				setVerticalMargin(newInto, dpPx(6));

				var title = new AppCompatTextView(getContext());
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				title.setText(filter.getTitle(getContext()));
				newInto.addView(title);
				setMargin(title, dpPx(24), dpPx(6));

				for(var item : filter.getItems()) {
					createFilterView(item, newInto);
				}
			}

			case DIVIDER -> {
				var divider = new MaterialDivider(getContext());
				setVerticalMargin(divider, dpPx(8));
				into.addView(divider, MATCH_PARENT, WRAP_CONTENT);
				return;
			}

			case SELECT -> {
				var linear = new LinearLayoutCompat(getContext());
				linear.setOrientation(LinearLayoutCompat.VERTICAL);
				holder.addView(linear, MATCH_PARENT, WRAP_CONTENT);
				setVerticalMargin(linear, dpPx(6));

				for(int i = 0; i < filter.getItems().size(); i++) {
					final int finalI = i;
					var item = filter.getItems().get(i);

					var view = new LinearLayoutCompat(getContext());
					view.setClickable(true);
					view.setFocusable(true);
					view.setBackgroundResource(R.drawable.ripple_rect_you);
					linear.addView(view, MATCH_PARENT, WRAP_CONTENT);
					setPadding(view, dpPx(24), dpPx(16));

					var title = new AppCompatTextView(getContext());
					title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					title.setTextColor(resolveAttrColor(getContext(), com.google.android.material.R.attr.colorOnBackground));
					title.setText(item.getTitle(getContext()));
					view.addView(title);
					useLayoutParams(title, params -> params.weight = 1, LinearLayoutCompat.LayoutParams.class);

					var icon = new AppCompatImageView(getContext());
					icon.setImageResource(R.drawable.ic_done);
					icon.setId(1);
					view.addView(icon, dpPx(24), dpPx(24));

					view.setOnClickListener(v -> {
						for(int j = 0; j < linear.getChildCount(); j++) {
							var child = linear.getChildAt(j);
							var iconChild = child.findViewById(1);
							iconChild.setVisibility(finalI == j ? View.VISIBLE : View.INVISIBLE);
						}

						pendingChanges.put(filter, finalI);
					});

					icon.setVisibility((item.getKey().equals(filter.getStringValue())) ? View.VISIBLE : View.INVISIBLE);
				}
			}

			default -> {
				toast("Unsupported filter type! " + filter.getType());
				return;
			}
		}

		if(into.getChildCount() == 0) {
			setTopPadding(holder, holder.getPaddingTop() + getTopMargin(holder));
			setTopMargin(holder, 0);
		}

		into.addView(holder, MATCH_PARENT, WRAP_CONTENT);
	}

	@Override
	public View getContentView(Context context) {
		var scroller = new ScrollView(context);

		var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);
		scroller.addView(linear);
		setTopMargin(linear, dpPx(3));

		var fields = new LinearLayoutCompat(context);
		fields.setOrientation(LinearLayoutCompat.VERTICAL);
		linear.addView(fields);

		for(var filter : filters) {
			createFilterView(filter, fields);
		}

		var progressBar = new ProgressBar(context);
		linear.addView(progressBar, MATCH_PARENT, WRAP_CONTENT);
		setVerticalMargin(progressBar, dpPx(8));

		var actions = new LinearLayoutCompat(context);
		setPadding(actions, dpPx(16), dpPx(8));
		linear.addView(actions, MATCH_PARENT, WRAP_CONTENT);

		var cancel = new MaterialButton(context);
		cancel.setText(R.string.cancel);
		actions.addView(cancel, 0, WRAP_CONTENT);
		cancel.setOnClickListener(v -> dismiss());
		useLayoutParams(cancel, params -> params.weight = 1, LinearLayoutCompat.LayoutParams.class);
		setRightMargin(cancel, dpPx(6));

		var save = new MaterialButton(context);
		save.setText(R.string.save);
		actions.addView(save, 0, WRAP_CONTENT);
		useLayoutParams(save, params -> params.weight = 1, LinearLayoutCompat.LayoutParams.class);
		setLeftMargin(cancel, dpPx(6));

		save.setOnClickListener(v -> {
			for(var entry : pendingChanges.entrySet()) {

			}

			filters.addAll(newFilters);

			dismiss();
			applyCallback.run();
		});

		provider.getFilters(new ExtensionProvider.ResponseCallback<>() {

			@Override
			public void onSuccess(List<SettingsItem> filters) {
				if(!isShown()) return;

				runOnUiThread(() -> {
					progressBar.setVisibility(View.GONE);

					for(var filter : filters) {
						createFilterView(filter, fields);
					}
				});
			}

			@Override
			public void onFailure(Throwable e) {
				runOnUiThread(() -> progressBar.setVisibility(View.GONE));
			}
		});

		return scroller;
	}
}