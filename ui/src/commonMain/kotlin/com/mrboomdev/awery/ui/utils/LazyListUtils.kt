package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.retryUntilSuccess

inline fun <T> LazyListScope.safeItems(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit,
) {
    retryUntilSuccess(
        onFailure = {
            Log.d("LazyListUtils", "Failed to render LazyListScope.items, so rerender!")
        }
    ) {
        items(
            count = items.size,
            key = if(key != null) { index: Int -> key(items[index]) } else null,
            contentType = { index: Int -> contentType(items[index]) },
        ) {
            itemContent(items[it])
        }
    }
}

/**
 * Adds a single item to the [LazyListScope].
 *
 * This is a convenience function that simplifies adding a single item with a key.
 * The `key` is used for both the item's key and its content type.
 *
 * @param key A stable and unique key representing the item.
 * @param content The composable content of the item.
 */
fun LazyListScope.singleItem(
    key: String,
    content: @Composable LazyItemScope.() -> Unit = {}
) = item(key, key, content)

/**
 * Adds a single item to the [LazyGridScope].
 *
 * This is a convenience function that simplifies adding a single item with a specific key.
 *
 * @param key A stable and unique key representing the item.
 * @param span A lambda to define the span of the item. Defaults to occupying the maximum line span.
 * @param content The composable content of the item.
 */
fun LazyGridScope.singleItem(
    key: String,
    span: (LazyGridItemSpanScope.() -> GridItemSpan)? = { GridItemSpan(maxLineSpan) },
    content: @Composable LazyGridItemScope.() -> Unit = {}
) = item(key, span, key, content)