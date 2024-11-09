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
import com.mrboomdev.awery.app.App.Companion.i18n
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getActivities
import com.mrboomdev.awery.app.data.settings.NicePreferences
import com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs
import com.mrboomdev.awery.app.data.settings.SettingsItem
import com.mrboomdev.awery.databinding.WidgetCircleButtonBinding
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.generated.AwerySettings.ThemeColorPalette_Values
import com.mrboomdev.awery.app.ThemeManager
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsAdapter
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsDataHandler
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.balloon
import com.mrboomdev.awery.util.extensions.bottomPadding
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.setHorizontalMargin
import com.mrboomdev.awery.util.ui.RecyclerItemDecoration
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import com.skydoves.balloon.BalloonAlign

class SetupThemeAdapter private constructor(context: Context) : RecyclerView.Adapter<SetupThemeAdapter.ViewHolder?>() {
	private val isAmoled = AwerySettings.USE_AMOLED_THEME.value
	private val materialYouDrawable: Drawable?
	private val selectedDrawable: Drawable
	private val themes: MutableList<Theme> = ArrayList()
	private val context: Context
	private var didSuggestYou = AwerySettings.DID_SUGGEST_MATERIAL_YOU.getValue(false)
	var selected: Theme?

	/**
	 * Why this constructor is private? We just need a single adapter,
	 * but we can't inherit an ConcatAdapter,
	 * so instead we do expose an already prebuilt ConcatAdapter.
	 * @author MrBoomDev
	 */
	init {
		val palette = ThemeManager.getCurrentColorPalette()
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
					setColor(ContextThemeWrapper(context,
						ThemeManager.getThemeRes(ThemeColorPalette_Values.MATERIAL_YOU, isAmoled)
					).resolveAttrColor(com.google.android.material.R.attr.colorPrimary))
				},

				ContextCompat.getDrawable(context, R.drawable.ic_round_auto_awesome_24)
			))
		}

		for(theme in ThemeColorPalette_Values.entries.toTypedArray()) {
			val setting = theme.findSetting()
			if(!setting.isVisible) continue

			themes.add(Theme(theme, setting))
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

			if(theme.palette == ThemeColorPalette_Values.MATERIAL_YOU) {
				binding.root.setImageDrawable(materialYouDrawable)
			} else {
				imageDrawable.setColor(ContextThemeWrapper(context,
					ThemeManager.getThemeRes(theme.palette, isAmoled
					)).resolveAttrColor(com.google.android.material.R.attr.colorPrimary)
				)

				binding.root.setImageDrawable(imageDrawable)
			}

			if(theme.palette == ThemeColorPalette_Values.MATERIAL_YOU && !didSuggestYou) {
				binding.root.balloon(i18n(R.string.wallpaper_based_colors), BalloonAlign.END)
				didSuggestYou = true
				getPrefs().setValue(AwerySettings.DID_SUGGEST_MATERIAL_YOU, true).saveAsync()
			}

			binding.root.setOnLongClickListener {
				if(theme.palette == ThemeColorPalette_Values.MATERIAL_YOU) {
					binding.root.balloon(i18n(R.string.wallpaper_based_colors), BalloonAlign.END)
					return@setOnLongClickListener true
				}

				false
			}
		}
	}

	inner class Theme(val palette: ThemeColorPalette_Values, private val item: SettingsItem) {
		val id: String
			get() = palette.key

		val name: String
			get() = item.getTitle(context)

		@SuppressLint("PrivateResource", "RestrictedApi")
		fun apply() {
			getPrefs().setValue(AwerySettings.THEME_COLOR_PALETTE, palette).saveSync()

			for(activity in getActivities<AppCompatActivity>()) {
				activity.recreate()
			}
		}
	}

	companion object {
		@JvmStatic
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
						items.add(NicePreferences.getSettingsMap().findItem(AwerySettings.USE_DARK_THEME.key))

						if(AwerySettings.USE_DARK_THEME.value) {
							items.add(NicePreferences.getSettingsMap().findItem(AwerySettings.USE_AMOLED_THEME.key))
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
							ThemeManager.applyApp(context)
							return
						}

						for(activity in getActivities<AppCompatActivity>()) {
							activity.recreate()
							activity.applyTheme()
						}
					}

					override fun restoreValue(item: SettingsItem): Any {
						if(AwerySettings.USE_DARK_THEME.key == item.key) {
							return getPrefs().getBoolean(item.key, ThemeManager.isDarkModeEnabled())
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