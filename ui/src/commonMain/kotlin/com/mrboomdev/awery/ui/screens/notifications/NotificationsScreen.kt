package com.mrboomdev.awery.ui.screens.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_notifications_outlined
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.utils.viewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun NotificationsScreen(
	viewModel: NotificationsViewModel = viewModel { NotificationsViewModel() },
	contentPadding: PaddingValues
) {
	InfoBox(
		modifier = Modifier
			.fillMaxSize()
			.wrapContentSize(Alignment.Center),
		icon = painterResource(Res.drawable.ic_notifications_outlined),
		title = "No notifications",
		message = "They aren't supported yet"
	)
}