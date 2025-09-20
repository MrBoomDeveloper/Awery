package com.mrboomdev.awery.ui.utils

import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.extension.loaders.ExtensionInstallException
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.ui.utils.ExceptionClassifier.Type
import eu.kanade.tachiyomi.network.HttpException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.serialization.*
import java.io.*
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.*
import java.security.cert.*
import javax.net.ssl.SSLHandshakeException

class ExceptionClassifier internal constructor(
    val exception: Throwable,
    val type: Type
) {
    val title get() = when(type) {
        Type.ACCESS_DENIED -> "Access denied"
        Type.NOT_IMPLEMENTED -> "Not implemented"
        Type.TIMEOUT -> "Response timeout"
        Type.SERVER_FAIL -> "Server error"
        Type.SERVER_DOWN -> "Server is down"
        Type.NO_NETWORK -> "No internet"
        Type.CONNECTION_FAILED -> "Failed to connect"
        Type.PARSING_FAILED -> "Incompatible data"
        Type.CAPTCHA -> "Failed to bypass captcha"
        Type.NOTHING_FOUND -> "Nothing found"
        Type.TOO_MANY_REQUESTS -> "Too many requests"
        
        Type.UNKNOWN -> when(exception) {
            is ExtensionLoadException -> "Failed to load an extension"
            is ExtensionInstallException -> "Failed to install an extension"
            else -> "Unknown error has occurred"
        }
    }

    val message get() = when(type) {
        Type.NOT_IMPLEMENTED -> exception.message
            ?: "This functionality isn't done yet! Try updating app."

        Type.TIMEOUT -> "The response timeout expired. " +
                "The server may be under load right now. Try again later."

        Type.SERVER_FAIL -> "So it looks like the server is unable to response " +
                "at the current time. Retry after several minutes."

        Type.CONNECTION_FAILED -> "We are unable to connect to the server. " +
                "You may need to enable VPN if your IPS is blocking the service."

        Type.PARSING_FAILED -> "We cannot resolve data returned from the server. " +
                "Maybe your app is outdated, try updating it.\n${exception.stackTraceToString()}"

        Type.NO_NETWORK -> "It looks that you are not connected to the internet. " +
                "Try switching WI-FI and mobile network."

        Type.SERVER_DOWN -> "Damn. The server is feeling pretty bad right now... " +
                "Your can try again a little bit later."

        Type.CAPTCHA -> "Oh no... Access has been denied by an anti-bot system! " +
                "Try turning on and off your VPN."

        Type.NOTHING_FOUND -> "No results were found. Try changing your query."

        Type.TOO_MANY_REQUESTS -> "Too much requests in a short time! Come back later."
        
        Type.ACCESS_DENIED -> "You don't have access to this resource. Try signing in into your account."

        Type.UNKNOWN -> when(exception) {
            is ExtensionLoadException -> exception.message
            is ExtensionInstallException -> exception.message
            else -> null
        }
    } ?: exception.stackTraceToString()

    enum class Type {
        NOT_IMPLEMENTED,
        TIMEOUT,
        SERVER_FAIL,
        SERVER_DOWN,
        TOO_MANY_REQUESTS,
        NOTHING_FOUND,
        ACCESS_DENIED,
        NO_NETWORK,
        PARSING_FAILED,
        CONNECTION_FAILED,
        CAPTCHA,
        UNKNOWN
    }
}

private fun typeFromHttpCode(code: Int): Type? {
    return when(code) {
        401, 403, 511 -> Type.ACCESS_DENIED
        404, 410, 451 -> Type.NOTHING_FOUND
        408, 504 -> Type.TIMEOUT
        429 -> Type.TOO_MANY_REQUESTS
        422, 500, 502 -> Type.SERVER_FAIL
        503, 521 -> Type.SERVER_DOWN
        else -> null
    }
}

private fun Throwable.getType(): Type {
    if(this is ResponseException) {
        typeFromHttpCode(response.status.value)?.also { return it }
    }

    if(this is HttpException) {
        typeFromHttpCode(code)?.also { return it }
    }

    if(this is NothingFoundException) {
        return Type.NOTHING_FOUND
    }

    if(this is CertPathValidatorException || this is SocketException || this is SSLHandshakeException) {
        return Type.CONNECTION_FAILED
    }

    if(this is HttpRequestTimeoutException || this is SocketTimeoutException) {
        return Type.TIMEOUT
    }

    if(this is UnresolvedAddressException || this is UnknownHostException) {
        return Type.NO_NETWORK
    }

    if(this is SerializationException) {
        return Type.PARSING_FAILED
    }

    if(this is IOException) {
        when(message) {
            "Connection reset by peer" -> Type.CONNECTION_FAILED
            "Failed to bypass Cloudflare!" -> Type.CAPTCHA
            "timeout" -> Type.TIMEOUT
            else -> null
        }?.also { return it }
    }

    if(this is NotImplementedError || this is UnsupportedOperationException) {
        return Type.NOT_IMPLEMENTED
    }

    return Type.UNKNOWN
}

fun Throwable.classify(): ExceptionClassifier {
    var current = this
    var type: Type

    while(true) {
        type = current.getType()

        if(type != Type.UNKNOWN) {
            break
        }

        val cause = current.cause

        if(cause == null) {
            current = this
            type = Type.UNKNOWN
            break
        } else {
            current = cause
        }
    }

    return ExceptionClassifier(current, type)
}