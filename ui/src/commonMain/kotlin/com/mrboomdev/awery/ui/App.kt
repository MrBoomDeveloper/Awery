package com.mrboomdev.awery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.mrboomdev.awery.core.utils.Log.e
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.ExtensionInstaller
import com.mrboomdev.awery.extension.sdk.Video
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.Toaster
import com.mrboomdev.awery.ui.components.ToasterContainer
import com.mrboomdev.awery.ui.screens.browser.BrowserScreen
import com.mrboomdev.awery.ui.screens.extension.ExtensionFeedScreen
import com.mrboomdev.awery.ui.screens.extension.ExtensionScreen
import com.mrboomdev.awery.ui.screens.extension.ExtensionSearchScreen
import com.mrboomdev.awery.ui.screens.intro.IntroScreen
import com.mrboomdev.awery.ui.screens.intro.IntroStep
import com.mrboomdev.awery.ui.screens.main.MainScreen
import com.mrboomdev.awery.ui.screens.media.MediaScreen
import com.mrboomdev.awery.ui.screens.player.PlayerScreen
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.navigation.core.Navigation
import com.mrboomdev.navigation.core.TypeSafeNavigation
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.serialization.Serializable

val Navigation = TypeSafeNavigation<Routes>()

sealed interface Routes {
    @Serializable
    data object Main: Routes {
        @Composable
        override fun Content() = MainScreen()
    }

    @Serializable
    data class Settings(
        val initialPage: SettingsPages = SettingsPages.Main()
    ): Routes {
        @Composable
        override fun Content() = SettingsScreen(initialPage)
    }

    @Serializable
    data class Media(
        val extensionId: String,
        val extensionName: String?,
        val media: com.mrboomdev.awery.extension.sdk.Media
    ): Routes {
        @Composable
        override fun Content() = MediaScreen(this)
    }

    @Serializable
    data class Player(
        val video: Video,
        val title: String = video.title ?: video.url
    ): Routes {
        @Composable
        override fun Content() = PlayerScreen(this)
    }

    @Serializable
    data class Extension(
        val extensionId: String,
        val extensionName: String
    ): Routes {
        @Composable
        override fun Content() = ExtensionScreen(this)
    }

    @Serializable
    data class ExtensionFeed(
        val extensionId: String,
        val extensionName: String,
        val feedId: String,
        val feedName: String
    ): Routes {
        @Composable
        override fun Content() = ExtensionFeedScreen(this)
    }

    @Serializable
    data class ExtensionSearch(
        val extensionId: String,
        val extensionName: String
    ): Routes {
        @Composable
        override fun Content() = ExtensionSearchScreen(this)
    }

    @Serializable
    data class Intro(
		val step: IntroStep,
		val singleStep: Boolean
    ): Routes {
        @Composable
        override fun Content() = IntroScreen(this)
    }

    @Serializable
    data class Browser(
        val url: String
    ): Routes {
        @Composable
        override fun Content() = BrowserScreen(url)
    }

    @Composable
    fun Content()
}

private fun getInitialRoute(): Routes {
    if(!AwerySettings.introDidWelcome.value) {
        return Routes.Intro(IntroStep.Welcome, singleStep = false)
    }
    
    if(!AwerySettings.introDidTheme.value) {
        return Routes.Intro(IntroStep.Theme, singleStep = false) 
    }

    if(AwerySettings.username.value.isBlank()) {
        return Routes.Intro(IntroStep.UserCreation, singleStep = false)
    }
    
    return Routes.Main
}

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
    AweryTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val windowSize = currentWindowSize()
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
                Column {
                    val installing by ExtensionInstaller.observeInstalling().collectAsState()

                    AnimatedVisibility(installing.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                text = "Installing ${installing.size} extensions"
                            )

                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    JetpackNavigationHost(
                        modifier = Modifier.fillMaxSize(),
                        navigation = navigation(),
                        graph = remember { sealedNavigationGraph { it.Content() } },

                        enterTransition = {
                            fadeIn(tween(500)) +
                                    slideInHorizontally(tween(350)) { it / 2 } +
                                    scaleIn(tween(250), initialScale = .95f)
                        },

                        exitTransition = {
                            fadeOut(tween(500)) +
                                    slideOutHorizontally(tween(350)) +
                                    scaleOut(tween(250), targetScale = .95f)
                        }
                    )
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