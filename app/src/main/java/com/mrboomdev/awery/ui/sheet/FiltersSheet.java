package com.mrboomdev.awery.ui.sheet;

import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.button.MaterialButton;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.util.ui.dialog.SheetDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class FiltersSheet extends SheetDialog {
	private static final WeakHashMap<ExtensionProvider, List<SettingsItem>> defaultFilters = new WeakHashMap<>();
	private final Map<SettingsItem, Object> pendingChanges = new HashMap<>();
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

	@Override
	public View getContentView(Context context) {
		var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);

		var actions = new LinearLayoutCompat(context);
		setHorizontalPadding(actions, dpPx(8));
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

			dismiss();
			applyCallback.run();
		});

		return linear;
	}
}