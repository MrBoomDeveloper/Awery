package com.mrboomdev.awery.ui.mobile.screens.setup

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getActivities
import com.mrboomdev.awery.app.theme.ThemeManager
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.asSetting
import com.mrboomdev.awery.data.settings.NicePreferences.getPrefs
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.databinding.WidgetCircleButtonBinding
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.data.getRecursively
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.PlatformResources
import com.mrboomdev.awery.platform.android.AndroidGlobals
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsAdapter
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsDataHandler
import com.mrboomdev.awery.util.extensions.balloon
import com.mrboomdev.awery.util.extensions.bottomPadding
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.setHorizontalMargin
import com.mrboomdev.awery.util.ui.RecyclerItemDecoration
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import com.mrboomdev.awery.utils.dpPx
import com.skydoves.balloon.BalloonAlign

class SetupThemeAdapter private constructor(context: Context) : RecyclerView.Adapter<SetupThemeAdapter.ViewHolder?>() {
	private val materialYouDrawable: Drawable?
	private val selectedDrawable: Drawable
	private val themes: MutableList<Theme> = ArrayList()
	private val context: Context
	var selected: Theme?

	/**
	 * Why this constructor is private? We just need a single adapter,
	 * but we can't inherit an ConcatAdapter,
	 * so instead we do expose an already prebuilt ConcatAdapter.
	 * @author MrBoomDev
	 */
	init {
		val palette = ThemeManager.currentColorPalette
		this.context = context

		selectedDrawable = GradientDrawable().apply {
			cornerRadius = context.dpPx(16f).toFloat()
			setColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnSecondary))
		}

		materialYouDrawable = run {
			if(!DynamicColors.isDynamicColorAvailable()) {
				return@run null
			}

			LayerDrawable(arrayOf(
				GradientDrawable().apply {
					shape = GradientDrawable.OVAL
					
					val themeRes = ThemeManager.getThemeRes(AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU, AwerySettings.USE_AMOLED_THEME.value)
					setColor(ContextThemeWrapper(context, themeRes).resolveAttrColor(com.google.android.material.R.attr.colorPrimary))
				},

				ContextCompat.getDrawable(context, R.drawable.ic_round_auto_awesome_24)
			))
		}

		for(theme in AwerySettings.ThemeColorPaletteValue.entries.toTypedArray()) {
			if(theme == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU && !DynamicColors.isDynamicColorAvailable()) continue
			themes.add(Theme(theme, AwerySettings.maps.SYSTEM_SETTINGS.items!!.getRecursively(theme.key)!!))
		}

		selected = themes.find { it.id == palette.key }
		setHasStableIds(true)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			WidgetCircleButtonBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(themes[position])
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun getItemCount(): Int {
		return themes.size
	}

	inner class ViewHolder(private val binding: WidgetCircleButtonBinding) : RecyclerView.ViewHolder(binding.root) {
		private val imageDrawable: GradientDrawable
		private var theme: Theme? = null

		init {
			binding.root.setOnClickListener {
				selected = theme
				notifyItemRangeChanged(0, themes.size)
				theme!!.apply()
			}

			imageDrawable = GradientDrawable()
			imageDrawable.shape = GradientDrawable.OVAL

			binding.root.setImageDrawable(imageDrawable)
			binding.root.visibility = View.VISIBLE
			binding.root.imageTintList = ColorStateList.valueOf(0)
		}

		fun bind(theme: Theme) {
			this.theme = theme

			binding.root.background = if(this.theme === selected) selectedDrawable
			else ContextCompat.getDrawable(context, R.drawable.ui_button_popup_background)

			if(theme.palette == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU) {
				binding.root.setImageDrawable(materialYouDrawable)
			} else {
				val themeRes = ThemeManager.getThemeRes(theme.palette, AwerySettings.USE_AMOLED_THEME.value)
				imageDrawable.setColor(ContextThemeWrapper(context, themeRes).resolveAttrColor(com.google.android.material.R.attr.colorPrimary))
				binding.root.setImageDrawable(imageDrawable)
			}

			if(theme.palette == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU && !AwerySettings.DID_SUGGEST_MATERIAL_YOU.value) {
				binding.root.balloon(i18n(Res.string.wallpaper_based_colors), BalloonAlign.BOTTOM)
				AwerySettings.DID_SUGGEST_MATERIAL_YOU.value = true
			}

			binding.root.setOnLongClickListener {
				if(theme.palette == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU) {
					binding.root.balloon(i18n(Res.string.wallpaper_based_colors), BalloonAlign.BOTTOM)
					return@setOnLongClickListener true
				}

				false
			}
		}
	}

	inner class Theme(
		val palette: AwerySettings.ThemeColorPaletteValue, 
		private val item: Setting
	) {
		val id: String
			get() = palette.key

		val name: String
			get() = item.title!!

		@SuppressLint("PrivateResource", "RestrictedApi")
		fun apply() {
			AwerySettings.THEME_COLOR_PALETTE.value = palette

			for(activity in getActivities<AppCompatActivity>()) {
				activity.recreate()
			}
		}
	}

	companion object {
		fun create(context: Context): ConcatAdapter {
			return ConcatAdapter(
				ConcatAdapter.Config.Builder()
					.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
					.build(), SingleViewAdapter.fromViewDynamic {
					RecyclerView(context).apply {
						layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
						adapter = SetupThemeAdapter(context)
						bottomPadding = dpPx(24f)
						addItemDecoration(RecyclerItemDecoration(dpPx(16f)))
					}
				}, object : SettingsAdapter(object : SettingsItem() {
					private val items: MutableList<SettingsItem> = ArrayList()

					init {
						items.add(AwerySettings.USE_DARK_THEME.asSetting())

						if(AwerySettings.USE_DARK_THEME.value == true) {
							items.add(AwerySettings.USE_AMOLED_THEME.asSetting())
						}
					}

					override fun getItems(): List<SettingsItem> {
						return items
					}
				}, object : SettingsDataHandler {
					override fun onScreenLaunchRequest(item: SettingsItem) {
						throw UnsupportedOperationException("Nested screens aren't supported!")
					}

					override fun saveValue(item: SettingsItem, newValue: Any) {
						// NOTE: There are only boolean settings, so we don't expect other setting types.
						// If it'll change, add support for other setting types.
						getPrefs().setValue(item.key, (newValue as Boolean)).saveSync()

						if(AwerySettings.USE_DARK_THEME.key == item.key) {
							AndroidGlobals.applicationContext.applyTheme()
							return
						}

						for(activity in getActivities<AppCompatActivity>()) {
							activity.recreate()
							activity.applyTheme()
						}
					}

					override fun restoreValue(item: SettingsItem): Any {
						if(AwerySettings.USE_DARK_THEME.key == item.key) {
							return getPrefs().getBoolean(item.key, ThemeManager.isDarkModeEnabled)
						}

						// NOTE: There are only boolean settings, so we don't expect other setting types.
						// If it'll change, add support for other setting types.
						return getPrefs().getBoolean(item.key, item.booleanValue)
					}
				}) {
					override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
						return super.onCreateViewHolder(parent, viewType).apply {
							itemView.setHorizontalMargin(context.dpPx(-16f))
						}
					}
				})
		}
	}
}