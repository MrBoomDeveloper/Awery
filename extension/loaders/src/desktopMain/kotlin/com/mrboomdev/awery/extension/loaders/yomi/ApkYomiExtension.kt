package com.mrboomdev.awery.extension.loaders.yomi

import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.extension.sdk.Image
import com.mrboomdev.awery.extension.sdk.modules.Module
import java.io.File

class ApkYomiExtension(
    file: File
): Extension {
    override val name: String
        get() = TODO("Not yet implemented")

    override val id: String
        get() = TODO("Not yet implemented")

    override val version: String
        get() = TODO("Not yet implemented")

    override val lang: String?
        get() = TODO("Not yet implemented")

    override val webpage: String?
        get() = TODO("Not yet implemented")
    
    override val isNsfw: Boolean
        get() = TODO("Not yet implemented")
    
    override val icon: Image?
        get() = TODO("Not yet implemented")

    override val loadException: ExtensionLoadException?
        get() = null

    override fun createModules(): Collection<Module> {
        TODO("Not yet implemented")
    }
}