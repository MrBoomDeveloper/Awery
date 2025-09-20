package com.mrboomdev.awery.core.utils

sealed interface LoadingStatus {
    data object NotInitialized: LoadingStatus
    data object Loading: LoadingStatus
    data object Loaded: LoadingStatus
    data class Failed(val throwable: Throwable): LoadingStatus
}

val LoadingStatus.mayStartLoading get() = when(this) {
    LoadingStatus.Loading, LoadingStatus.Loaded -> false
    LoadingStatus.NotInitialized, is LoadingStatus.Failed -> true
}