package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.extension.sdk.Image
import com.mrboomdev.awery.extension.sdk.modules.Module

class FailedExtension(
    val parentExtension: Extension? = null,
    override val id: String,
    override val name: String = id,
    override val loadException: ExtensionLoadException
): Extension {
    override val version: String
        get() = parentExtension?.version ?: "Unknown"

    override val lang: String?
        get() = parentExtension?.lang

    override val icon: Image?
        get() = parentExtension?.icon

    override val isNsfw: Boolean
        get() = parentExtension?.isNsfw == true

    override val webpage: String?
        get() = parentExtension?.webpage

    override fun createModules() = emptyList<Module>()
}