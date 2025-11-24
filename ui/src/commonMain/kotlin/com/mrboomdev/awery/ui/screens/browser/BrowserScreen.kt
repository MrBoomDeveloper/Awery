package com.mrboomdev.awery.ui.screens.browser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_close
import com.mrboomdev.awery.resources.ic_refresh
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.WebBrowser
import com.mrboomdev.awery.ui.components.rememberWebBrowserState
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.navigation.RouteInfoEffect
import com.mrboomdev.awery.ui.utils.formatAsUrl
import com.mrboomdev.navigation.core.safePop
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(url: String) {
    val state = rememberWebBrowserState(url)
    val navigation = Navigation.current()

    RouteInfoEffect(
        displayHeader = false
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        
        topBar = {
            TopAppBar(
                title = { Text(state.url.formatAsUrl()) },
                
                navigationIcon = {
                    IconButton(
                        padding = 9.dp,
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = null,
                        onClick = { navigation.safePop() }
                    )
                },
                
                actions = {
                    IconButton(
                        padding = 10.dp,
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = null,
                        onClick = { state.reload() }
                    )
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            WebBrowser(
                modifier = Modifier.fillMaxSize(),
                state = state
            )

            if(state.progress < 1f) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { state.progress }
                )
            }
        }
    }
}