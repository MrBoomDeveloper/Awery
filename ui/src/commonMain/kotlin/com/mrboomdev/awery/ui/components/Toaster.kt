package com.mrboomdev.awery.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
                    state = rememberSwipeToDismissBoxState(confirmValueChange = {
                        if(it != SwipeToDismissBoxValue.Settled) {
                            state.toasts -= item
                            true
                        } else false
                    }),
                    backgroundContent = {},
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

                        Spacer(Modifier.weight(1f))

                        toast.actionText?.also { action ->
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = action
                            )
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
}

data class Toast(
    val title: String,
    val message: String?,
    val actionText: String?,
    val onClick: (() -> Unit)?,
    val duration: Long
)

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