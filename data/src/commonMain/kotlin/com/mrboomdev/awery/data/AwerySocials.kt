package com.mrboomdev.awery.data

import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.logo_discord
import com.mrboomdev.awery.resources.logo_github
import com.mrboomdev.awery.resources.logo_telegram
import org.jetbrains.compose.resources.DrawableResource

data class SocialMedia(
	val logo: DrawableResource,
	val url: String
)

val AwerySocials = listOf(
	SocialMedia(
		logo = Res.drawable.logo_telegram,
		url = "https://t.me/mrboomdev_awery"
	),
	
	SocialMedia(
		logo = Res.drawable.logo_discord,
		url = "https://discord.com/invite/yspVzD4Kbm"
	),

	SocialMedia(
		logo = Res.drawable.logo_github,
		url = "https://github.com/MrBoomDeveloper/Awery"
	)
)