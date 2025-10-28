package com.mrboomdev.awery.ui.screens.intro.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_mood_outlined
import com.mrboomdev.awery.resources.username
import com.mrboomdev.awery.ui.LocalApp
import com.mrboomdev.awery.ui.components.FilePicker
import com.mrboomdev.awery.ui.screens.intro.IntroDefaults
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

@Serializable
data object IntroUserStep: IntroStep {
    @Composable
    override fun Content(
        singleStep: Boolean,
        contentPadding: PaddingValues
    ) = IntroDefaults.page(
        contentPadding = contentPadding,
        title = "Customize experience",
        description = "Some inspirational text will appear here soon. if i'll won't forget about it.",
        canOpenNextStep = AwerySettings.username.collectAsState().value.isNotBlank(),
        nextStep = { IntroThemeStep }.takeUnless { singleStep },

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
            val isLoggedIn = AwerySettings.aweryServerToken.collectAsState().value.isNotEmpty()
            
            if(!isLoggedIn) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = AwerySettings.username.collectAsState().value,
                    onValueChange = { runBlocking { AwerySettings.username.set(it) } },
                    label = { Text(stringResource(Res.string.username)) },
                    singleLine = true
                )
            }
            
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