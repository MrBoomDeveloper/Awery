@file:OptIn(ExperimentalStdlibApi::class)

package com.mrboomdev.awery.ui.mobile.screens.setup

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.data.settings.SettingsList
import com.mrboomdev.awery.databinding.ItemListSettingBinding
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.util.TabsTemplate
import com.mrboomdev.awery.util.extensions.clearImageTint
import com.mrboomdev.awery.util.extensions.context
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.readAssets
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.resolveDrawable
import com.mrboomdev.awery.util.extensions.scale
import com.mrboomdev.awery.util.extensions.setImageTintAttr
import com.mrboomdev.awery.util.extensions.topMargin
import com.mrboomdev.awery.utils.dpPx
import com.mrboomdev.awery.utils.inflater
import com.squareup.moshi.adapter
import java.io.File

class SetupTabsAdapter : RecyclerView.Adapter<SetupTabsAdapter.ViewHolder?>() {
	private val templates: MutableList<TabsTemplate> = ArrayList()
	private var selectedDrawable: Drawable? = null
	var selected: TabsTemplate? = null

	init {
		// TODO: 6/27/2024 Return this option when custom feeds will be done
		/*templates.add(new TabsTemplate() {{
			this.id = "custom";
			this.title = "Custom";
			this.icon = "ic_settings_outlined";
			this.description = "Are you a power user who likes to customize everything? You must like it!";
		}});*/

		templates.addAll(getMoshi(SettingsList.ADAPTER).adapter<List<TabsTemplate>>()
			.fromJson(File("tabs_templates.json").readAssets())!!)

		val savedSelected = AwerySettings.TABS_TEMPLATE.value
		selected = templates.find { it.id == savedSelected }
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(ItemListSettingBinding.inflate(
			parent.context.inflater, parent, false
		).apply {
			title.topMargin = title.dpPx(-5f)
			checkbox.visibility = View.GONE
			toggle.visibility = View.GONE
			divider.visibility = View.GONE
			options.visibility = View.GONE
		})
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(templates[position])
	}

	override fun getItemCount(): Int {
		return templates.size
	}

	inner class ViewHolder(private val binding: ItemListSettingBinding) : RecyclerView.ViewHolder(binding.root) {
		private var template: TabsTemplate? = null

		init {
			binding.root.setOnClickListener {
				selected = template
				notifyItemRangeChanged(0, templates.size)
			}

			/* We need only a single instance so we lazily init an shared object */
			if(selectedDrawable == null) {
				selectedDrawable = GradientDrawable().apply {
					cornerRadius = binding.context.dpPx(16f).toFloat()
					setColor(binding.context.resolveAttrColor(R.attr.colorOnSecondary))
				}
			}
		}

		fun bind(template: TabsTemplate) {
			this.template = template

			binding.title.text = template.title
			binding.description.text = template.description
			binding.icon.visibility = if(template.icon != null) View.VISIBLE else View.INVISIBLE

			if(template.icon != null) {
				binding.icon.setImageDrawable(binding.context.resolveDrawable(template.icon))

				if(binding.icon.drawable is VectorDrawable) {
					binding.icon.setImageTintAttr(R.attr.colorOnSecondaryContainer)
					binding.icon.scale = 1f
				} else {
					binding.icon.clearImageTint()
					binding.icon.scale = 1.2f
				}
			}

			binding.root.background = if(template !== selected) null else selectedDrawable
		}
	}
}