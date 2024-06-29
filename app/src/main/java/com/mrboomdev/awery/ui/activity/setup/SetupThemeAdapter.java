package com.mrboomdev.awery.ui.activity.setup;

import static com.mrboomdev.awery.app.AweryApp.balloon;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivities;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;
import static com.mrboomdev.awery.data.settings.NicePreferences.getSettingsMap;
import static com.mrboomdev.awery.ui.ThemeManager.isDarkModeEnabled;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalMargin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.DynamicColors;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.databinding.WidgetCircleButtonBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.activity.settings.SettingsAdapter;
import com.mrboomdev.awery.ui.activity.settings.SettingsDataHandler;
import com.mrboomdev.awery.util.ui.RecyclerItemDecoration;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.skydoves.balloon.BalloonAlign;

import java.util.ArrayList;
import java.util.List;

public class SetupThemeAdapter extends RecyclerView.Adapter<SetupThemeAdapter.ViewHolder> {
	private final boolean isAmoled = AwerySettings.USE_AMOLED_THEME.getValue();
	private final Drawable materialYouDrawable, selectedDrawable;
	private final List<Theme> themes = new ArrayList<>();
	private final Context context;
	private boolean didSuggestYou = AwerySettings.DID_SUGGEST_MATERIAL_YOU.getValue(false);
	private Theme selected;

	@NonNull
	public static ConcatAdapter create(Context context) {
		return new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), SingleViewAdapter.fromViewDynamic(parent -> {
					var recycler = new RecyclerView(context);
					recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
					recycler.addItemDecoration(new RecyclerItemDecoration(dpPx(16)));
					recycler.setAdapter(new SetupThemeAdapter(context));
					setBottomPadding(recycler, dpPx(24));
					return recycler;
		}), new SettingsAdapter(new SettingsItem() {
			private final List<SettingsItem> items = new ArrayList<>();

			{
				items.add(getSettingsMap().findItem(AwerySettings.USE_DARK_THEME.getKey()));

				if(AwerySettings.USE_DARK_THEME.getValue()) {
					items.add(getSettingsMap().findItem(AwerySettings.USE_AMOLED_THEME.getKey()));
				}
			}

			@Override
			public List<? extends SettingsItem> getItems() {
				return items;
			}
		}, new SettingsDataHandler() {

			@Override
			public void onScreenLaunchRequest(SettingsItem item) {
				throw new UnsupportedOperationException("Nested screens aren't supported!");
			}

			@Override
			public void saveValue(SettingsItem item, Object newValue) {
				// NOTE: There are only boolean settings, so we don't expect other setting types.
				// If it'll change, add support for other setting types.
				getPrefs().setValue(item.getKey(), (Boolean) newValue).saveSync();

				if(AwerySettings.USE_DARK_THEME.getKey().equals(item.getKey())) {
					ThemeManager.applyApp(context);
					return;
				}

				for(var activity : getActivities(AppCompatActivity.class)) {
					activity.recreate();
				}
			}

			@Override
			public Object restoreValue(SettingsItem item) {
				if(AwerySettings.USE_DARK_THEME.getKey().equals(item.getKey())) {
					return getPrefs().getBoolean(item.getKey(), isDarkModeEnabled());
				}

				// NOTE: There are only boolean settings, so we don't expect other setting types.
				// If it'll change, add support for other setting types.
				return getPrefs().getBoolean(item.getKey(), item.getBooleanValue());
			}
		}) {

			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				var view = super.onCreateViewHolder(parent, viewType);
				setHorizontalMargin(view.itemView, dpPx(-16), dpPx(-16));
				return view;
			}
		});
	}

	/**
	 * Why this constructor is private? We just need a single adapter,
	 * but we can't inherit an ConcatAdapter,
	 * so instead we do expose an already prebuilt ConcatAdapter.
	 * @author MrBoomDev
	 */
	private SetupThemeAdapter(Context context) {
		var palette = ThemeManager.getCurrentColorPalette();
		this.context = context;

		selectedDrawable = returnWith(() -> {
			var drawable = new GradientDrawable();
			drawable.setCornerRadius(dpPx(16));
			drawable.setColor(resolveAttrColor(context, com.google.android.material.R.attr.colorOnSecondary));
			return drawable;
		});

		materialYouDrawable = returnWith(() -> {
			if(!DynamicColors.isDynamicColorAvailable()) {
				return null;
			}

			var icon = ContextCompat.getDrawable(context, R.drawable.ic_round_auto_awesome_24);

			var color = new GradientDrawable();
			color.setShape(GradientDrawable.OVAL);

			color.setColor(resolveAttrColor(new ContextThemeWrapper(context,
					ThemeManager.getThemeRes(AwerySettings.ThemeColorPalette_Values.MATERIAL_YOU, isAmoled)),
					com.google.android.material.R.attr.colorPrimary));

			var layered = new LayerDrawable(new Drawable[]{ color, icon });
			layered.setLayerGravity(1, Gravity.CENTER);
			layered.setLayerInset(1, dpPx(8), dpPx(8), dpPx(8), dpPx(8));

			return layered;
		});

		for(var theme : AwerySettings.ThemeColorPalette_Values.values()) {
			var setting = theme.findSetting();
			if(!setting.isVisible()) continue;

			themes.add(new Theme(theme, setting));
		}

		selected = find(themes, theme -> theme.getId().equals(palette.getKey()));
		setHasStableIds(true);
	}

	public Theme getSelected() {
		return selected;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(WidgetCircleButtonBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(themes.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return themes.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final GradientDrawable imageDrawable;
		private final WidgetCircleButtonBinding binding;
		private Theme theme;

		public ViewHolder(@NonNull WidgetCircleButtonBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot().setOnClickListener(v -> {
				selected = theme;
				notifyItemRangeChanged(0, themes.size());
				theme.apply();
			});

			imageDrawable = new GradientDrawable();
			imageDrawable.setShape(GradientDrawable.OVAL);

			binding.getRoot().setImageDrawable(imageDrawable);
			binding.getRoot().setVisibility(View.VISIBLE);
			binding.getRoot().setImageTintList(ColorStateList.valueOf(0));
		}

		public void bind(@NonNull Theme theme) {
			this.theme = theme;

			binding.getRoot().setBackground(this.theme == selected ? selectedDrawable
					: ContextCompat.getDrawable(context, R.drawable.button_popup_background));

			if(theme.palette == AwerySettings.ThemeColorPalette_Values.MATERIAL_YOU) {
				binding.getRoot().setImageDrawable(materialYouDrawable);
			} else {
				imageDrawable.setColor(resolveAttrColor(new ContextThemeWrapper(context,
						ThemeManager.getThemeRes(theme.palette, isAmoled)), com.google.android.material.R.attr.colorPrimary));

				binding.getRoot().setImageDrawable(imageDrawable);
			}

			if(theme.palette == AwerySettings.ThemeColorPalette_Values.MATERIAL_YOU && !didSuggestYou) {
				balloon(binding.getRoot(), "The app's color will be based on your wallpaper!", BalloonAlign.END);
				didSuggestYou = true;

				getPrefs().setValue(AwerySettings.DID_SUGGEST_MATERIAL_YOU, true).saveAsync();
			}

			binding.getRoot().setOnLongClickListener(v -> {
				if(theme.palette == AwerySettings.ThemeColorPalette_Values.MATERIAL_YOU) {
					balloon(binding.getRoot(), "The app's color will be based on your wallpaper!", BalloonAlign.END);
					return true;
				}

				return false;
			});
		}
	}

	public class Theme {
		private final AwerySettings.ThemeColorPalette_Values palette;
		private final SettingsItem item;

		public Theme(@NonNull AwerySettings.ThemeColorPalette_Values palette, SettingsItem item) {
			this.palette = palette;
			this.item = item;
		}

		public String getId() {
			return palette.getKey();
		}

		public String getName() {
			return item.getTitle(context);
		}

		@SuppressLint({"PrivateResource", "RestrictedApi"})
		public void apply() {
			getPrefs().setValue(AwerySettings.THEME_COLOR_PALETTE, palette).saveSync();

			for(var activity : getActivities(AppCompatActivity.class)) {
				activity.recreate();
			}
		}
	}
}