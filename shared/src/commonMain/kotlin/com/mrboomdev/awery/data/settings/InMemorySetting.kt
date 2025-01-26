package com.mrboomdev.awery.data.settings

import com.mrboomdev.awery.ext.data.Setting
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KClass

@Serializable
open class InMemorySetting(
	/**
	 * Tells the factory how it should be loaded.
	 */
	val isSuspend: Boolean,
	/**
	 * Factory may use these params to guess how to restore an setting.
	 */
	val params: Map<String, String>,
	/**
	 * Factories should set this value to false so that it wouldn't stuck in a loop trying to restore.
	 */
	@Transient
	val wasDeserialized: Boolean = true,
	/**
	 * A class that is responsible for loading this in-memory setting.
	 */
	val factoryClass: InMemorySettingsFactoryClass
): Setting()

@JvmInline
@Serializable
value class InMemorySettingsFactoryClass(val className: String) {
	constructor(clazz: KClass<out InMemorySettingsFactory>): this(clazz.qualifiedName!!)
}

/**
 * An [InMemorySettingsFactory] should to have an zero argument constructor!
 */
interface InMemorySettingsFactory {
	fun create(setting: InMemorySetting): InMemorySetting {
		throw UnsupportedOperationException("Sync setting creation isn't implemented!")
	}
	
	suspend fun createSuspend(setting: InMemorySetting): InMemorySetting {
		throw UnsupportedOperationException("Suspend setting creation isn't implemented!")
	}
	
	companion object {
		/**
		 * We don't want to create factories each time we need to restore an setting, so we do cache them.
		 */
		private val cachedFactories = mutableMapOf<InMemorySettingsFactoryClass, InMemorySettingsFactory>()
		
		operator fun get(clazz: InMemorySettingsFactoryClass): InMemorySettingsFactory {
			cachedFactories[clazz]?.let { return it }
			
			val instance = Class.forName(clazz.className).getConstructor().apply { 
				isAccessible = true
			}.newInstance() as InMemorySettingsFactory
			
			cachedFactories[clazz] = instance
			return instance
		}
	}
}
