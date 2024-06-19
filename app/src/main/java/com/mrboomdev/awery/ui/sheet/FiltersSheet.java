package com.mrboomdev.awery.ui.sheet;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.util.ui.dialog.SheetDialog;
import com.mrboomdev.awery.util.ui.fields.EditTextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FiltersSheet extends SheetDialog {
	private static final List<String> HIDDEN_FILTERS = List.of(ExtensionProvider.FILTER_QUERY, ExtensionProvider.FILTER_PAGE);
	private final Map<SettingsItem, Object> pendingChanges = new HashMap<>();
	private final List<SettingsItem> newFilters = new ArrayList<>();
	private final Runnable applyCallback;
	private final List<SettingsItem> filters;
	private final ExtensionProvider provider;

	private static final int ACTION_BUTTON_STYLE =
			com.google.android.material.R.style.Widget_Material3_Button_TextButton;

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

	private void createFilterView(@NonNull SettingsItem filter, @NonNull ViewGroup into) {
		if(HIDDEN_FILTERS.contains(filter.getKey())) {
			return;
		}

		switch(filter.getType()) {
			case STRING -> {
				var field = new EditTextField(getContext());
				field.setHint(filter.getTitle(getContext()));
				field.setText(filter.getStringValue());
				into.addView(field.getView(), MATCH_PARENT, WRAP_CONTENT);
				setMargin(field.getView(), dpPx(8));
			}

			case INTEGER -> {
				var field = new EditTextField(getContext());
				field.setType(EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
				field.setHint(filter.getTitle(getContext()));
				field.setText(filter.getIntegerValue());
				into.addView(field.getView(), MATCH_PARENT, WRAP_CONTENT);
				setMargin(field.getView(), dpPx(8));
			}

			case BOOLEAN -> {
				var toggle = new MaterialSwitch(getContext());
				toggle.setText(filter.getTitle(getContext()));
				toggle.setChecked(Objects.requireNonNullElse(filter.getBooleanValue(), false));
				into.addView(toggle, MATCH_PARENT, WRAP_CONTENT);
				setMargin(toggle, dpPx(8));
			}

			case CATEGORY -> {
				var text = new AppCompatTextView(getContext());
				text.setText(filter.getTitle(getContext()));
				into.addView(text, MATCH_PARENT, WRAP_CONTENT);
				setMargin(text, dpPx(8));
			}

			default -> toast("Unsupported filter type! " + filter.getType());
		}
	}

	@Override
	public View getContentView(Context context) {
		var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);

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
		setPadding(actions, dpPx(8));
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
						//createFilterView(filter, fields);
					}
				});
			}

			@Override
			public void onFailure(Throwable e) {
				runOnUiThread(() -> progressBar.setVisibility(View.GONE));
			}
		});

		return linear;
	}
}