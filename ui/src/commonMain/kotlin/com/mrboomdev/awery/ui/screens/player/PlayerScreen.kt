package com.mrboomdev.awery.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.formatTime
import com.mrboomdev.awery.core.utils.next
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_aspect_ratio_outlined
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.ic_crop_landscape_outlined
import com.mrboomdev.awery.resources.ic_fit_screen_outlined
import com.mrboomdev.awery.resources.ic_pause_filled
import com.mrboomdev.awery.resources.ic_play_filled
import com.mrboomdev.awery.resources.ic_settings_outlined
import com.mrboomdev.awery.resources.ic_skip_next_filled
import com.mrboomdev.awery.resources.ic_skip_previous_filled
import com.mrboomdev.awery.resources.ic_subtitles_outlined
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.MediaPlayer
import com.mrboomdev.awery.ui.components.MediaPlayerState
import com.mrboomdev.awery.ui.components.Saver
import com.mrboomdev.awery.ui.components.Toaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.effects.InsetsController
import com.mrboomdev.awery.ui.effects.KeepScreenOn
import com.mrboomdev.awery.ui.effects.RequestScreenOrientation
import com.mrboomdev.awery.ui.effects.ScreenOrientation
import com.mrboomdev.awery.ui.utils.enumStateSaver
import com.mrboomdev.awery.ui.utils.saveable
import com.mrboomdev.awery.ui.utils.viewModel
import com.mrboomdev.navigation.core.Navigation
import com.mrboomdev.navigation.core.safePop
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private fun AwerySettings.PlayerFitMode.toContentScale() = when(this) {
    AwerySettings.PlayerFitMode.FILL -> ContentScale.FillBounds
    AwerySettings.PlayerFitMode.FIT -> ContentScale.Fit
    AwerySettings.PlayerFitMode.CROP -> ContentScale.Crop
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    destination: Routes.Player,
    viewModel: PlayerScreenViewModel = run {
        val navigation = Navigation.current()
        val toaster = LocalToaster.current
        viewModel { PlayerScreenViewModel(destination, navigation, toaster, it) }
    }
) {
    val coroutineScope = rememberCoroutineScope()
    val navigation = Navigation.current()

    var hideUiJob by remember { mutableStateOf<Job?>(null) }
    var isPausedByUser by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    val dialogs = remember { mutableStateListOf<PlayerDialog>() }

    var contentScale by rememberSaveable(saver = enumStateSaver()) {
        mutableStateOf(AwerySettings.defaultPlayerFitMode.value)
    }

    // We cannot check if user did leave pip mode or not
    // so media continues playing, which is a bad thing.
    // Uncomment once fixed.
//    val pipState = rememberPictureInPictureState(
//        autoEnter = !viewModel.player.isPaused,
//        aspectRatio = viewModel.player.aspectRatio
//    )

    fun launchHideUiJob() {
        hideUiJob?.cancel()

        hideUiJob = coroutineScope.launch {
            delay(2500)
            if(!viewModel.player.isPaused) showControls = false
        }
    }

    fun onScreenClick() {
        showControls = !showControls
        if(showControls) launchHideUiJob()
    }

    LaunchedEffect(viewModel.player.isPaused, viewModel.player.isLoading) {
        if(showControls && !viewModel.player.isLoading) {
            launchHideUiJob()
        }
    }

    RequestScreenOrientation(ScreenOrientation.LANDSCAPE)
    InsetsController(hideBars = !showControls)
    if(!viewModel.player.isPaused) KeepScreenOn()
    dialogs.forEach { it() }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = ::onScreenClick
            )
    ) {
        MediaPlayer(
            modifier = Modifier.fillMaxSize(),
            state = viewModel.player,
            contentScale = contentScale.toContentScale()
        )

        AnimatedVisibility(
            visible = showControls/* && !pipState.isActive*/,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spacer(Modifier.background(Color(0x88000000)).fillMaxSize())
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(96.dp)
        ) {
            fun doubleTapSeek(offset: Int) {
                viewModel.player.seekTo(viewModel.player.position + AwerySettings.playerDoubleTapSeek.value * 1000 * offset)
            }

            DoubleTapZone(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                offset = -1,
                onTap = ::onScreenClick,
                onDoubleTap = { doubleTapSeek(-1) }
            )

            DoubleTapZone(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                offset = 1,
                onTap = ::onScreenClick,
                onDoubleTap = { doubleTapSeek(1) }
            )
        }

        AnimatedVisibility(
            visible = showControls/* && !pipState.isActive*/,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeContent.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            padding = 4.dp,
                            onClick = { navigation.safePop() },
                            painter = painterResource(Res.drawable.ic_back),
                            contentDescription = null
                        )

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            text = destination.title
                        )

                        IconButton(
                            padding = 10.dp,
                            onClick = { dialogs += SubtitlesDialog { dialogs -= it } },
                            painter = painterResource(Res.drawable.ic_subtitles_outlined),
                            contentDescription = null
                        )

                        IconButton(
                            padding = 10.dp,
                            onClick = { dialogs += QualityDialog { dialogs -= it } },
                            painter = painterResource(Res.drawable.ic_settings_outlined),
                            contentDescription = null
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.size(56.dp),
                            enabled = false,
                            onClick = {},
                            painter = painterResource(Res.drawable.ic_skip_previous_filled),
                            contentDescription = null
                        )

                        if(viewModel.player.isLoading) {
                            Spacer(Modifier.size(96.dp))
                        } else {
                            IconButton(
                                modifier = Modifier.size(96.dp),
                                onClick = {
                                    if(viewModel.player.isPaused) {
                                        viewModel.player.play()
                                        isPausedByUser = false
                                        launchHideUiJob()
                                    } else {
                                        hideUiJob?.cancel()
                                        viewModel.player.pause()
                                        isPausedByUser = true
                                    }
                                },
                                painter = painterResource(if(viewModel.player.isPaused) {
                                    Res.drawable.ic_play_filled
                                } else Res.drawable.ic_pause_filled),
                                contentDescription = null
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(56.dp),
                            enabled = false,
                            onClick = {},
                            painter = painterResource(Res.drawable.ic_skip_next_filled),
                            contentDescription = null
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeContent.only(
                                WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("${viewModel.player.position.formatTime()}/${viewModel.player.duration.formatTime()}")

                            Spacer(Modifier.weight(1f))

                            Row(
                                modifier = Modifier.offset(y = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    padding = 10.dp,
                                    contentDescription = null,

                                    painter = painterResource(when(contentScale) {
                                        AwerySettings.PlayerFitMode.FIT -> Res.drawable.ic_fit_screen_outlined
                                        AwerySettings.PlayerFitMode.FILL -> Res.drawable.ic_aspect_ratio_outlined
                                        AwerySettings.PlayerFitMode.CROP -> Res.drawable.ic_crop_landscape_outlined
                                    }),

                                    onClick = {
                                        contentScale = contentScale.next()
                                        launchHideUiJob()
                                    }
                                )
                            }
                        }

                        val interactionSource = remember { MutableInteractionSource() }
                        val interactions = remember { mutableStateListOf<Interaction>() }

                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect { interaction ->
                                when(interaction) {
                                    is PressInteraction.Press -> interactions.add(interaction)
                                    is PressInteraction.Release -> interactions.remove(interaction.press)
                                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                                    is DragInteraction.Start -> interactions.add(interaction)
                                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                                }
                            }
                        }

                        LaunchedEffect(interactions) {
                            if(interactions.isNotEmpty()) {
                                showControls = true
                                hideUiJob?.cancel()
                                viewModel.player.pause()
                            } else {
                                if(isPausedByUser) return@LaunchedEffect
                                viewModel.player.play()
                                launchHideUiJob()
                            }
                        }

                        Slider(
                            modifier = Modifier.fillMaxWidth().zIndex(1f),
                            value = (viewModel.player.position / 1000).toFloat(),
                            valueRange = 0F..(viewModel.player.duration / 1000).toFloat(),

                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color(0x22FFFFFF)
                            ),

                            onValueChange = {
                                showControls = true
                                hideUiJob?.cancel()
                                viewModel.player.pause()
                                viewModel.player.seekTo((it * 1000).toLong())
                            },

                            onValueChangeFinished = {
                                if(isPausedByUser) return@Slider
                                viewModel.player.play()
                                launchHideUiJob()
                            }
                        )

                        // Customized material3 slider
//                        Slider(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 6.dp),
//                            interactionSource = sliderInteractionSource,
//                            value = (viewModel.player.position / 1000).toFloat(),
//                            valueRange = 0F..(viewModel.player.duration / 1000).toFloat(),
//
//                            onValueChange = {
//                                showControls = true
//                                hideUiJob?.cancel()
//                                viewModel.player.pause()
//                                viewModel.player.seekTo((it * 1000).toLong())
//                            },
//
//                            onValueChangeFinished = {
//                                if(isPausedByUser) return@Slider
//                                viewModel.player.play()
//                                launchHideUiJob()
//                            },
//
//                            track = { state ->
//                                Canvas(Modifier
//                                    .fillMaxWidth()
//                                    .height(4.dp)
//                                ) {
//                                    fun fuckingMagic(current: Long, max: Long): Float {
//                                        return (current.div(1000).toFloat() safeDivide max.div(1000).toFloat()) ?: 1F
//                                    }
//
//                                    val progressPercent = viewModel.player.let { player ->
//                                        fuckingMagic(player.position, player.duration)
//                                    }
//
//                                    val loadedPercent = viewModel.player.let { player ->
//                                        fuckingMagic(player.bufferedPosition, player.duration)
//                                    }
//
//                                    drawRoundRect(
//                                        color = Color(0x22FFFFFF),
//                                        size = Size(size.width, size.height),
//                                        cornerRadius = CornerRadius(size.height / 2)
//                                    )
//
//                                    drawRoundRect(
//                                        color = Color(0x44FFFFFF),
//                                        size = Size(size.width * loadedPercent, size.height),
//                                        cornerRadius = CornerRadius(size.height / 2)
//                                    )
//
//                                    drawRoundRect(
//                                        color = activeColor,
//                                        size = Size(size.width * progressPercent, size.height),
//                                        cornerRadius = CornerRadius(size.height / 2)
//                                    )
//                                }
//                            },
//
//                            thumb = { state ->
//                                val interactions = remember { mutableStateListOf<Interaction>() }
//
//                                LaunchedEffect(sliderInteractionSource) {
//                                    sliderInteractionSource.interactions.collect { interaction ->
//                                        when(interaction) {
//                                            is PressInteraction.Press -> interactions.add(interaction)
//                                            is PressInteraction.Release -> interactions.remove(interaction.press)
//                                            is PressInteraction.Cancel -> interactions.remove(interaction.press)
//                                            is DragInteraction.Start -> interactions.add(interaction)
//                                            is DragInteraction.Stop -> interactions.remove(interaction.start)
//                                            is DragInteraction.Cancel -> interactions.remove(interaction.start)
//                                        }
//                                    }
//                                }
//
//                                Spacer(Modifier
//                                    .size(50.dp)
//                                    .offset(y = 3.dp)
//                                    .scale(animateFloatAsState(if(interactions.isNotEmpty()) 1.25F else 1F).value)
//                                    .hoverable(interactionSource = sliderInteractionSource)
//                                    .background(activeColor, CircleShape))
//                            }
//                        )
                    }
                }
            }
        }

        if(viewModel.player.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(96.dp)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun DoubleTapZone(
    modifier: Modifier,
    offset: Int,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var clicksCount by remember { mutableIntStateOf(0) }

    var resetClicksCountJob by remember { mutableStateOf<Job?>(null) }
    var regularClickJob by remember { mutableStateOf<Job?>(null) }

    Spacer(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },

                indication = if(clicksCount > 0) {
                    LocalIndication.current
                } else null,

                onClick = {
                    resetClicksCountJob?.cancel()
                    regularClickJob?.cancel()

                    if(++clicksCount > 1) {
                        onDoubleTap()
                    } else {
                        regularClickJob = coroutineScope.launch {
                            delay(150)
                            onTap()
                        }
                    }

                    resetClicksCountJob = coroutineScope.launch {
                        delay(500)
                        clicksCount = 0
                    }
                }
            )
            .scale(2f)
    )
}

class PlayerScreenViewModel(
    destination: Routes.Player,
    navigation: Navigation<Routes>,
    toaster: Toaster,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val playerFactory = {
        MediaPlayerState(
            autoDispose = false,
            onEnd = {},
            onError = {
                //TODO: Suggest to try again
                Log.e("PlayerScreen", "Error occurred during playback!", it)
                toaster.toast("Error occurred during playback!")
                navigation.pop()
            }
        )
    }

    val player = savedStateHandle.saveable(
        key = "player",
        saver = MediaPlayerState.Saver(playerFactory),
        init = playerFactory
    )

    init {
        initPlayer(player)

        if(!player.didRestoreState) {
            player.setUrl(destination.video.url)
            player.play()
        }
    }

    override fun onCleared() {
        player.dispose()
    }
}

internal expect fun initPlayer(player: MediaPlayerState)