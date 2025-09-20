package com.mrboomdev.awery.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrboomdev.awery.core.utils.LoadingStatus
import com.mrboomdev.awery.core.utils.retryUntilSuccess
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.has
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.resources.AweryFonts
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.adult_content
import com.mrboomdev.awery.resources.ic_explict_outlined
import com.mrboomdev.awery.resources.no_extensions_found
import com.mrboomdev.awery.resources.no_media_found
import com.mrboomdev.awery.resources.nothing_found
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.DefaultExtImage
import com.mrboomdev.awery.ui.components.ExtImage
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.formatAsLanguage
import com.mrboomdev.awery.ui.utils.singleItem
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    viewModel: MainScreenViewModel,
    contentPadding: PaddingValues,
    query: String
): () -> Unit {
    val navigation = Navigation.current()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isLoadingExtensions by Extensions.observeIsLoading().collectAsState(true)

    val extensions by remember(query) {
        Extensions.observeAll(enabled = true).map { extensions ->
			extensions.filter { extension ->
                extension.name.contains(query) && 
                        extension.has<CatalogModule>() &&
                        when(AwerySettings.adultContent.state.value) {
                            AwerySettings.AdultContent.SHOW -> true
                            AwerySettings.AdultContent.HIDE -> !extension.isNsfw
                            AwerySettings.AdultContent.ONLY -> extension.isNsfw
                        }
            }.sortedBy { it.name }.map { extension ->
                extension to listOfNotNull(
                    extension.lang?.formatAsLanguage(),
                    extension.id.takeIf { AwerySettings.showIds.value }
                ).joinToString(" • ").let { subtitle ->
                    if(subtitle.isNotBlank() || extension.isNsfw) {
                        buildAnnotatedString {
                            if(extension.isNsfw) {
                                appendInlineContent("nsfw")
                                append(" ")
                            }

                            append(subtitle)
                        }
                    } else null
                }
            }
        }
    }.collectAsState(emptyList())
    
    val inlineContent = remember {
        mapOf(
            "nsfw" to InlineTextContent(
                Placeholder(
                    width = 14.sp,
                    height = 14.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_explict_outlined),
                    contentDescription = stringResource(Res.string.adult_content)
                )
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        if(isLoadingExtensions) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding.add(bottom = 16.dp),
            verticalArrangement = if(extensions.isEmpty()) {
                Arrangement.Center
            } else Arrangement.Top
        ) {
            singleItem("scrollFix")

            items(
                items = extensions,
                key = { it.first.id },
                contentType = { "extension" }
            ) { (extension, subtitle) ->
                Row(
                    modifier = Modifier
                        .clickable { navigation.push(Routes.Extension(extension.id, extension.name)) }
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .heightIn(min = 48.dp)
                        .animateItem(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    extension.icon?.also { icon ->
                        ExtImage(Modifier.size(36.dp), icon)
                    } ?: run {
                        DefaultExtImage(Modifier.size(36.dp))
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(extension.name)

                        subtitle?.also {
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                inlineContent = inlineContent,
                                text = it
                            )
                        }
                    }
                }
            }

            if(!isLoadingExtensions && extensions.isEmpty()) {
                singleItem("empty") {
                    InfoBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                            .animateItem(),
                        title = stringResource(Res.string.nothing_found),
                        message = stringResource(Res.string.no_extensions_found)
                    )
                }
            }
        }
    }
    
    return {
        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }
}