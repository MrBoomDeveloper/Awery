package com.mrboomdev.awery.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.thenIf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val DEFAULT_MAX_ITEMS = 3

val LocalToaster = staticCompositionLocalOf<Toaster> {
    throw NotImplementedError("LocalToaster was not declared!")
}

@Composable
fun ToasterContainer(
    maxItems: Int = DEFAULT_MAX_ITEMS,
    state: Toaster = remember(maxItems) { Toaster(maxItems) },
    contentAlignment: Alignment = Alignment.BottomCenter
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .animateContentSize()
                .align(contentAlignment)
                .widthIn(max = 400.dp)
                .fillMaxWidth(.9f),
            
            contentPadding = WindowInsets.safeContent
                .exclude(WindowInsets.statusBars)
                .asPaddingValues()
        ) {
            items(
                items = state.toasts,
                key = { it.second }
            ) { item ->
                val toast = item.first

                SwipeToDismissBox(
                    modifier = Modifier.animateItem(),
                    backgroundContent = {},
                    state = rememberSwipeToDismissBoxState(confirmValueChange = {
                        if(it != SwipeToDismissBoxValue.Settled) {
                            state.toasts -= item
                            true
                        } else false
                    })
                ) {
                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
                            .thenIf(toast.onClick != null) { clickable(onClick = toast.onClick!!) }
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                text = toast.title
                            )

                            toast.message?.also { message ->
                                Text(
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    text = message
                                )
                            }
                        }

                        toast.actionText?.also { action ->
                            TextButton(
                                onClick = { 
                                    toast.onClick!!()
                                    state.dismiss(toast)
                                }
                            ) {
                                Text(
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = action
                                )
                            }
                        }
                    }
                }
            }

            if(state.toasts.isNotEmpty()) {
                singleItem("empty") {
                    Spacer(
                        modifier = Modifier
                            .height(8.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

class Toaster(private val maxItems: Int = DEFAULT_MAX_ITEMS) {
    internal val toasts = mutableStateListOf<Pair<Toast, String>>()

    /**
     * Shows a toast message for a time specified in it and then automatically dismisses it.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun toast(toast: Toast) {
        if(toasts.size + 1 > maxItems) {
            toasts.removeFirstOrNull()
        }
        
        val pair = toast to Uuid.random().toString()
        toasts.add(pair)

        launchGlobal(Dispatchers.Default) {
            delay(toast.duration)

            launch(Dispatchers.Main) {
                toasts.remove(pair)
            }
        }
    }

    /**
     * Dismisses all messages fired with this [toast]
     */
    fun dismiss(toast: Toast) {
        toasts.removeAll { it.first == toast }
    }
}

data class Toast(
    val title: String,
    val message: String?,
    val actionText: String?,
    val onClick: (() -> Unit)?,
    val duration: Long
)

/**
 * Shows a toast message for a time specified in it and then automatically dismisses it.
 *
 * @param title The title of the toast message.
 * @param message The message of the toast message. If null, the toast won't show a message.
 * @param duration The duration of the toast message in milliseconds. Defaults to 5000.
 */
fun Toaster.toast(
    title: String,
    message: String? = null,
    duration: Long = 5000
) = toast(Toast(
    title = title,
    message = message,
    actionText = null,
    onClick = null,
    duration = duration
))

/**
 * Shows a toast message for a time specified in it and then automatically dismisses it.
 *
 * @param title The title of the toast message.
 * @param message The message of the toast message. If null, the toast won't show any message.
 * @param actionText The text of the action button of the toast message. If null, the toast won't show any action button.
 * @param onClick The action to be performed when the action button is clicked. If null, the toast won't show any action button.
 * @param duration The duration of the toast message in milliseconds. Defaults to 5000 (5 seconds).
 */
fun Toaster.toast(
    title: String,
    message: String? = null,
    actionText: String? = null,
    onClick: () -> Unit,
    duration: Long = 5000
) = toast(Toast(
    title = title,
    message = message,
    actionText = actionText,
    onClick = onClick,
    duration = duration
))