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
			is BotSecurityBypassException -> i18n(R.string.failed_to_bypass, t.blockerName)
			is ExtensionInstallException -> i18n(R.string.extension_installed_failed)
			is ExtensionLoadException -> i18n(R.string.extension_load_failed)
			is CancellationException -> "Operation cancelled"
			is UnsupportedOperationException -> "Unsupported action"
			is NotImplementedError -> "Unsupported action"

			is HttpException -> when(t.code) {
				400, 422 -> i18n(R.string.bad_request)
				403 -> i18n(R.string.access_denied)
				404 -> i18n(R.string.nothing_found)
				429 -> i18n(R.string.too_much_requests)
				500, 503 -> i18n(R.string.server_down)
				504 -> i18n(R.string.timed_out)
				else -> i18n(R.string.unknown_net_error)
			}

			else -> null
		}) ?: run {
			if(t.message?.contains(ROOM_EXCEPTION) == true) {
				return i18n(R.string.database_corrupted)
			}

			return@run i18n(R.string.something_went_wrong)
		}

	val message: String
		get() = when(t) {
			is LocalizedException -> t.getDescription(appContext)
			is SocketTimeoutException -> i18n(R.string.connection_timeout)
			is BotSecurityBypassException -> i18n(R.string.failed_bypass_detailed, t.blockerName)
			is SocketException -> i18n(R.string.failed_to_connect_to_server)
			is SSLHandshakeException -> i18n(R.string.failed_to_connect_to_server)
			is NotImplementedError -> t.message
			is ExtensionInstallException -> t.userReadableMessage
			is ExtensionLoadException -> t.userReadableMessage

			is HttpException -> "(${t.code}) " + when(t.code) {
				400, 422 -> i18n(R.string.request_invalid_detailed)
				401 -> i18n(R.string.not_logged_detailed)
				403 -> i18n(R.string.no_access_detailed)
				404 -> i18n(R.string.not_found_detailed)
				429 -> i18n(R.string.rate_limited_detailed)
				500 -> i18n(R.string.server_internal_error)
				503 -> i18n(R.string.server_unavailable)
				504 -> i18n(R.string.connection_timeout)
				else -> i18n(R.string.unknown_error)
			}

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
			is NoSuchElementException -> Category.NO_RESULTS
			is HttpException -> Category.FAILED_TO_CONNECT
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
			|| this is HttpException
			|| this is CancellationException
			|| this is LocalizedException
			|| this is NotImplementedError
			|| this is SocketTimeoutException
			|| this is BotSecurityBypassException
			|| this is SSLHandshakeException
			|| this is SocketException
			|| this is UnknownHostException

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