package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_add
import com.mrboomdev.awery.resources.ic_bookmarks_outlined
import com.mrboomdev.awery.resources.ic_delete_outlined
import com.mrboomdev.awery.resources.ic_edit_outlined
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.popups.CreateListDialog
import com.mrboomdev.awery.ui.popups.EditListDialog
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.plus
import com.mrboomdev.awery.ui.utils.singleItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsListsPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?
) {
	var showCreateDialog by remember { mutableStateOf(false) }

	if(showCreateDialog) {
		CreateListDialog({ showCreateDialog = false })
	}
	
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text("Lists") },
		fab = {
			FloatingActionButton(
				modifier = Modifier.padding(16.dp),
				onClick = { showCreateDialog = true }
			) {
				Icon(
					modifier = Modifier.size(32.dp),
					painter = painterResource(Res.drawable.ic_add),
					contentDescription = null
				)
			}
		}
	) { contentPadding ->
		val lists by Awery.database.lists.observeAll().collectAsState(emptyList())
		val listsCount by Awery.database.lists.observeCount().collectAsState(-1)
		
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding.add(bottom = 16.dp),
			verticalArrangement = if(lists.isEmpty()) Arrangement.Center else Arrangement.Top
		) { 
			singleItem("scrollFixer")
				
			items(
				items = lists,
				key = { it.id }
			) { list ->
				var showEditDialog by remember { mutableStateOf(false) }
					
				if(showEditDialog) {
					EditListDialog(list) { showEditDialog = false }
				}

				OutlinedCard(
					modifier = Modifier
						.fillMaxWidth()
						.animateItem(),
					onClick = { showEditDialog = true }
				) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 24.dp, vertical = 16.dp),
						verticalAlignment = Alignment.CenterVertically
					) { 
						Text(
							modifier = Modifier.weight(1f),
							color = MaterialTheme.colorScheme.onBackground,
							text = list.name
						)

						Icon(
							modifier = Modifier.size(24.dp),
							painter = painterResource(Res.drawable.ic_edit_outlined),
							contentDescription = null
						)
					}
				}
			}
				
			if(listsCount == 0) {
				singleItem("empty") {
					InfoBox(
						modifier = Modifier
							.fillMaxSize()
							.wrapContentSize()
							.padding(bottom = 64.dp)
							.animateItem(),
						icon = painterResource(Res.drawable.ic_bookmarks_outlined),
						title = "No lists",
						message = "You don't have any lists. Create them to bookmark media to your library!"
					)
				}
			} else if(lists.isEmpty()) {
				singleItem("loading") {
					CircularProgressIndicator(
						modifier = Modifier
							.fillMaxSize()
							.wrapContentSize()
							.animateItem()
					)
				}
			}
		}
	}
}