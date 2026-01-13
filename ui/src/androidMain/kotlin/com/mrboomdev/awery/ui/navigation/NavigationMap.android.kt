package com.mrboomdev.awery.ui.navigation

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.mrboomdev.navigation.core.InternalNavigationApi
import com.mrboomdev.navigation.core.currentNavigationOrNull
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import com.mrboomdev.navigation.jetpack.Saver

@OptIn(InternalNavigationApi::class)
@Composable
internal actual fun rememberNavigationMap(): NavigationMap {
    val parent = currentNavigationOrNull()
    val context = LocalActivity.current ?: LocalContext.current

    val navigationSaver = remember(parent) {
        // This shit totally depends on a library saved state internal structure. 
        // If something changes in the update, we'll have to remake a lot of stuff here.
        // We have to do this because of stupid Android serialization mechanisms.
        @Suppress("UNCHECKED_CAST")
        JetpackNavigation.Saver<Routes>(
            context, 
            parent
        ) as Saver<JetpackNavigation<Routes>, Pair<String, Bundle?>>
    }

    val navigators = rememberSaveable(
        inputs = arrayOf(parent, context),
        saver = Saver<MutableMap<Int, JetpackNavigation<Routes>>, Bundle>(
            save = { mutableMap ->
                mutableMap.mapValues { (_, navigation) ->
                    with(navigationSaver) {
                        save(navigation)
                    }!!
                }.let {
                    Bundle().apply {
                        for((key, value) in it) {
                            val (encodedInitialRoute, navControllerState) = value
                            putString("${key}_route", encodedInitialRoute)
                            putBundle("${key}_state", navControllerState)
                        }
                    }
                }
            },

            restore = { savedBundle ->
                val routes = mutableMapOf<Int, String>()
                val states = mutableMapOf<Int, Bundle?>()

                for(key in savedBundle.keySet()) {
                    val index = key.substringBefore("_").toInt()

                    when(key.substringAfter("_")) {
                        "route" -> routes[index] = savedBundle.getString(key)!!
                        "state" -> states[index] = savedBundle.getBundle(key)
                        else -> throw IllegalStateException("The fuck is this shit!? $key")
                    }
                }

                routes.keys.associateWith { index ->
                    navigationSaver.restore(routes[index]!! to states[index])!!
                }.toMutableMap()
            }
        )
    ) {
        mutableMapOf()
    }

    return remember(navigators) {
        object : NavigationMap {
            override fun get(index: Int): JetpackNavigation<Routes> {
                return navigators.getOrPut(index) {
                    JetpackNavigation(
                        context = context,
                        initialRoute = MainRoutes.entries[index].route,
                        parent = parent
                    )
                }
            }
        }
    }
}