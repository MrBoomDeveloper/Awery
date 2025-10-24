package com.mrboomdev.awery.ui.popups

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.LayoutBoundsHolder
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter.State.Empty.painter
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.retryUntilSuccess
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.DBList
import com.mrboomdev.awery.data.database.entity.DBListMediaCrossRef
import com.mrboomdev.awery.data.database.entity.toDBMedia
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.bookmark
import com.mrboomdev.awery.resources.close
import com.mrboomdev.awery.resources.done
import com.mrboomdev.awery.resources.ic_add
import com.mrboomdev.awery.resources.ic_close
import com.mrboomdev.awery.resources.ic_done
import com.mrboomdev.awery.resources.new_list
import com.mrboomdev.awery.ui.components.BottomSheetDialog
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.exclude
import com.mrboomdev.awery.ui.utils.only
import com.mrboomdev.awery.ui.utils.padding
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.singleStickyHeader
import com.mrboomdev.awery.ui.utils.start
import com.mrboomdev.awery.ui.utils.thenIf
import com.mrboomdev.awery.ui.utils.toDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.SystemColor.text

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookmarkMediaDialog(
    extensionId: String,
    media: Media,
    onDismissRequest: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val lists = remember { mutableStateListOf<DBList>() }
    val selectedLists = remember { mutableStateListOf<DBList>() }
    var ogSignature by remember { mutableStateOf<String?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    var didRequestFocus by remember { mutableStateOf(false) }

    /**
     * Used to check whatever changes were made
     */
    fun generateSignature() = selectedLists
        .map { it.id }
        .sorted()
        .joinToString()

    LaunchedEffect(Unit) {
        launch(Dispatchers.Default) {
            lists += Awery.database.lists.getAll()
            selectedLists += Awery.database.media.getMediaLists(extensionId, media.id)

            if(lists.isEmpty()) {
                with(Awery.database.lists) {
                    lists += get(insert(DBList(name = "Uncategorized")))
                }
            }

            ogSignature = generateSignature()
            isLoading = false
        }
    }
    
    fun save() {
        isLoading = true
        
        if(ogSignature == generateSignature()) {
            // Nothing changed, so don't touch the database.
            onDismissRequest()
            return
        }

        coroutineScope.launch(Dispatchers.Default) {
            Awery.database.media.insert(media.toDBMedia(extensionId))

            selectedLists.forEach { list ->
                Awery.database.media.insert(DBListMediaCrossRef(
                    mediaExtensionId = extensionId,
                    mediaId = media.id,
                    listId = list.id
                ))
            }

            onDismissRequest()
        }
    }
    
    BottomSheetDialog(onDismissRequest = ::save) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val fabBounds = remember { LayoutBoundsHolder() }
            
            val contentPadding = WindowInsets.safeContent.only(
                WindowInsetsSides.Vertical
            ).asPaddingValues()
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = contentPadding.only(bottom = true)
                    .add(bottom = animateDpAsState(fabBounds.bounds?.height?.toDp() ?: 0.dp).value + 8.dp)
            ) {
                singleStickyHeader("header") { 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 16.dp, start = contentPadding.start + 32.dp, end = 16.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Normal,
                            text = "${stringResource(Res.string.bookmark)} \"${media.title}\""
                        )

                        IconButton(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 16.dp)
                                .size(48.dp),
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = stringResource(Res.string.close),
                            onClick = onDismissRequest
                        )
                    }
                }
                
                if(isLoading) {
                    singleItem("loading") {
                        LoadingIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                                .wrapContentSize()
                                .animateItem()
                        )
                    }
                }

                itemsIndexed(
                    items = lists,
                    key = { _, list -> list.id }
                ) { index, list ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val value = list in selectedLists

                    fun onChangeValue(newValue: Boolean) {
                        if(newValue) {
                            selectedLists += list
                        } else selectedLists -= list
                    }

                    Row(
                        modifier = Modifier
                            .onGloballyPositioned {
                                if(it.isAttached && index == 0 && !didRequestFocus) {
                                    focusRequester.requestFocus()
                                    didRequestFocus = true
                                }
                            }.fillMaxWidth()
                            .toggleable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                enabled = !isLoading,
                                value = value,
                                onValueChange = ::onChangeValue
                            ).padding(horizontal = 18.dp, vertical = 4.dp)
                            .thenIf(index == 0) { focusRequester(focusRequester) }
                            .animateItem(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            interactionSource = interactionSource,
                            enabled = !isLoading,
                            checked = value,
                            onCheckedChange = ::onChangeValue
                        )

                        Text(list.name)
                    }
                }
            }
            
            SmallExtendedFloatingActionButton(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(bottom = 8.dp, end = 16.dp)
                    .align(Alignment.BottomEnd)
                    .layoutBounds(fabBounds),
                
                onClick = {
                    showCreateDialog = true
                }
            ) {
                Row(
                    modifier = Modifier.offset(x = (-8).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null
                    )

                    Text(stringResource(Res.string.new_list))
                }
            }
        }
    }

    if(showCreateDialog) {
        CreateListDialog(
            onDismissRequest = { showCreateDialog = false },
            onListCreated = { lists += it }
        )
    }
}