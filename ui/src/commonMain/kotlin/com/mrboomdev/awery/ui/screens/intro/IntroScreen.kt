package com.mrboomdev.awery.ui.screens.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.LocalApp
import com.mrboomdev.awery.ui.MainRoutes
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.FilePicker
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemSetting
import com.mrboomdev.awery.ui.theme.isDarkTheme
import com.mrboomdev.awery.ui.theme.isMaterialYouAvailable
import com.mrboomdev.awery.ui.theme.materialYouColorScheme
import com.mrboomdev.awery.ui.theme.seedColorScheme
import com.mrboomdev.awery.ui.utils.end
import com.mrboomdev.awery.ui.utils.start
import com.mrboomdev.awery.ui.utils.top
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation
import io.github.vinceglb.confettikit.compose.ConfettiKit
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.core.models.Shape
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Serializable
sealed interface IntroStep {
    @Serializable
    data object Welcome: IntroStep {
        @Composable
        override fun Content(
            singleStep: Boolean,
            contentPadding: PaddingValues
        ) = IntroDefaults.page(
            contentPadding = contentPadding,
            title = stringResource(Res.string.welcome_to_app),
            description = stringResource(Res.string.welcome_to_app_description),

            icon = {
                Image(
                    modifier = Modifier.size(IntroDefaults.iconSize),
                    painter = painterResource(Res.drawable.logo_awery),
                    contentDescription = null
                )
            },
            
            actions = {
                val navigation = IntroDefaults.navigation.current()
                
                Spacer(Modifier.weight(1f))
                
                Button(onClick = {
					runBlocking { AwerySettings.introDidWelcome.set(true) }
                    navigation.push(Theme)
                }) {
                    Text(stringResource(Res.string.lets_begin))
                }
            }
        )
    }
    
    @Serializable
    data object Theme: IntroStep {
        @Composable
        override fun Content(
            singleStep: Boolean,
            contentPadding: PaddingValues
        ) = IntroDefaults.page(
            contentPadding = contentPadding,
            title = stringResource(Res.string.color_palette),
            description = stringResource(Res.string.color_palette_description),

            icon = {
                Icon(
                    modifier = Modifier.size(IntroDefaults.iconSize),
                    painter = painterResource(Res.drawable.ic_palette_outlined),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            },
            
            nextStep = {
                runBlocking { AwerySettings.introDidTheme.set(true) }
                return@page UserCreation 
            }
        ) { contentPadding ->
            Box(Modifier.padding(contentPadding)) {
                ChildContent(PaddingValues.Zero)
            }
        }
        
        @Composable
        fun ChildContent(contentPadding: PaddingValues) {
            Column {
                FlowRow(
                    modifier = Modifier
                        .padding(
                            start = contentPadding.start,
                            top = contentPadding.top,
                            end = contentPadding.end,
                            bottom = 16.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val materialYou = -0x100000000

                    for(primaryColor in listOfNotNull(
                        // All stolen from Dartotsu.
                        // Copying every single one property would be pain in as to maintain,
                        // so instead we do just generate a whole palette based of a primary color.
                        materialYou.takeIf { isMaterialYouAvailable() },
                        0xFF00658E, // Blue
                        0xFF426916, // Green
                        0xFF6750A4, // Lavender
                        0xFF008F8C, // Ocean
                        0xFFFF9800, // Oriax
                        0xFFFF92E2, // Pink
                        0xFF7C4997, // Purple
                        0xFFC0000A, // Red
                        0xFFFF007F // Saikou
                    )) {
                        val scheme = if(primaryColor == materialYou) {
                            materialYouColorScheme()
                        } else seedColorScheme(Color(primaryColor))

                        Card(
                            modifier = Modifier.size(48.dp),

                            colors = CardDefaults.cardColors(
                                containerColor = scheme.primary,
                                contentColor = scheme.onPrimary
                            ),

                            onClick = {
								runBlocking {
									AwerySettings.primaryColor.set(primaryColor)
								}
                            }
                        ) {
                            if(AwerySettings.primaryColor.collectAsState().value.let {
                                it == primaryColor || (primaryColor == materialYou && it < 0L)
                            }) {
                                Icon(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    painter = painterResource(Res.drawable.ic_done),
                                    contentDescription = null
                                )
                            } else if(primaryColor == materialYou) {
                                Icon(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    painter = painterResource(Res.drawable.ic_awesome_filled),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                
                val buttonPadding = PaddingValues(
                    start = contentPadding.start,
                    top = 12.dp, 
                    end = contentPadding.end,
                    bottom = 12.dp
                )

                if(!Awery.isTv) {
                    SettingsDefaults.itemSetting(
                        setting = AwerySettings.darkTheme,
                        contentPadding = buttonPadding,
                        icon = painterResource(Res.drawable.ic_dark_mode_outlined),
                        title = stringResource(Res.string.dark_theme),
                        description = stringResource(Res.string.dark_theme_description),
                        enumValues = {
                            when(it) {
                                AwerySettings.DarkTheme.AUTO -> "Auto"
                                AwerySettings.DarkTheme.ON -> "On"
                                AwerySettings.DarkTheme.OFF -> "Off"
                            }
                        }
                    )
                }

                AnimatedVisibility(isDarkTheme() && !Awery.isTv) {
                    SettingsDefaults.itemSetting(
                        setting = AwerySettings.amoledTheme,
                        contentPadding = buttonPadding,
                        icon = painterResource(Res.drawable.ic_contrast),
                        title = stringResource(Res.string.amoled),
                        description = stringResource(Res.string.amoled_theme_description)
                    )
                }
            }
        }
    }
    
    @Serializable
    data object UserCreation: IntroStep {
        @Composable
        override fun Content(
            singleStep: Boolean,
            contentPadding: PaddingValues
        ) = IntroDefaults.page(
            contentPadding = contentPadding,
            title = "Customize experience",
            description = "Some inspirational text will appear here soon. if i'll won't forget about it.",
            canOpenNextStep = AwerySettings.username.collectAsState().value.isNotBlank(),
            nextStep = { End }.takeUnless { singleStep },

            icon = {
                Icon(
                    modifier = Modifier.size(IntroDefaults.iconSize),
                    painter = painterResource(Res.drawable.ic_mood_outlined),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier.padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = AwerySettings.username.collectAsState().value,
                    onValueChange = { runBlocking { AwerySettings.username.set(it) } },
                    label = { Text(stringResource(Res.string.username)) },
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val app = LocalApp.current
                    
                    @Composable
                    fun Item(fileName: String, title: String) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .addLastModifiedToFileCacheKey(true)
                                    .data(FileKit.filesDir / fileName)
                                    .build()
                            )
                            
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Normal,
                                text = title
                            )

                            FilePicker(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .fillMaxWidth()
                                    .aspectRatio(1f),

                                fileType = FileKitType.Image,

                                preview = {
                                    Image(
                                        modifier = Modifier.matchParentSize(),
                                        painter = painter,
                                        contentScale = ContentScale.Crop,
                                        contentDescription = null
                                    )
                                },

                                onPicked = { 
                                    it.copyTo(FileKit.filesDir / fileName)
                                    painter.restart()
                                    
                                    if(fileName == "wallpaper.png") {
                                        app.reloadWallpaper()
                                    }
                                }
                            )
                        }
                    }
                    
                    Item("avatar.png", "Avatar")
                    Item("wallpaper.png", "Wallpaper")
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Normal,
                    text = "Wallpaper opacity"
                )

                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = AwerySettings.wallpaperOpacity.collectAsState().value / 100f,
                    onValueChange = { runBlocking { AwerySettings.wallpaperOpacity.set((it * 100).roundToInt()) } }
                )
            }
        }
    }
    
    @Serializable
    data object End: IntroStep {
        private fun createParty(
            durationMs: Int, 
            amountPerSec: Int, 
            delayMs: Int, 
            spread: Int, 
            angle: Int, 
            x: Double
        ) = Party(
            emitter = Emitter(durationMs.milliseconds).perSecond(amountPerSec),
            shapes = listOf(Shape.Square, Shape.Circle),
            position = Position.Relative(x, .3),
            delay = delayMs,
            spread = spread,
            angle = angle,
            timeToLive = 4000,
            speed = 0f,
            maxSpeed = 30f
        )
        
        @Composable
        override fun Content(
            singleStep: Boolean,
            contentPadding: PaddingValues
        ) {
            ConfettiKit(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    createParty(250, 300, 0, 360, 0, .5),
                    createParty(275, 225, 300, 200, 150, .4),
                    createParty(275, 225, 300, 200, 330, .6)
                )
            )
            
            IntroDefaults.page(
                contentPadding = contentPadding,
                title = stringResource(Res.string.status_finished),
                description = stringResource(Res.string.setup_finished_description),

                icon = {
                    Icon(
                        modifier = Modifier.size(IntroDefaults.iconSize),
                        painter = painterResource(Res.drawable.ic_done),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                },

                actions = {
                    val introNavigation = IntroDefaults.navigation.current()
                    val appNavigation = Navigation.current()

                    TextButton(onClick = { introNavigation.safePop() }) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(Res.string.back)
                        )
                    }

                    Button(onClick = { 
                        appNavigation.clear()
                        appNavigation.push(MainRoutes.entries[AwerySettings.mainDefaultTab.value.ordinal].route)
                    }) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(Res.string.finish)
                        )
                    }
                }
            )
        }
    }
    
    @Composable
    fun Content(singleStep: Boolean, contentPadding: PaddingValues)
}

@Composable
fun IntroScreen(
    destination: Routes.Intro,
    contentPadding: PaddingValues
) {
    JetpackNavigationHost(
        modifier = Modifier.fillMaxSize(),
        navigation = rememberJetpackNavigation(destination.step),
        enterTransition = { slideInHorizontally(tween(500)) { it } },
        exitTransition = { slideOutHorizontally(tween(500)) { -it } },
        graph = remember {
            sealedNavigationGraph {
                it.Content(
                    singleStep = destination.singleStep,
                    contentPadding = contentPadding
                )
            }
        }
    )
}