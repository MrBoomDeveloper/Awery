package com.mrboomdev.awery.ui.mobile.screens.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.databinding.ScreenSetupBinding
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.PlatformSettingHandler
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.mobile.screens.SplashActivity
import com.mrboomdev.awery.ui.mobile.screens.setup.SetupThemeAdapter.Companion.create
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.setImageTintAttr
import com.mrboomdev.awery.util.extensions.setMarkwon
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.ui.RecyclerItemDecoration
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.dpPx
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import com.mrboomdev.safeargsnext.util.rememberSafeArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartyFactory
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape.Circle
import nl.dionsegijn.konfetti.core.models.Shape.Square
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SetupActivity : AppCompatActivity(), SafeArgsActivity<SetupActivity.Extras> {
	private lateinit var binding: ScreenSetupBinding

	data class Extras(val step: Int, val finishOnComplete: Boolean = false)

	override fun onCreate(savedInstanceState: Bundle?) {
		applyTheme()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		binding = ScreenSetupBinding.inflate(layoutInflater)
		binding.root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))

		binding.root.applyInsets(UI_INSETS, { view, insets ->
			view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
			false
		})

		binding.continueButton.setOnClickListener { tryToStartNextStep() }
		binding.backButton.setOnClickListener { finish() }

		if(rememberSafeArgs?.finishOnComplete == true) {
			binding.backButton.visibility = View.GONE
			binding.continueButton.text = i18n(Res.string.done)
		}

		when(intent.getIntExtra("step", STEP_WELCOME)) {
			STEP_WELCOME -> {
				if(AwerySettings.SETUP_VERSION_FINISHED.value != -1) {
					binding.title.text = i18n(Res.string.awery_updated_title)
					binding.message.text = i18n(Res.string.awery_updated_description)
					binding.backButton.visibility = View.GONE
				}

				binding.backButton.text = i18n(Res.string.restore_backup)
				binding.continueButton.text = i18n(Res.string.lets_begin)

				binding.backButton.setOnClickListener {
					PlatformSettingHandler.handlePlatformClick(this, AwerySettings.RESTORE.asPlatformSetting())
				}

				binding.icon.setImageResource(R.mipmap.ic_launcher_foreground)
				binding.icon.visibility = View.VISIBLE
			}

			STEP_THEMING -> {
				binding.title.text = i18n(Res.string.color_palette)
				binding.message.text = i18n(Res.string.color_palette_description)

				binding.recycler.layoutManager = LinearLayoutManager(this)
				binding.recycler.adapter = create(this)
				binding.recycler.visibility = View.VISIBLE

				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_palette_filled)

				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
				binding.recycler.applyInsets(UI_INSETS, { _, _ -> true })
			}

			STEP_TEMPLATE -> {
				binding.title.text = i18n(Res.string.select_template)
				binding.message.text = i18n(Res.string.generic_message)

				binding.recycler.layoutManager = LinearLayoutManager(this)
				binding.recycler.addItemDecoration(RecyclerItemDecoration(dpPx(8f)))
				binding.recycler.adapter = SetupTabsAdapter()
				binding.recycler.visibility = View.VISIBLE

				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_amp_stories_filled)
				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
			}

			STEP_SOURCES -> {
				if(AwerySettings.TABS_TEMPLATE.value == "dantotsu") {
				    binding.message.setMarkwon(i18n(Res.string.dantotsu_message, "https://discord.com/invite/yspVzD4Kbm", "https://t.me/mrboomdev_awery"))
				} else {
				    binding.message.setMarkwon(i18n(Res.string.generic_message, "https://discord.com/invite/yspVzD4Kbm", "https://t.me/mrboomdev_awery"))
				}

				binding.title.text = i18n(Res.string.extensions)
				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_extension_filled)
				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
			}

			STEP_ANALYTICS -> {
			    binding.title.text = i18n(Res.string.analytics_title)
			    binding.message.text = i18n(Res.string.analytics_message)
			    binding.recycler.visibility = View.VISIBLE
			
			    binding.recycler.adapter = SingleViewAdapter.fromView(MaterialSwitch(this).apply {
			        text = i18n(Res.string.automatically_send_reports)
			        isChecked = true
			    })
			}

			STEP_FINISH -> {
				binding.konfetti.start(
					createParty(250, 300, 0, 360, 0, .5),
					createParty(275, 225, 300, 200, 150, .4),
					createParty(275, 225, 300, 200, 330, .6))

				binding.title.text = i18n(Res.string.were_done)
				binding.message.text = i18n(Res.string.setup_finished_description)
				binding.continueButton.text = i18n(Res.string.finish)

				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_done)
				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
			}

			else -> throw IllegalArgumentException("Unknown step! " + intent.getIntExtra("step", -1))
		}

		setContentView(binding.root)
	}

	private fun tryToStartNextStep() {
		when(rememberSafeArgs?.step ?: STEP_WELCOME) {
			STEP_TEMPLATE -> {
				val selected = (binding.recycler.adapter as SetupTabsAdapter).selected

				if(selected == null) {
					AwerySettings.TABS_TEMPLATE.value = "awery"
					startNextStep()
					return
				}

				binding.continueButton.isEnabled = false
				binding.backButton.isEnabled = false

				lifecycleScope.launch(Dispatchers.IO) {
					val tabsDao = database.tabsDao
					val feedsDao = database.feedsDao

					val tabs = tabsDao.allTabs

					if(tabs.isNotEmpty()) {
						val didDeleted = AtomicBoolean()

						runOnUiThread {
							DialogBuilder(this@SetupActivity)
							    .setTitle(i18n(Res.string.existing_custom_tabs_title))
							    .setMessage(i18n(Res.string.existing_custom_tabs_message))
							    .setOnDismissListener {
							        if (!didDeleted.get()) {
							            binding.backButton.isEnabled = true
							            binding.continueButton.isEnabled = true
							        }
							    }
								.setNegativeButton(i18n(Res.string.cancel)) { it.dismiss() }
								.setPositiveButton(i18n(Res.string.delete)) { dialog ->
									didDeleted.set(true)
									dialog.dismiss()

									lifecycleScope.launch(Dispatchers.IO) {
										for(tab in tabs) {
											for(feed in feedsDao.getAllFromTab(tab.id)) {
												feedsDao.delete(feed)
											}

											tabsDao.delete(tab)
										}

										tryToStartNextStep()
									}
								}.show()
						}

						return@launch
					}

					runOnUiThread {
						AwerySettings.TABS_TEMPLATE.value = selected.id
						startNextStep()
						binding.continueButton.isEnabled = true
						binding.backButton.isEnabled = true
					}
				}
			}

			else -> startNextStep()
		}
	}

	private fun startNextStep() {
		if(rememberSafeArgs?.finishOnComplete == true) {
			setResult(RESULT_OK)
			finish()
			return
		}

		val nextStep = when(rememberSafeArgs?.step ?: STEP_WELCOME) {
			STEP_WELCOME -> STEP_THEMING
			STEP_THEMING -> STEP_TEMPLATE
			STEP_TEMPLATE -> STEP_SOURCES
			STEP_SOURCES -> STEP_FINISH
			STEP_ANALYTICS -> STEP_FINISH

			STEP_FINISH -> {
				AwerySettings.SETUP_VERSION_FINISHED.value = SETUP_VERSION
				finishAffinity()
				startActivity(buildIntent(SplashActivity::class))
				return
			}

			else -> throw IllegalArgumentException("Unknown step!")
		}

		startActivity(buildIntent(SetupActivity::class, Extras(nextStep)))
	}

	private fun createParty(durationMs: Int, amountPerSec: Int, delayMs: Int, spread: Int, angle: Int, x: Double): Party {
		return PartyFactory(Emitter(durationMs.toLong(), TimeUnit.MILLISECONDS).perSecond(amountPerSec))
			.delay(delayMs)
			.spread(spread)
			.angle(angle)
			.timeToLive(4000)
			.shapes(listOf(Square, Circle))
			.setSpeedBetween(0f, 30f)
			.position(Position.Relative(x, .3))
			.build()
	}
	
	companion object {
		// Note: Please increment this value by one every time when a new step is being added
		const val SETUP_VERSION = 2

		const val STEP_WELCOME = 0
		const val STEP_FINISH = 1
		const val STEP_TEMPLATE = 2
		const val STEP_THEMING = 3
		const val STEP_SOURCES = 4
		const val STEP_ANALYTICS = 5
	}
}