package com.mrboomdev.awery.utils.exceptions

import com.mrboomdev.awery.ext.util.LocaleAware
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.ext.util.exceptions.ExtensionLoadException
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n
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
	val unwrapped = throwable.unwrap(unwrapper)
	
	fun print(): String {
		val title = this.title
		val message = this.message
		
		if(title == message) {
			return title
		}
		
		return "$title\n$message".trimIndent()
	}
	
	val title: String
		get() = (when(unwrapped) {
			is SocketTimeoutException -> i18n(Res.string.timed_out)
			is SSLHandshakeException -> i18n(Res.string.failed_handshake)
			is SocketException -> unwrapped.message
			is UnknownHostException -> unwrapped.message
			is BotSecurityBypassException -> i18n(Res.string.failed_to_bypass, unwrapped.blocker)
			is ExtensionInstallException -> i18n(Res.string.extension_installed_failed)
			is ExtensionLoadException -> i18n(Res.string.extension_load_failed)
			is CancellationException -> "Operation cancelled"
			is UnsupportedOperationException -> "Unsupported action"
			is NotImplementedError -> "Unsupported action"
			
			is HttpException -> when(unwrapped.code) {
				400, 422 -> i18n(Res.string.bad_request)
				403 -> i18n(Res.string.access_denied)
				404 -> i18n(Res.string.nothing_found)
				429 -> i18n(Res.string.too_much_requests)
				500, 503 -> i18n(Res.string.server_down)
				504 -> i18n(Res.string.timed_out)
				else -> i18n(Res.string.unknown_net_error)
			}
			
			else -> null
		}) ?: run {
			if(unwrapped.message?.contains(ROOM_EXCEPTION) == true) {
				return i18n(Res.string.database_corrupted)
			}
			
			return@run i18n(Res.string.something_went_wrong)
		}
	
	val message: String
		get() = when(unwrapped) {
			is LocaleAware -> unwrapped.localizedMessage
			is SocketTimeoutException -> i18n(Res.string.connection_timeout)
			is BotSecurityBypassException -> i18n(Res.string.failed_bypass_detailed, unwrapped.blocker)
			is SocketException -> i18n(Res.string.failed_to_connect_to_server)
			is SSLHandshakeException -> i18n(Res.string.failed_to_connect_to_server)
			is NotImplementedError -> unwrapped.message
			
			is HttpException -> "(${unwrapped.code}) " + when(unwrapped.code) {
				400, 422 -> i18n(Res.string.request_invalid_detailed)
				401 -> i18n(Res.string.not_logged_detailed)
				403 -> i18n(Res.string.no_access_detailed)
				404 -> i18n(Res.string.not_found_detailed)
				429 -> i18n(Res.string.rate_limited_detailed)
				500 -> i18n(Res.string.server_internal_error)
				503 -> i18n(Res.string.server_unavailable)
				504 -> i18n(Res.string.connection_timeout)
				else -> i18n(Res.string.unknown_error)
			}
			
			else -> null
		} ?: run {
			if(unwrapped.message?.contains(ROOM_EXCEPTION) == true) {
				return@run """
		Yeah, you've hear right. The database has been corrupted!
		How can you fix it? Clear app data.
		
		Please, do not use alpha versions to keep your library. Use them only to test new things.
		
		${unwrapped.stackTraceToString()}
		""".trimIndent()
			}
			
			return@run unwrapped.stackTraceToString()
		}
	
	val category: Category
		get() = when(unwrapped) {
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
			|| this is NotImplementedError
			|| this is SocketTimeoutException
			|| this is BotSecurityBypassException
			|| this is SSLHandshakeException
			|| this is SocketException
			|| this is UnknownHostException
			|| this is LocaleAware

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

fun Throwable.explain() = OkiThrowableMessage(this)