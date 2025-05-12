package com.mrboomdev.awery.core

object Awery {
    /**
     * Checks whatever device is an TV or not.
     */
    val Awery.TV: Boolean
        get() = isTv()
}

internal expect fun isTv(): Boolean