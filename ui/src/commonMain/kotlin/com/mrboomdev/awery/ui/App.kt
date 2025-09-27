package com.mrboomdev.awery.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.ExtensionInstaller
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.Toaster
import com.mrboomdev.awery.ui.components.ToasterContainer
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

interface App {
    fun reloadWallpaper()
}

val LocalApp = staticCompositionLocalOf<App> { 
    throw IllegalStateException("LocalApp isn't initialized!")
}

@Composable
fun App(
    navigation: @Composable () -> JetpackNavigation<Routes> = {
        val initialRoute = remember { getInitialRoute() }
        rememberJetpackNavigation(initialRoute)
    }
) {
    val windowSize = currentWindowSize()
    val navigation = navigation()
    val backStack by navigation.currentBackStack.collectAsState(null)
    
    AweryTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val toaster = remember { Toaster(maxItems = 3) }

            val context = LocalPlatformContext.current
            val backgroundColor = MaterialTheme.colorScheme.background
            val wallpaperOpacity = AwerySettings.wallpaperOpacity.state.value / 100f
            
            val wallpaperPainter = rememberAsyncImagePainter(
                filterQuality = FilterQuality.High,
                model = remember {
                    ImageRequest.Builder(context)
                        .addLastModifiedToFileCacheKey(true)
                        .data(FileKit.filesDir / "wallpaper.png")
                        .build()
                },
                
                contentScale = ContentScale.Crop
            )

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val gradient = Brush.verticalGradient(listOf(
                            Color.Transparent,
                            backgroundColor.copy(alpha = (1f - wallpaperOpacity) * 2f)
                        ))

                        onDrawWithContent {
                            drawContent()
                            drawRect(gradient)
                        }
                    },

                painter = wallpaperPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = wallpaperOpacity
            )

            CompositionLocalProvider(
                LocalToaster provides toaster,
                LocalApp provides remember { 
                    object : App {
                        override fun reloadWallpaper() {
                            wallpaperPainter.restart()
                        }
                    }
                }
            ) {
                Row {
                    val useRail = windowSize.width >= WindowSizeType.Large && false
                    
                    if(useRail) {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface.let {
                                    if(isAmoledTheme()) it.copy(alpha = .75f) else it
                                }).fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Start + WindowInsetsSides.Vertical))
                                .padding(horizontal = 8.dp)
                                .verticalScroll(rememberScrollState()),
                            
                            verticalArrangement = Arrangement.spacedBy(
                                if(AwerySettings.showNavigationLabels.state.value 
                                    == AwerySettings.NavigationLabels.SHOW) 16.dp else 0.dp
                            )
                        ) {
                            val currentTab = backStack?.lastOrNull { currentTab ->
                                MainRoutes.entries.any { tab ->
                                    tab.route == currentTab
                                } || currentTab is Routes.Settings
                            }
                            
                            @Composable
                            fun Tab(tab: MainRoutes) {
                                WideNavigationRailItem(
                                    railExpanded = false,
                                    selected = tab.route == currentTab || 
                                            (tab == MainRoutes.SETTINGS && currentTab is Routes.Settings),

                                    icon = {
                                        if(tab == MainRoutes.PROFILE) {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .size(24.dp),

                                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                                    .addLastModifiedToFileCacheKey(true)
                                                    .data(FileKit.filesDir / "avatar.png")
                                                    .build(),

                                                contentDescription = null,
                                                contentScale = ContentScale.Crop
                                            )

                                            return@WideNavigationRailItem
                                        }

                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            imageVector = vectorResource(tab.getIcon(tab.route == currentTab)),
                                            contentDescription = null
                                        )
                                    },

                                    label = if(AwerySettings.showNavigationLabels.state.value 
                                        != AwerySettings.NavigationLabels.HIDE
                                    ) {label@{
                                        if(tab == MainRoutes.PROFILE) {
                                            Text(
                                                text = AwerySettings.username.state.value,
                                                fontSize = 11.sp
                                            )

                                            return@label
                                        }

                                        Text(
                                            text = stringResource(tab.title),
                                            fontSize = 11.sp
                                        )
                                    }} else null,

                                    onClick = {
                                        navigation.clear()
                                        navigation.push(tab.route)
                                    }
                                )
                            }
                            
                            for(tab in MainRoutes.entries.filter { !it.desktopOnly }) {
                                Tab(tab)
                            }

                            Spacer(Modifier.weight(1f))

                            for(tab in MainRoutes.entries.filter { it.desktopOnly }) {
                                Tab(tab)
                            }
                        }
                    }
                    
                    Column {
                        val installing by ExtensionInstaller.observeInstalling().collectAsState()

                        AnimatedVisibility(installing.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .windowInsetsPadding(WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Top + if(useRail) {
                                            WindowInsetsSides.Right
                                        } else WindowInsetsSides.Horizontal
                                    ))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Text(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    text = "Installing ${installing.size} extensions"
                                )
                            }
                        }

                        JetpackNavigationHost(
                            modifier = Modifier.fillMaxSize(),
                            navigation = navigation,

                            enterTransition = {
                                fadeIn(tween(500)) +
                                        slideInHorizontally(tween(350)) { it / 2 } +
                                        scaleIn(tween(250), initialScale = .95f)
                            },

                            exitTransition = {
                                fadeOut(tween(500)) +
                                        slideOutHorizontally(tween(350)) +
                                        scaleOut(tween(250), targetScale = .95f)
                            },

                            graph = remember {
                                sealedNavigationGraph {
                                    it.Content(PaddingValues.Zero) 
                                } 
                            }
                        )
                    }
                }
            }

            ToasterContainer(
                state = toaster,
                contentAlignment = if(windowSize.width >= WindowSizeType.Large) {
                    Alignment.BottomStart
                } else Alignment.BottomCenter
            )
        }
    }
}