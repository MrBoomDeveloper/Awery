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
import com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs
import com.mrboomdev.awery.app.data.settings.SettingsItem
import com.mrboomdev.awery.databinding.ScreenSetupBinding
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.mobile.screens.SplashActivity
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActions
import com.mrboomdev.awery.ui.mobile.screens.setup.SetupThemeAdapter.Companion.create
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.setImageTintAttr
import com.mrboomdev.awery.util.extensions.setMarkwon
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.ui.RecyclerItemDecoration
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
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

class SetupActivity : AppCompatActivity() {
	private lateinit var binding: ScreenSetupBinding

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

		if(intent.getBooleanExtra(EXTRA_FINISH_ON_COMPLETE, false)) {
			binding.backButton.visibility = View.GONE
			binding.continueButton.setText(R.string.done)
		}

		when(intent.getIntExtra("step", STEP_WELCOME)) {
			STEP_WELCOME -> {
				if(AwerySettings.SETUP_VERSION_FINISHED.value != 0) {
					binding.title.setText(R.string.awery_updated_title)
					binding.message.setText(R.string.awery_updated_description)
					binding.backButton.visibility = View.GONE
				}

				binding.backButton.setText(R.string.restore_backup)
				binding.continueButton.setText(R.string.lets_begin)

				binding.backButton.setOnClickListener {
					SettingsActions.run(SettingsItem.Builder().setKey(AwerySettings.RESTORE).build())
				}

				binding.icon.setImageResource(R.mipmap.ic_launcher_foreground)
				binding.icon.visibility = View.VISIBLE
			}

			STEP_THEMING -> {
				binding.title.setText(R.string.color_palette)
				binding.message.setText(R.string.color_palette_description)

				binding.recycler.layoutManager = LinearLayoutManager(this)
				binding.recycler.adapter = create(this)
				binding.recycler.visibility = View.VISIBLE

				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_palette_filled)

				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
				binding.recycler.applyInsets(UI_INSETS, { _, _ -> true })
			}

			STEP_TEMPLATE -> {
				binding.title.setText(R.string.select_template)
				binding.message.text = "The content you see through the app. You can select it at any time in Settings."

				binding.recycler.layoutManager = LinearLayoutManager(this)
				binding.recycler.addItemDecoration(RecyclerItemDecoration(dpPx(8f)))
				binding.recycler.adapter = SetupTabsAdapter()
				binding.recycler.visibility = View.VISIBLE

				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_amp_stories_filled)
				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
			}

			STEP_SOURCES -> {
				val template = AwerySettings.TABS_TEMPLATE.value

				if(template == "dantotsu") {
					binding.message.setMarkwon("""
						The Dantotsu template requires some extensions to work.
						In this beta version, you cannot install extensions directly through the app :(
						Currently you can download them on our:
						Discord server: https://discord.com/invite/yspVzD4Kbm
						Telegram channel: https://t.me/mrboomdev_awery
						""".trimIndent())
				} else {
					binding.message.setMarkwon("""
						In this beta version, you cannot install extensions directly through the app :(
						Currently you can download them on our:
						Discord server: https://discord.com/invite/yspVzD4Kbm
						Telegram channel: https://t.me/mrboomdev_awery
						""".trimIndent())
				}

				binding.title.setText(R.string.extensions)
				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_extension_filled)
				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
			}

			STEP_ANALYTICS -> {
				binding.title.text = "Analytics"
				binding.message.text = "TODO"
				binding.recycler.visibility = View.VISIBLE

				binding.recycler.adapter = SingleViewAdapter.fromView(MaterialSwitch(this).apply {
					text = "Automatically send crash reports"
					isChecked = true
				})
			}

			STEP_FINISH -> {
				binding.konfetti.start(
					createParty(250, 300, 0, 360, 0, .5),
					createParty(275, 225, 300, 200, 150, .4),
					createParty(275, 225, 300, 200, 330, .6))

				binding.title.setText(R.string.were_done)
				binding.message.setText(R.string.setup_finished_description)
				binding.continueButton.setText(R.string.finish)

				binding.icon.visibility = View.VISIBLE
				binding.icon.setImageResource(R.drawable.ic_done)
				binding.icon.setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
			}

			else -> throw IllegalArgumentException("Unknown step! " + intent.getIntExtra("step", -1))
		}

		setContentView(binding.root)
	}

	private fun tryToStartNextStep() {
		when(intent.getIntExtra(EXTRA_STEP, STEP_WELCOME)) {
			STEP_TEMPLATE -> {
				val selected = (binding.recycler.adapter as SetupTabsAdapter).selected

				if(selected == null) {
					getPrefs().removeValue(AwerySettings.TABS_TEMPLATE).saveSync()
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
								.setTitle("Existing custom tabs found")
								.setMessage("To use an template we have to delete all your custom tabs with feeds. If you don't want that, then simply select \"None\".")
								.setOnDismissListener {
									if(!didDeleted.get()) {
										binding.backButton.isEnabled = true
										binding.continueButton.isEnabled = true
									}
								}
								.setNegativeButton(R.string.cancel) { it.dismiss() }
								.setPositiveButton(R.string.delete) { dialog ->
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
						getPrefs().setValue(AwerySettings.TABS_TEMPLATE, selected.id).saveAsync()
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
		if(intent.getBooleanExtra(EXTRA_FINISH_ON_COMPLETE, false)) {
			setResult(RESULT_OK)
			finish()
			return
		}

		val nextStep = when(intent.getIntExtra(EXTRA_STEP, STEP_WELCOME)) {
			STEP_WELCOME -> STEP_THEMING
			STEP_THEMING -> STEP_TEMPLATE
			STEP_TEMPLATE -> STEP_SOURCES
			STEP_SOURCES -> STEP_FINISH
			STEP_ANALYTICS -> STEP_FINISH

			STEP_FINISH -> {
				getPrefs().setValue(AwerySettings.SETUP_VERSION_FINISHED, SETUP_VERSION).saveSync()
				finishAffinity()
				startActivity(SplashActivity::class)
				return
			}

			else -> throw IllegalArgumentException("Unknown step!")
		}

		startStep(nextStep)
	}

	private fun startStep(step: Int) {
		val intent = Intent(this, SetupActivity::class.java)
		intent.putExtra(EXTRA_STEP, step)
		startActivity(intent)
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
		/**
		 * Note: Please increment this value by one every time when a new step is being added
		 */
		const val SETUP_VERSION: Int = 2

		const val EXTRA_STEP: String = "step"
		const val EXTRA_FINISH_ON_COMPLETE: String = "finish_on_complete"
		const val STEP_WELCOME: Int = 0
		const val STEP_FINISH: Int = 1
		const val STEP_TEMPLATE: Int = 2
		const val STEP_THEMING: Int = 3
		const val STEP_SOURCES: Int = 4
		const val STEP_ANALYTICS: Int = 5
	}
}