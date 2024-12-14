package com.mrboomdev.awery.ui.mobile.screens.media

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigationrail.NavigationRailView
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.share
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.databinding.ScreenMediaDetailsBinding
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.extensions.data.CatalogVideo
import com.mrboomdev.awery.AwerySettings
import com.mrboomdev.awery.util.MediaUtils
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.ui.FadeTransformer
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import com.mrboomdev.safeargsnext.util.rememberSafeArgs

class MediaActivity : AppCompatActivity(), SafeArgsActivity<MediaActivity.Extras> {
	private lateinit var binding: ScreenMediaDetailsBinding
	private var commentsFragment: MediaCommentsFragment? = null
	private var pendingExtra: Any? = null

	enum class Action {
		WATCH, INFO, COMMENTS
	}

	data class Extras(val media: CatalogMedia, val action: Action? = null)

	@SuppressLint("NonConstantResourceId")
	override fun onCreate(savedInstanceState: Bundle?) {
		applyTheme()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		binding = ScreenMediaDetailsBinding.inflate(layoutInflater)
		binding.root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))

		binding.pager.apply {
			isUserInputEnabled = false
			adapter = PagerAdapter(supportFragmentManager, lifecycle)
			setPageTransformer(FadeTransformer())
		}

		binding.navigation.apply {
			if(AwerySettings.USE_AMOLED_THEME.value == true) {
				setBackgroundColor(0x00000000)
			}

			if(this is NavigationRailView) {
				addHeaderView(FloatingActionButton(
					context,
					null,
					com.google.android.material.R.attr.floatingActionButtonSmallSecondaryStyle
				).apply {
					background = ContextCompat.getDrawable(context, R.drawable.ripple_circle_white)
					setImageResource(R.drawable.ic_back)
					setOnClickListener { finish() }
				})
			}

			applyInsets(UI_INSETS, { view, insets ->
				if(AwerySettings.USE_AMOLED_THEME.value == true) {
					view.setBackgroundColor(-0x1000000)
					window.navigationBarColor = if(isLandscape) 0 else -0x1000000
				} else {
					view.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(view.context))
					window.navigationBarColor = if(isLandscape) 0 else SurfaceColors.SURFACE_2.getColor(view.context)
				}

				if(view is NavigationRailView) {
					view.setPadding(insets.left, insets.top, 0, 0)
				} else {
					view.setPadding(0, 0, 0, insets.bottom)
				}

				true
			})

			setOnItemSelectedListener { item ->
				binding.pager.setCurrentItem(
					when(item.itemId) {
						R.id.info -> 0
						R.id.watch -> 1
						R.id.comments -> 2
						R.id.relations -> 3
						else -> throw IllegalArgumentException("Invalid item id: " + item.itemId)
					}, false
				)

				true
			}
		}

		launchAction(rememberSafeArgs?.action ?: Action.INFO)
		setContentView(binding.root)

		if(rememberSafeArgs?.media?.type == CatalogMedia.Type.POST ||
			rememberSafeArgs?.media?.type == CatalogMedia.Type.BOOK
		) {
			binding.navigation.menu.findItem(R.id.watch).apply {
				setIcon(R.drawable.ic_book)
				setTitle(R.string.read)
			}
		}
	}

	@JvmOverloads
	fun launchAction(action: Action, payload: Any? = null) {
		binding.navigation.selectedItemId = when(action) {
			Action.INFO -> R.id.info
			Action.WATCH -> R.id.watch

			Action.COMMENTS -> {
				if(commentsFragment != null) {
					commentsFragment!!.setEpisode(payload as CatalogVideo?)
				} else {
					pendingExtra = payload
				}

				R.id.comments
			}
		}
	}

	private inner class PagerAdapter(
		fragmentManager: FragmentManager,
		lifecycle: Lifecycle
	) : FragmentStateAdapter(fragmentManager, lifecycle) {
		override fun createFragment(position: Int) = when(position) {
			0 -> MediaInfoFragment().apply { putSafeArgs(MediaInfoFragment.Args(
				this@MediaActivity.rememberSafeArgs!!.media)) }

			1 -> MediaPlayFragment().apply { putSafeArgs(MediaPlayFragment.Args(
				this@MediaActivity.rememberSafeArgs!!.media)) }

			2 -> MediaCommentsFragment(
				this@MediaActivity.rememberSafeArgs!!.media,
				pendingExtra as CatalogVideo?
			).also { commentsFragment = it }

			3 -> MediaRelationsFragment().apply { putSafeArgs(MediaRelationsFragment.Args(
				this@MediaActivity.rememberSafeArgs!!.media)) }

			else -> throw IllegalArgumentException("Invalid position: $position")
		}

		override fun getItemCount() = 4
	}

	companion object {

		@JvmStatic
		fun handleOptionsClick(anchor: View, media: CatalogMedia) {
			val context = ContextThemeWrapper(anchor.context, anchor.context.theme)
			val popup = PopupMenu(context, anchor)

			if(media.url != null) {
				popup.menu.add(0, 0, 0, R.string.share)
			}

			popup.menu.add(0, 1, 0, R.string.blacklist)

			popup.setOnMenuItemClickListener { item ->
				when(item.itemId) {
					0 -> {
						share(media.url!!)
						true
					}

					1 -> {
						MediaUtils.blacklistMedia(media) { toast("Blacklisted successfully") }
						true
					}

					else -> false
				}
			}

			popup.show()
		}
	}
}