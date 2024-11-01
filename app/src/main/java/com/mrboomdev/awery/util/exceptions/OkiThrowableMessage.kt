package com.mrboomdev.awery.util.exceptions

import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.i18n
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.ext.util.exceptions.ExtensionLoadException
import eu.kanade.tachiyomi.network.HttpException
import kotlinx.coroutines.CancellationException
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

private const val ROOM_EXCEPTION = "Room cannot verify the data integrity. " +
		"Looks like you've changed schema but forgot to update the version number."

class OkiThrowableMessage(
	throwable: Throwable,
	unwrapper: ((throwable: Throwable) -> Boolean) = { it.mayDescribe }
) {
	val throwable = throwable.unwrap(unwrapper)

	val title: String
		get() = when(throwable) {
			is SocketTimeoutException -> i18n(R.string.timed_out)
			is SSLHandshakeException -> i18n(R.string.failed_handshake)
			is BotSecurityBypassException -> "Failed to bypass an security"
			is ExtensionInstallException -> "Failed to install an extension"
			is ExtensionLoadException -> "Failed to load an extension"
			is CancellationException -> "Operation been cancelled"
			is UnsupportedOperationException -> "Unsupported action"
			is NotImplementedError -> "Unsupported action"
			else -> "Unknown error has occurred"
		}

	val message: String?
		get() = when(throwable) {
			is ExtensionInstallException -> throwable.userReadableMessage
			is ExtensionLoadException -> throwable.userReadableMessage

			is BotSecurityBypassException -> "Your request has been blocked by \"${throwable.blockerName}\". " +
					"Try completing an captcha and refreshing to resolve this problem"

			else -> null
		} ?: throwable.message

	val category: Category
		get() = when(throwable) {
			is SocketTimeoutException -> Category.TIMEOUT
			is SSLHandshakeException -> Category.FAILED_TO_CONNECT
			is ZeroResultsException -> Category.NO_RESULTS
			else -> Category.UNKNOWN
		}

	enum class Category {
		NO_RESULTS,
		FAILED_TO_CONNECT,
		TIMEOUT,
		UNKNOWN
	}
}

private fun Throwable.unwrap(unwrapper: ((throwable: Throwable) -> Boolean) = { it.mayDescribe }): Throwable {
	var t: Throwable? = this

	while(t != null) {
		if(unwrapper(t)) {
			return t
		}

		if(t.cause == t) {
			return t
		}

		t = t.cause
	}

	return this
}

private val Throwable.mayDescribe: Boolean
	get() = this is ExtensionLoadException
			|| this is ExtensionInstallException
			|| this is UnsupportedOperationException
			|| this is CancellationException
			|| this is NotImplementedError
			|| this is SocketTimeoutException
			|| this is BotSecurityBypassException
			|| this is SSLHandshakeException

private val Throwable.isNetworkExceptionImpl
	get() = this is SocketTimeoutException ||
			this is SocketException ||
			this is HttpException ||
			this is BotSecurityBypassException ||
			this is SSLHandshakeException

val Throwable.isNetworkException: Boolean
	get() {
		var t: Throwable? = this

		while(t != null) {
			if(t.isNetworkExceptionImpl) {
				return true
			}

			if(t.cause == t) {
				return false
			}

			t = t.cause
		}

		return false
	}