package com.mrboomdev.awery.ui.screens.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.ui.utils.*

@DslMarker
private annotation class IntroDslMarker

@IntroDslMarker
interface IntroDsl {
    var iconSize: Dp
    var icon: @Composable () -> Painter?
    var title: @Composable () -> String?
    var description: @Composable () -> String?
    
    var primaryContent: (@Composable ColumnScope.(PaddingValues) -> Unit)?
    var primaryContentAlignment: Alignment.Horizontal
    
    var secondaryContent: (@Composable ColumnScope.(PaddingValues) -> Unit)?
    var secondaryContentAlignment: Alignment.Horizontal
    
    fun addAction(content: IntroActionDsl.() -> Unit)
    var actionsAlignment: Alignment.Horizontal
    var actionScale: Float
}

fun IntroDsl.setAlignment(alignment: Alignment.Horizontal) {
    primaryContentAlignment = alignment
    secondaryContentAlignment = alignment
    actionsAlignment = alignment
}

@IntroDslMarker
interface IntroActionDsl {
    var text: String
    var enabled: Boolean
    var onClick: () -> Unit
}

@Composable
fun IntroDslWrapper(
    contentPadding: PaddingValues = PaddingValues.Zero,
    content: IntroDsl.() -> Unit
) {
    val windowSize = currentWindowSize()
    
    val contentImpl = remember(content) {
        object : IntroDsl {
            val actions = mutableListOf<IntroActionDsl>()
            override var actionScale = 1f
            override var iconSize = IntroDefaults.iconSize
            override var icon: @Composable () -> Painter? = { null }
            override var title: @Composable () -> String? = { null }
            override var description: @Composable () -> String? = { null }
            override var primaryContent: @Composable (ColumnScope.(PaddingValues) -> Unit)? = null
            override var primaryContentAlignment = Alignment.Start
            override var secondaryContent: @Composable (ColumnScope.(PaddingValues) -> Unit)? = null
            override var secondaryContentAlignment = Alignment.Start
            override var actionsAlignment = Alignment.Start

            override fun addAction(content: IntroActionDsl.() -> Unit) {
                actions += object : IntroActionDsl {
                    override lateinit var text: String
                    override var enabled = true
                    override lateinit var onClick: () -> Unit
                }.apply(content)
            }
        }.apply(content)
    }
    
    val icon: (@Composable () -> Unit)? = contentImpl.icon()?.let { icon ->
        when(icon) {
            is VectorPainter -> {{
                Icon(
                    modifier = Modifier.size(contentImpl.iconSize),
                    tint = MaterialTheme.colorScheme.primary,
                    painter = icon,
                    contentDescription = null
                )
            }}

            else -> {{
                Image(
                    modifier = Modifier.size(contentImpl.iconSize),
                    painter = icon,
                    contentDescription = null
                )
            }}
        }
    }

    if(windowSize.width >= WindowSizeType.Large) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(64.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding.only(top = true, horizontal = true))
                    .padding(start = niceSideInset()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = contentImpl.primaryContentAlignment
            ) {
                icon?.invoke()

                contentImpl.title()?.also { title ->
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal,
                        textAlign = contentImpl.primaryContentAlignment.toTextAlign(),
                        text = title
                    )
                }

                contentImpl.description()?.also { description ->
                    Text(
                        textAlign = contentImpl.primaryContentAlignment.toTextAlign(),
                        text = description
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = contentImpl.secondaryContentAlignment
                ) {
                    contentImpl.secondaryContent?.invoke(this, contentPadding.only(
                        top = true, end = true
                    ).add(bottom = 64.dp, end = niceSideInset()))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding.only(bottom = true, end = true))
                        .padding(end = niceSideInset())
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(IntroDefaults.spaceBetweenActions, contentImpl.actionsAlignment)
                ) {
                    contentImpl.actions.forEach { action ->
                        Button(
                            enabled = action.enabled,
                            onClick = action.onClick
                        ) {
                            Text(action.text)
                        }
                    }
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding.only(top = true, start = true, end = true)),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = contentImpl.primaryContentAlignment
            ) {
                if(icon != null && contentImpl.primaryContentAlignment == Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                }
                
                icon?.invoke()

                contentImpl.title()?.also { title ->
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal,
                        textAlign = contentImpl.primaryContentAlignment.toTextAlign(),
                        text = title
                    )
                }

                contentImpl.description()?.also { description ->
                    Text(
                        textAlign = contentImpl.primaryContentAlignment.toTextAlign(),
                        text = description
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = contentImpl.secondaryContentAlignment
                ) {
                    contentImpl.secondaryContent?.invoke(this, contentPadding.only(
                        /*horizontal = true*/
                    ).add(vertical = 16.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding.only(horizontal = true, bottom = true)),
                horizontalArrangement = Arrangement.spacedBy(IntroDefaults.spaceBetweenActions, contentImpl.actionsAlignment)
            ) {
                contentImpl.actions.forEach { action ->
                    Button(
                        enabled = action.enabled,
                        onClick = action.onClick,

                        contentPadding = ButtonDefaults.ContentPadding * 
                                (contentImpl.actionScale.takeUnless { it == 1f }?.times(1.25f) ?: 1f)
                    ) {
                        Text(
                            fontSize = LocalTextStyle.current.fontSize * contentImpl.actionScale,
                            text = action.text
                        )
                    }
                }
            }
            
            if(contentImpl.actionScale > 1f) {
                Spacer(Modifier.height((contentImpl.actionScale * 10f).dp))
            }
        }
    }
}