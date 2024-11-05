package com.mrboomdev.awery.util.exceptions

import android.util.Log
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.i18n
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.ext.util.exceptions.ExtensionLoadException
import eu.kanade.tachiyomi.network.HttpException
import kotlinx.coroutines.CancellationException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

private const val ROOM_EXCEPTION = "Room cannot verify the data integrity. " +
		"Looks like you've changed schema but forgot to update the version number."

class OkiThrowableMessage(
	throwable: Throwable,
	unwrapper: ((throwable: Throwable) -> Boolean) = { it.mayDescribe }
) {
	private val t = throwable.unwrap(unwrapper)

	val title: String
		get() = (when(t) {
			is LocalizedException -> t.getTitle(appContext)
			is SocketTimeoutException -> i18n(R.string.timed_out)
			is SSLHandshakeException -> i18n(R.string.failed_handshake)
			is SocketException -> t.message
			is UnknownHostException -> t.message
			is BotSecurityBypassException -> "Failed to bypass ${t.blockerName}"
			is ExtensionInstallException -> "Failed to install an extension"
			is ExtensionLoadException -> "Failed to load an extension"
			is CancellationException -> "Operation cancelled"
			is UnsupportedOperationException -> "Unsupported action"
			is NotImplementedError -> "Unsupported action"

			is HttpException -> when(t.code) {
				400, 422 -> "Bad request"
				403 -> i18n(R.string.access_denied)
				404 -> i18n(R.string.nothing_found)
				429 -> i18n(R.string.too_much_requests)
				500, 503 -> i18n(R.string.server_down)
				504 -> i18n(R.string.timed_out)
				else -> "Unknown network error"
			}

			else -> null
		}) ?: run {
			if(t.message?.contains(ROOM_EXCEPTION) == true) {
				return "Database corrupted!"
			}

			return@run i18n(R.string.something_went_wrong)
		}

	val message: String
		get() = when(t) {
			is LocalizedException -> t.getDescription(appContext)
			is SocketTimeoutException -> i18n(R.string.connection_timeout)
			is SocketException -> "Failed to connect to the server!"
			is SSLHandshakeException -> "Failed to connect to the server!"
			is NotImplementedError -> t.message
			is ExtensionInstallException -> t.userReadableMessage
			is ExtensionLoadException -> t.userReadableMessage

			is HttpException -> "(${t.code}) " + when(t.code) {
				400, 422 -> "The request was invalid, please try again later."
				401 -> "You are not logged in, please log in and try again."
				403 -> "You have no access to this resource, try logging into your account."
				404 -> i18n(R.string.not_found_detailed)
				429 -> "You have exceeded the rate limit, please try again later."
				500 -> "An internal server error has occurred, please try again later."
				503 -> "The server is currently unavailable, please try again later."
				504 -> "The connection timed out, please try again later."
				else -> i18n(R.string.unknown_error)
			}

			is BotSecurityBypassException -> "Your request has been blocked by \"${t.blockerName}\". " +
					"Try completing an captcha and refreshing to resolve this problem"

			else -> null
		} ?: run {
			if(t.message?.contains(ROOM_EXCEPTION) == true) {
				return@run """
		Yeah, you've hear right. The database has been corrupted!
		How can you fix it? Clear app data.
		
		Please, do not use alpha versions to keep your library. Use them only to test new things.
		
		${Log.getStackTraceString(t)}
		""".trimIndent()
			}

			return@run Log.getStackTraceString(t)
		}

	val category: Category
		get() = when(t) {
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

private fun Throwable.unwrap(
	unwrapper: ((throwable: Throwable) -> Boolean) = { it.mayDescribe }
): Throwable {
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