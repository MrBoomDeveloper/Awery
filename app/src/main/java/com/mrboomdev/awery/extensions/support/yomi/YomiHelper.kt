package com.mrboomdev.awery.extensions.support.yomi

import android.app.Application
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.addSingletonFactory

object YomiHelper {
    private var networkHelper: NetworkHelper? = null
    private var didInit = false

    fun networkHelper(): NetworkHelper = networkHelper ?: throw IllegalStateException("AniyomiHelper not initialized")

    @JvmStatic
    fun init(context: Application) {
        if(didInit) return

        val networkHelper = NetworkHelper(context)
        YomiHelper.networkHelper = networkHelper

        Injekt.addSingleton(context)
        Injekt.addSingleton(networkHelper)

        Injekt.addSingletonFactory {
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        }

        didInit = true
    }
}