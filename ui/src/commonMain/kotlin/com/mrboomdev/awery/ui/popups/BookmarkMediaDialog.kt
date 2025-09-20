package com.mrboomdev.awery.ui.popups

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.retryUntilSuccess
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.DBList
import com.mrboomdev.awery.data.database.entity.DBListMediaCrossRef
import com.mrboomdev.awery.data.database.entity.toDBMedia
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.done
import com.mrboomdev.awery.resources.ic_add
import com.mrboomdev.awery.resources.ic_done
import com.mrboomdev.awery.resources.new_list
import com.mrboomdev.awery.ui.components.BottomSheetDialog
import com.mrboomdev.awery.ui.utils.thenIf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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

    BottomSheetDialog(
        onDismissRequest = ::save
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 8.dp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    text = buildAnnotatedString {
                        append("Bookmark \"")

                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(media.title)
                        }

                        append("\"")
                    }
                )

                TextButton(
                    enabled = !isLoading,
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(Res.string.new_list)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth()
            )

            Crossfade(
                modifier = Modifier
                    .weight(1f, false)
                    .animateContentSize(),
                targetState = isLoading
            ) { isLoading ->
                if(isLoading) {
                    CircularProgressIndicator(Modifier.padding(32.dp))
                    return@Crossfade
                }

                LazyColumn(Modifier.fillMaxWidth()) {
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
                                }
                                .fillMaxWidth()
                                .toggleable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    enabled = !isLoading,
                                    value = value,
                                    onValueChange = ::onChangeValue
                                )
                                .padding(horizontal = 18.dp, vertical = 4.dp)
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
            }

            HorizontalDivider()
            
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                enabled = !isLoading,
                onClick = ::save
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(Res.drawable.ic_done),
                    contentDescription = null
                )

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(Res.string.done)
                )
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