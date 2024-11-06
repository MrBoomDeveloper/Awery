package com.mrboomdev.awery.ui.activity.settings

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.compose.material.Card
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.textview.MaterialTextView
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.app.App.Companion.getMarkwon
import com.mrboomdev.awery.app.App.Companion.i18n
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.openUrl
import com.mrboomdev.awery.databinding.ScreenAboutBinding
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.bottomMargin
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.leftMargin
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.setImageTintAttr
import com.mrboomdev.awery.util.extensions.setPadding
import com.mrboomdev.awery.util.extensions.topMargin
import java.util.Date

class AboutActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		applyTheme()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		val binding = ScreenAboutBinding.inflate(layoutInflater)
		binding.root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))
		setContentView(binding.root)

		binding.back.setOnClickListener { finish() }

		binding.version.text = arrayOf(
			"${i18n(R.string.version)}: ${BuildConfig.VERSION_NAME}",
			"${i18n(R.string.built_at)}: ${Date(BuildConfig.BUILD_TIME)}"
		).joinToString("\n")

		getMarkwon(this).setMarkdown(
			binding.info.fundMessage,
			binding.info.fundMessage.text.toString()
		)

		binding.applyInsets(UI_INSETS, { _, insets ->
			if(isLandscape) {
				binding.root.setPadding(insets.left, insets.top, insets.right, insets.bottom)
				window.navigationBarColor = resolveAttrColor(android.R.attr.colorBackground)
			} else {
				binding.root.setPadding(insets.left, 0, insets.right, insets.bottom)
				binding.header.topMargin = insets.top
			}

			true
		})
	}

	class ContributorsView @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0
	) : LinearLayoutCompat(context, attrs, defStyleAttr) {

		init {
			orientation = VERTICAL

			for((name, roles, url, avatar) in listOf(
				Contributor(
					"MrBoomDev", arrayOf("Main Developer"), "https://github.com/MrBoomDeveloper",
					"https://cdn.discordapp.com/avatars/1034891767822176357/3420c6a4d16fe513a69c85d86cb206c2.png?size=4096"
				),

				Contributor(
					"Ichiro", arrayOf("App Icon"),
					"https://discord.com/channels/@me/1262060731981889536",
					"https://cdn.discordapp.com/avatars/778503249619058689/9d5baf6943f4eafbaf09eb8e9e287f2d.png?size=4096"
				)
			)) {
				val linear = LinearLayoutCompat(context).apply {
					orientation = HORIZONTAL
					setBackgroundResource(R.drawable.ripple_round_you)
					this@ContributorsView.addView(this, MATCH_PARENT, WRAP_CONTENT)
					setPadding(dpPx(8f))
					bottomMargin = dpPx(4f)

					isClickable = true
					isFocusable = true
					setOnClickListener { App.openUrl(context, url) }
				}

				val iconWrapper = CardView(context).apply {
					radius = dpPx(48f).toFloat()
					linear.addView(this)
				}

				AppCompatImageView(context).apply {
					iconWrapper.addView(this, dpPx(48f), dpPx(48f))

					Glide.with(this)
						.load(avatar)
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(this)
				}

				val info = LinearLayoutCompat(context).apply {
					orientation = VERTICAL
					linear.addView(this)
					leftMargin = dpPx(16f)
				}

				MaterialTextView(context).apply {
					text = name
					setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
					setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnBackground))
					info.addView(this)
					bottomMargin = dpPx(4f)
				}

				MaterialTextView(context).apply {
					setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
					text = roles.joinToString(", ")
					info.addView(this)
				}
			}
		}
	}

	class SocialView @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0
	) : LinearLayoutCompat(context, attrs, defStyleAttr) {

		init {
			val linear = LinearLayoutCompat(context).apply {
				gravity = Gravity.CENTER_HORIZONTAL
				orientation = VERTICAL
				isClickable = true
				isFocusable = true
				setBackgroundResource(R.drawable.ripple_round_you)
				this@SocialView.addView(this)
				setPadding(dpPx(12f), dpPx(8f))
			}

			val icon = AppCompatImageView(context).apply {
				linear.addView(this, dpPx(42f), dpPx(42f))
				setImageTintAttr(com.google.android.material.R.attr.colorOnSecondaryContainer)
			}

			val label = MaterialTextView(context).apply {
				linear.addView(this, WRAP_CONTENT, WRAP_CONTENT)
				topMargin = dpPx(4f)
			}

			if(attrs != null) {
				context.obtainStyledAttributes(attrs, R.styleable.SocialView).use { typed ->
					icon.setImageDrawable(typed.getDrawable(R.styleable.SocialView_socialIcon))
					label.text = typed.getString(R.styleable.SocialView_socialName)

					typed.getString(R.styleable.SocialView_socialLink)?.let { url ->
						linear.setOnClickListener { openUrl(context, url) }
					}
				}
			}
		}
	}

	data class Contributor(
		val name: String,
		val roles: Array<String>,
		val url: String,
		val avatar: String
	)
}