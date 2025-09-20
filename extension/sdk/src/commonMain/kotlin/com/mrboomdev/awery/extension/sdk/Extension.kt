package com.mrboomdev.awery.extension.sdk

import com.mrboomdev.awery.extension.sdk.modules.Module

/**
 * Implementation class has to have either a zero-arg constructor or 
 * the one with [Extension] as the single input value, 
 * which would be resolved by using an extension manifest.
 * 
 * Example:
 * ```kotlin
 * class SampleExtension(
 *     private val resolvedManifest: Extension
 * ): Extension by resolvedManifest {
 *     ...
 * }
 * ```
 */
interface Extension {
    val name: String
    val id: String
    val version: String
    val isNsfw: Boolean

    val lang: String?
        get() = null

    val webpage: String?
        get() = null

    val icon: Image?
        get() = null
    
    val loadException: ExtensionLoadException?
        get() = null
    
    fun createModules(): Collection<Module>
}