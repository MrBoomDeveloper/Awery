package com.mrboomdev.awery.extension.sdk.modules

import com.mrboomdev.awery.extension.sdk.Extension
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface ManagerModule: Module {
    suspend fun get(id: String) = getAll().firstOrNull { it.id == id }
    fun getAll(): Flow<Extension>
}