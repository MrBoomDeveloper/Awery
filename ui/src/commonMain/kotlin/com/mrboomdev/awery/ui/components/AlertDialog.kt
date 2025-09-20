package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * More customizable version of the original AlertDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor
        ) {
            val contentColor = AlertDialogDefaults.textContentColor

            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalTextStyle provides LocalTextStyle.current.copy(contentColor)
            ) {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    content = content
                )
            }
        }
    }
}

/**
 * More customizable version of the original AlertDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit = {},
    cancelButton: @Composable () -> Unit = {},
    neutralButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(top = 24.dp, bottom = 12.dp)
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineMedium
        ) {
            Box(Modifier.padding(horizontal = 24.dp)) {
                title()
            }
        }

        Column(
            modifier = Modifier
                .padding(contentPadding)
                .weight(1f, false),
            content = content
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge
                    .copy(color = MaterialTheme.colorScheme.primary)
            ) {
                neutralButton()
                Spacer(Modifier.weight(1f))
                cancelButton()
                confirmButton()
            }
        }
    }
}