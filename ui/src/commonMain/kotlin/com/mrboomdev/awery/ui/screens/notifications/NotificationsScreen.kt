package com.mrboomdev.awery.ui.screens.notifications

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_notifications_outlined
import com.mrboomdev.awery.resources.reload
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.utils.niceSideInset
import com.mrboomdev.awery.ui.utils.viewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.SystemColor.text

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationsScreen(
	viewModel: NotificationsViewModel = viewModel { NotificationsViewModel() },
	contentPadding: PaddingValues
) {
	val isReloading by viewModel.isReloading.collectAsState()
	val state by viewModel.state.collectAsState()

	PullToRefreshBox(
		isRefreshing = isReloading,
		onRefresh = viewModel::reload
	) {
		Crossfade(state) { state ->
			when(state) {
				is NotificationsState.Loading -> {
					LoadingIndicator(
						modifier = Modifier
							.fillMaxSize()
							.wrapContentSize(Alignment.Center)
					)
				}

				is NotificationsState.Loaded if state.items.isEmpty() -> {
					InfoBox(
						modifier = Modifier
							.fillMaxSize()
							.wrapContentSize(Alignment.Center),
						icon = painterResource(Res.drawable.ic_notifications_outlined),
						title = "No notifications",
						message = "Maybe they'll appear soon... Who knows?",
						actions = {
							action(stringResource(Res.string.reload)) {
								viewModel.reload()
							}
						}
					)
				}

				is NotificationsState.Error -> {
					InfoBox(
						modifier = Modifier
							.fillMaxSize()
							.wrapContentSize(Alignment.Center),
						throwable = state.throwable
					)
				}

				is NotificationsState.Loaded -> {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						contentPadding = contentPadding
					) {
						items(
							items = state.items
						) { item ->
							Column(
								modifier = Modifier
									.padding(horizontal = niceSideInset(), vertical = 8.dp),
								verticalArrangement = Arrangement.spacedBy(4.dp)
							) {
								Text(
									style = MaterialTheme.typography.titleMedium,
									text = item.title
								)
								
								Text(
									style = MaterialTheme.typography.bodyMedium,
									text = item.message
								)
							}
						}
					}
				}
			}
		}
	}
}