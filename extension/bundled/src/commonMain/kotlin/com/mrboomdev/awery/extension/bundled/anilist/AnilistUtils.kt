package com.mrboomdev.awery.extension.bundled.anilist

/**
 * @throws IllegalArgumentException If unable to serialize any value
 */
fun graphqlParams(
    vararg params: Pair<String, Any?>
) = params.toList()
    .filter { it.second != null }
    .joinToString(", ") { (key, value) -> 
        val formattedValue = when(value) {
            is Number, is Boolean -> value.toString()
            is Enum<*> -> value.name
            is String -> "\"$value\""
            else -> throw IllegalArgumentException("${value!!::class.qualifiedName}")
        }
        
        "$key: $formattedValue"
    }