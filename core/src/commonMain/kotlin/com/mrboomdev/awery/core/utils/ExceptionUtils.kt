package com.mrboomdev.awery.core.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes

/**
 * @throws NotImplementedError
 */
fun notImplemented(clazz: KClass<*>): NotImplementedError {
	return NotImplementedError(buildString { 
		append("(")
		append(clazz.qualifiedName)
		append(" : ")
		
		clazz.allSupertypes.iterator().also { iterator -> 
			while(iterator.hasNext()) {
				append(iterator.next().toString())
				
				if(iterator.hasNext()) {
					append(" , ")
				}
			}
		}
		
		append(") isn't implemented yet!")
	})
}