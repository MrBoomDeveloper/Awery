package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_image_outlined
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource

@Composable
fun FilePicker(
	modifier: Modifier = Modifier,
	fileType: FileKitType = FileKitType.File(),
	preview: @Composable BoxScope.() -> Unit,
	onPicked: suspend (PlatformFile) -> Unit
) {
	val launcher = rememberFilePickerLauncher(fileType) { 
		if(it != null) {
			runBlocking {
				onPicked(it)
			}
		}
	}

	Card(
		modifier = modifier,
		onClick = launcher::launch
	) {
		Box(Modifier.fillMaxSize()) {
			preview()

			Spacer(
				modifier = Modifier
					.background(Color(0x55000000))
					.matchParentSize()
			)

			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				Icon(
					modifier = Modifier
						.size(40.dp)
						.padding(bottom = 8.dp),
					painter = painterResource(Res.drawable.ic_image_outlined),
					tint = Color.White,
					contentDescription = null
				)
				
				Text(
					color = Color.White,
					textAlign = TextAlign.Center,
					text = "Select an " + when(fileType) {
						is FileKitType.File -> "file"
						FileKitType.Image -> "image"
						FileKitType.Video -> "video"
						FileKitType.ImageAndVideo -> "image or video"
					}
				)
			}
		}
	}
}