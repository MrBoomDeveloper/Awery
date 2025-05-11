package com.mrboomdev.awery.utils.exceptions

import java.io.IOException

class BotSecurityBypassException(
	val blocker: String, 
	message: String? = null,
	cause: Throwable? = null
): IOException(message, cause) {
	companion object {
		const val CLOUDFLARE = "Cloudflare"
	}
}