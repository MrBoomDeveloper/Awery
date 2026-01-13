package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateHandle.Companion.validateValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> rememberFlowOfState(value: T): StateFlow<T> {
    val flow = remember { MutableStateFlow(value) }

    LaunchedEffect(value) {
        flow.emit(value)
    }
    
    return flow
}

inline fun <reified T> stateListSaver() = listSaver<SnapshotStateList<T>, T>(
    save = { it },
    restore = { mutableStateListOf(*it.toTypedArray()) }
)

inline fun <reified T: Enum<T>> enumStateSaver() = Saver<MutableState<T>, String>(
    save = { it.value.name },
    restore = { mutableStateOf(enumValueOf<T>(it)) }
)

inline fun <reified T: Enum<T>> enumSaver() = Saver<T, String>(
    save = { it.name },
    restore = { enumValueOf<T>(it) }
)

expect fun <T : Any> SavedStateHandle.saveable(
    key: String,
    saver: Saver<T, out Any> = autoSaver(),
    init: () -> T,
): T