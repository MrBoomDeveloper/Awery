package com.mrboomdev.awery.ui.screens.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.screens.main.NotificationsPage
import com.mrboomdev.awery.ui.utils.viewModel

@Composable
fun NotificationsScreen(
	viewModel: NotificationsViewModel = viewModel { NotificationsViewModel() },
	contentPadding: PaddingValues
) {
	NotificationsPage(
		contentPadding = contentPadding
	)
}