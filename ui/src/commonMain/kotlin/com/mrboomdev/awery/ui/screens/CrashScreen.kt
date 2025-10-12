package com.mrboomdev.awery.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.utils.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashScreen(
	throwable: Throwable?,
	simpleThrowable: String?
) {
	AweryTheme {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			contentWindowInsets = WindowInsets.safeContent,
			topBar = {
				TopAppBar(
					windowInsets = WindowInsets.safeContent.only(
						WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
					title = { Text("Awery has crashed!") },
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
						scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
					)
				)
			}
		) { contentPadding ->
			SelectionContainer {
				Text(
					modifier = Modifier
						.fillMaxSize()
						.verticalScroll(rememberScrollState())
						.horizontalScroll(rememberScrollState())
						.padding(contentPadding + 16.dp),
					color = MaterialTheme.colorScheme.onBackground,
					text = throwable?.stackTraceToString() ?: simpleThrowable!!
				)
			}
		}
	}
}