package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_block
import com.mrboomdev.awery.ui.utils.ExceptionClassifier
import com.mrboomdev.awery.ui.utils.classify
import org.jetbrains.compose.resources.painterResource

object InfoBoxActionsScope {
    @Composable
    fun action(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = text
            )
        }
    }
}

@Composable
fun InfoBox(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(64.dp),
    throwable: Throwable,
    actions: @Composable InfoBoxActionsScope.() -> Unit = {}
) {
    val explained = remember(throwable) { throwable.classify() }
    
    val icon = when(explained.type) {
        // TODO: Match icon with the error type and use this component everywhere in the app.
        ExceptionClassifier.Type.ACCESS_DENIED -> painterResource(Res.drawable.ic_block)
        else -> null
    }
    
    InfoBox(
        modifier = modifier,
        contentPadding = contentPadding,
        icon = icon,
        title = explained.title,
        message = explained.message,
        actions = actions
    )
}

@Composable
fun InfoBox(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(64.dp),
    icon: Painter? = null,
    title: String,
    message: String,
    actions: @Composable InfoBoxActionsScope.() -> Unit = {}
) {
    Box(
        modifier = modifier.padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.also {
                Icon(
                    modifier = Modifier
                        .size(112.dp)
                        .padding(bottom = 8.dp),
                    painter = it,
                    contentDescription = null
                )
            }
            
            Text(
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                text = title
            )

            Text(
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                text = message
            )
            
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                actions(InfoBoxActionsScope)
            }
        }
    }
}