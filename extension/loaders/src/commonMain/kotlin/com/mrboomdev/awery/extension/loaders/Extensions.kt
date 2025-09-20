package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.core.utils.await
import com.mrboomdev.awery.core.utils.logger
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.bundled.BundledExtensions
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.modules.ManagerModule
import com.mrboomdev.awery.extension.sdk.modules.Module
import dev.mihon.injekt.patchInjekt
import eu.kanade.tachiyomi.network.JavaScriptEngine
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.addSingletonFactory
import kotlin.coroutines.CoroutineContext
import kotlin.time.measureTime
import kotlin.time.DurationUnit

private val logger by logger()

/**
 * Manages the loading and retrieval of extensions.
 *
 * This object is responsible for loading all available extensions and providing access to them.
 * It ensures that extensions are loaded only once and provides a flow for observing them.
 */
object Extensions {
    private val extensions = mutableMapOf<String, Extension>()
    private val observableExtensions = MutableStateFlow<Collection<Extension>>(emptyList())
    private val observableIsLoading = MutableStateFlow(false)
    private var pendingGetters = mutableListOf<Pair<CoroutineContext, ProducerScope<Extension>>>()
    private val cachedModules = mutableMapOf<Extension, Collection<Module>>()
    private val mutex = Mutex()
    private var didLoad = false
    
    fun observeIsLoading() = observableIsLoading.asStateFlow()

    val Extension.cachedModules
        get() = Extensions.cachedModules.getOrPut(this) { createModules() }

    inline fun <reified T: Module> Extension.get() = 
        cachedModules.firstOrNull { it is T } as T?

    inline fun <reified T: Module> Extension.has() = 
        cachedModules.any { it is T }

    suspend operator fun get(id: String): Extension? {
        extensions[id]?.also { return it }
        return getAll().firstOrNull { it.id == id }
    }
    
    suspend fun add(extension: Extension) {
        mutex.withLock {
            extensions[extension.id] = extension
            observableExtensions.emit(extensions.values.toList())
        }
    }
    
    suspend fun remove(extension: Extension) {
        mutex.withLock {
            extensions -= extension.id
            observableExtensions.emit(extensions.values.toList())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun observeAll(enabled: Boolean? = null) = observableExtensions
        .mapLatest { extensions ->
            extensions.filter { extension ->
                if(enabled == null) true
                else enabled == AwerySettings.isExtensionEnabled(extension.id)
            }.map { extension ->
                flow {
                    emit(extension)
                    emitAll(extension.getAllRecursively(enabled))
                }
            }.merge().toList()
        }

    @JvmName("getAllWithModuleT")
    inline fun <reified T: Module> getAll(enabled: Boolean? = null) =
        getAll(enabled).filter { extension ->
            extension.cachedModules.any { it is T }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAll(
        enabled: Boolean? = null
    ): Flow<Extension> = getAllImpl().filter { extension ->
        if(enabled == null) true
        else enabled == AwerySettings.isExtensionEnabled(extension.id)
    }.map { extension ->
        flow {
            emit(extension)
            emitAll(extension.getAllRecursively(enabled))
        }
    }.flattenConcat()

    private fun Extension.getAllRecursively(
        enabled: Boolean?
    ): Flow<Extension> = flow {
        cachedModules.filterIsInstance<ManagerModule>().forEach { module ->
            module.getAll().filter { extension ->
                if(enabled == null) true
                else enabled == AwerySettings.isExtensionEnabled(extension.id)
            }.collect { child ->
                emit(child)
                emitAll(child.getAllRecursively(enabled))
            }
        }
    }

    /**
     * Simply collects all extensions from the top-level loader.
     * This method does not use recursion so child
     * extensions should be collected from [getAll].
     */
    private fun getAllImpl(): Flow<Extension> {
        if(didLoad) {
            return flow {
                mutex.withLock {
                    extensions.values.forEach { emit(it) }
                }
            }
        }

        return channelFlow {
            val key = coroutineContext to this
            
            mutex.withLock {
                pendingGetters += key
                extensions.values.forEach { send(it) }
            }
            
            await { didLoad }
            mutex.withLock { pendingGetters -= key }
        }
    }

    /**
     * Loads all available extensions.
     *
     * This function should typically be called once at the application startup or when a full
     * reload of extensions is required.
     */
    suspend fun loadAll() {
        didLoad = false
        observableIsLoading.emit(true)
        logger.i("Started loading all extensions")

        val loadDuration = measureTime {
            injectFuckingShit()

            channelFlow {
                launch {
                    BundledExtensions.getAll { extensionId ->
                        ContextImpl(extensionId)
                    }.forEach { send(it) }
                }
                
                loadAllImpl()
            }.collect { extension ->
                add(extension)

                pendingGetters.forEach { key ->
                    if(!key.first.isActive) {
                        // Coroutine is no longer alive, so we have to remove it from the list by our own hands
                        pendingGetters -= key
                        return@forEach
                    }

                    key.second.send(extension)
                }
            }
        }

        if(pendingGetters.isNotEmpty()) {
            logger.w("There are still some pending getters! " +
                    "They may have received all the extensions due to thread racing.")

            pendingGetters.forEach { key ->
                key.second.close()
            }

            pendingGetters.clear()
        }

        didLoad = true
        observableIsLoading.emit(false)
        observableExtensions.emit(extensions.values.toList())
        logger.i("Loaded all extensions in ${loadDuration.toString(DurationUnit.SECONDS, 3)}!")
    }
}

/**
 * Initializes and configures Injekt for fucking Aniyomi/Tachiyomi extensions.
 */
private fun injectFuckingShit() {
    patchInjekt()

    Injekt.addSingleton(NetworkHelper)
    Injekt.addSingleton(JavaScriptEngine)

    Injekt.addSingletonFactory {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}

internal expect suspend fun ProducerScope<Extension>.loadAllImpl()