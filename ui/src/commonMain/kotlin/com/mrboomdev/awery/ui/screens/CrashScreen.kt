package com.mrboomdev.awery.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.utils.add
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
					windowInsets = WindowInsets.safeContent,
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