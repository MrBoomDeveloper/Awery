package com.mrboomdev.awery.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.history
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_delete_outlined
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.navigation.RouteInfo
import com.mrboomdev.awery.ui.navigation.RouteInfoEffect
import com.mrboomdev.awery.ui.navigation.Routes
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.niceSideInset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.awt.SystemColor.text

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues
) {
    val navigation = Navigation.current()
    val coroutineScope = rememberCoroutineScope()
    val historyItems by Awery.database.history.media.observeAll().collectAsState(emptyList())
    var isLoading by remember { mutableStateOf(false) }

    RouteInfoEffect(
        title = "History"
    )
    
    if(historyItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("History is empty")
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding
            .add(horizontal = niceSideInset())
    ) { 
        items(
            items = historyItems,
            key = { "${it.extensionId};;;${it.mediaId}" }
        ) { historyItem ->
            Surface(
                modifier = Modifier.animateItem(),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                onClick = {
                    navigation.push(Routes.Media(
                        extensionId = historyItem.extensionId,
                        extensionName = null,
                        media = historyItem.media
                    ))
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .height(112.dp)
                            .aspectRatio(9f / 16f),
                        
                        model = historyItem.media.getPoster(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            maxLines = 3,
                            text = historyItem.media.title
                        )
                        
                        historyItem.media.description?.also { description ->
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                text = htmlToAnnotatedString(
                                    html = description.trim(),
                                    compactMode = true
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) { 
                                isLoading = true
                                Awery.database.history.media.delete(historyItem)
                                isLoading = false
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.ic_delete_outlined),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}