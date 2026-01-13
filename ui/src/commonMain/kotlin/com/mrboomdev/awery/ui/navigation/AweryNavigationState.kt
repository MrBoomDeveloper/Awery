package com.mrboomdev.awery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

@Composable
fun AweryNavigationState.observeCurrentNavigation() = currentNavigationFlow.collectAsState(
    navigationMap[currentTabIndex.value]
)

@Composable
fun AweryNavigationState.observeCanPop() = canPopFlow.collectAsState(
    navigationMap[currentTabIndex.value].currentBackStack.size > 1,
)

class AweryNavigationState internal constructor(
    internal val navigationMap: NavigationMap
) {
    @Suppress("PropertyName")
    internal val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex = _currentTabIndex.asStateFlow()
    
    val currentNavigationFlow = currentTabIndex.map { tabIndex ->
        navigationMap[tabIndex]
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val canPopFlow = currentNavigationFlow.flatMapLatest { navigation ->
        navigation.currentBackStackFlow
    }.map { backStack ->
        backStack.size > 1
    }
    
    fun getNavigation(index: Int) = navigationMap[index]
    
    fun pop() {
        navigationMap[currentTabIndex.value].safePop()
    }
    
    suspend fun selectTab(index: Int) {
        _currentTabIndex.emit(index)
    }
}

@Composable
fun rememberAweryNavigationState(): AweryNavigationState {
    val navigationMap = rememberNavigationMap()
    
    return rememberSaveable(
        navigationMap,
        saver = Saver(
            save = { state ->
                state.currentTabIndex.value
            },
            
            restore = { savedTabIndex ->
                AweryNavigationState(navigationMap).apply {
                    _currentTabIndex.value = savedTabIndex
                }
            }
        )
    ) {
        AweryNavigationState(navigationMap) 
    }
}