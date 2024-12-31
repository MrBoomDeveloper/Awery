package com.mrboomdev.awery.ui.mobile.dialogs

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.data.settings.CustomSettingsItem
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.data.settings.SettingsItemType
import com.mrboomdev.awery.data.settings.SettingsList
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsAdapter
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsDataHandler
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.extensions.addView
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.leftMargin
import com.mrboomdev.awery.util.extensions.rightMargin
import com.mrboomdev.awery.util.extensions.setHorizontalPadding
import com.mrboomdev.awery.util.extensions.setPadding
import com.mrboomdev.awery.util.extensions.setVerticalPadding
import com.mrboomdev.awery.util.extensions.topPadding
import com.mrboomdev.awery.util.extensions.useLayoutParams
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import org.jetbrains.annotations.Contract

class FiltersDialog(
	filters: SettingsList,
	private val provider: ExtensionProvider? = null,
	private val applyCallback: ((updatedFilters: SettingsList) -> Unit)? = null
) : BasePanelDialog(), SettingsDataHandler {
	private val filters: SettingsList = SettingsList(filters
		.filter { !HIDDEN_FILTERS.contains(it.key) }
		.map { object : CustomSettingsItem(it) {} })

	override fun getView(context: Context): View {
		val screenAdapter = SettingsAdapter(object : SettingsItem() {
			override fun getItems(): List<SettingsItem> {
				return filters
			}
		}, this)

		val progressBarAdapter = SingleViewAdapter.fromViewDynamic {
			CircularProgressIndicator(context).apply {
				layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
				isIndeterminate = true
				setVerticalPadding(dpPx(8f))
			}
		}

		val adapter = ConcatAdapter(
			ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), screenAdapter, progressBarAdapter)

		val linear = LinearLayoutCompat(context).apply {
			orientation = LinearLayoutCompat.VERTICAL
			gravity = Gravity.CENTER_VERTICAL
		}

		val recycler = RecyclerView(context).apply {
			layoutManager = LinearLayoutManager(context)
			topPadding = dpPx(8f)
			clipToPadding = false
			setAdapter(adapter)
			setHorizontalPadding(dpPx(8f))
			linear.addView(this, MATCH_PARENT, 0, 1f)
		}

		if(applyCallback != null) {
			LinearLayoutCompat(context).apply {
				setPadding(dpPx(16f), dpPx(8f))
				linear.addView(this, MATCH_PARENT, WRAP_CONTENT)

				addView(MaterialButton(context).apply {
					setText(R.string.cancel)
					setOnClickListener { dismiss() }
				}, 0, WRAP_CONTENT) { cancel ->
					cancel.useLayoutParams<LinearLayoutCompat.LayoutParams> { it.weight = 1f }
					cancel.rightMargin = dpPx(6f)
				}

				addView(MaterialButton(context).apply {
					setText(R.string.save)
					setOnClickListener {
						applyCallback.invoke(this@FiltersDialog.filters)
						dismiss()
					}
				}, 0, WRAP_CONTENT) { save ->
					save.useLayoutParams<LinearLayoutCompat.LayoutParams> { it.weight = 1f }
					save.leftMargin = dpPx(6f)
				}
			}
		}

		if(provider == null) {
			progressBarAdapter.isEnabled = false
		} else {
			provider.filters.addCallback(object : AsyncFuture.Callback<SettingsList?> {
				override fun onSuccess(result: SettingsList) {
					if(!isShowing) return

					mergeFilters(result, filters)
					filters.clear()
					filters.addAll(result)

					runOnUiThread({
						progressBarAdapter.isEnabled = false
						screenAdapter.setItems(result, true)
					}, recycler)
				}

				override fun onFailure(t: Throwable) {
					Log.e(TAG, "Failed to load filters!", t)
					toast("Failed to load filters")
					runOnUiThread({ progressBarAdapter.isEnabled = false }, recycler)
				}
			})
		}

		return linear
	}

	@Contract(pure = true)
	private fun mergeFilters(original: List<SettingsItem>, values: List<SettingsItem>) {
		for(originalItem in original) {
			val found = values.find { originalItem.key == it.key }
			if(found == null) continue

			if(originalItem.items != null && found.items != null) {
				mergeFilters(originalItem.items, found.items)
			}

			if(originalItem.type != null) {
				when(originalItem.type) {
					SettingsItemType.BOOLEAN, SettingsItemType.SCREEN_BOOLEAN ->
						originalItem.setValue(found.booleanValue ?: originalItem.booleanValue)

					SettingsItemType.STRING, SettingsItemType.SELECT, SettingsItemType.JSON, SettingsItemType.SERIALIZABLE ->
						originalItem.setValue(found.stringValue ?: originalItem.stringValue)

					SettingsItemType.INTEGER, SettingsItemType.SELECT_INTEGER, SettingsItemType.COLOR ->
						originalItem.setValue(found.integerValue ?: originalItem.integerValue)

					SettingsItemType.EXCLUDABLE ->
						originalItem.setValue(found.excludableValue ?: originalItem.excludableValue)

					SettingsItemType.MULTISELECT ->
						originalItem.setValue(found.stringSetValue ?:  originalItem.stringSetValue)

					SettingsItemType.DATE ->
						originalItem.setValue(found.longValue ?: originalItem.longValue)

					else -> {}
				}
			}
		}
	}

	override fun onScreenLaunchRequest(item: SettingsItem) {
		FiltersDialog(SettingsList(item.items)).show(context!!)
	}

	override fun saveValue(item: SettingsItem, newValue: Any) {}

	override fun restoreValue(item: SettingsItem): Any {
		return when(item.type) {
			SettingsItemType.INTEGER, SettingsItemType.SELECT_INTEGER -> item.integerValue
			SettingsItemType.BOOLEAN, SettingsItemType.SCREEN_BOOLEAN -> item.booleanValue
			SettingsItemType.STRING, SettingsItemType.SELECT -> item.stringValue
			SettingsItemType.DATE -> item.longValue
			SettingsItemType.EXCLUDABLE -> item.excludableValue
			else -> null
		}!!
	}

	companion object {
		private const val TAG = "FiltersDialog"
		private val HIDDEN_FILTERS: List<String> = listOf(
			ExtensionProvider.FILTER_QUERY, ExtensionProvider.FILTER_PAGE
		)
	}
}