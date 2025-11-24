package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.isValidUrl
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.core.utils.toInt
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.repo.Repositories
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.extension.loaders.ExtensionInstaller
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.has
import com.mrboomdev.awery.extension.sdk.modules.ManageableModule
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.components.*
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.utils.*
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.SystemColor.text

class SettingsExtensionsPageViewModel: ViewModel() {
	val repositories = Awery.database.repositories.observeAll().stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = emptyList()
	)
	
	@OptIn(ExperimentalCoroutinesApi::class)
	val extensions = Extensions.observeAll()
		.mapLatest { extensions ->
			extensions.filter { extension ->
				extension.has<ManageableModule>()
			}.sortedBy { it.name }
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = emptyList()
		)
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsExtensionsPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onOpenPage: (SettingsPages) -> Unit,
	onBack: (() -> Unit)?,
	viewModel: SettingsExtensionsPageViewModel = viewModel { SettingsExtensionsPageViewModel() }
) {
	val repositories by viewModel.repositories.collectAsState()
	val extensions by viewModel.extensions.collectAsState()
	val isLoading by Extensions.observeIsLoading().collectAsState()
	val showRepos by AwerySettings.expandRepositoriesList.collectAsState()
	var showRepoCreateDialog by remember { mutableStateOf(false) }
	
	if(showRepoCreateDialog) {
		val coroutineScope = rememberCoroutineScope()
		var job by remember { mutableStateOf<Job?>(null) }
		var url by remember { mutableStateOf("") }
		var error by remember { mutableStateOf<String?>(null) }
		
		fun confirm() {
			if(repositories.any { it.url == url.trim() }) {
				error = "Repository already exists!"
				return
			}
			
			job = coroutineScope.launch {
				if(!url.isValidUrl()) {
					error = getString(Res.string.invalid_url)
					job = null
					return@launch
				}
				
				try {
					Awery.database.repositories.add(Repositories.fetch(url.trim()).info)
					showRepoCreateDialog = false
				} catch(e: Exception) {
					error = e.message
				}
				
				job = null
			}
		}
		
		AlertDialog(
			onDismissRequest = { 
				job?.cancel()
				job = null
				showRepoCreateDialog = false 
			},
			
			title = { Text("Add new repository") },
			
			confirmButton = { 
				TextButton(
					enabled = job == null,
					onClick = ::confirm
				) { Text(stringResource(Res.string.confirm)) }
			},
			
			cancelButton = { 
				TextButton(
					enabled = job == null,
					onClick = { showRepoCreateDialog = false }
				) { Text(stringResource(Res.string.cancel)) } 
			}
		) {
			val focusRequester = remember { FocusRequester() }

			LaunchedEffect(Unit) {
				focusRequester.requestFocus()
			}
			
			OutlinedTextField(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 16.dp)
					.focusRequester(focusRequester),
				value = url,
				onValueChange = { url = it },
				enabled = job == null,
				singleLine = true,
				placeholder = { Text(stringResource(Res.string.repository_url)) },
				
				isError = error != null,
				label = error?.let {{ Text(it) }},
				
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Uri,
					imeAction = ImeAction.Done
				),
				
				keyboardActions = KeyboardActions(
					onDone = { confirm() }
				)
			)
		}
	}
	
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.extensions)) },
		fab = {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
				horizontalAlignment = Alignment.End
			) {
				val toaster = LocalToaster.current
				
				val filePicker = rememberFilePickerLauncher { file ->
					if(file == null) return@rememberFilePickerLauncher
					
					if(when(file.extension) {
						"png", "jpg", "jpeg", "mp3", "mp4", "txt", "gif", "webp" -> true
						else -> false
					}) {
						toaster.toast("This is not an extension!") 
						return@rememberFilePickerLauncher
					}
					
					launchGlobal {
						try {
							val extension = ExtensionInstaller.install(file)
							toaster.toast("Successfully installed \"${extension.name}\" ${extension.version}")
						} catch(t: Throwable) {
							Log.e("SettingsExtensionsPage", "Failed to install an extension", t)
							val classified = t.classify()
							
							toaster.toast(
								title = "Failed to install an extension!",
								message = classified.title + "\n" + classified.message
							)
						}
					}
				}
				
				SmallFloatingActionButton({ filePicker.launch() }) {
					Icon(
						modifier = Modifier.size(28.dp),
						painter = painterResource(Res.drawable.ic_sd_card_outlined),
						contentDescription = null
					)
				}
				
				FloatingActionButton({ showRepoCreateDialog = true }) {
					Icon(
						modifier = Modifier.size(32.dp),
						painter = painterResource(Res.drawable.ic_add),
						contentDescription = null
					)
				}
			}
		}
	) { contentPadding ->
		val inlineContent = remember {
			mapOf("nsfw" to InlineTextContent(
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
			})
		}
		
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(contentPadding)
		) {
			if(isLoading) {
				LinearProgressIndicator(
					modifier = Modifier.fillMaxWidth()
				)
			}

			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(bottom = 16.dp)
			) { 
				singleItem("scrollFixer") {
					Spacer(Modifier.fillMaxWidth().height(1.dp))
				}
				
				if(repositories.isNotEmpty()) {
					singleItem("reposHeader") {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable { launchGlobal(Dispatchers.Default) { AwerySettings.expandRepositoriesList.toggle() } }
								.padding(horizontal = 18.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Text(
								modifier = Modifier
									.weight(1f)
									.animateItem(),
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.onBackground,
								text = "Repositories"
							)
							
							val iconDegree by animateFloatAsState(
								90f + 180 * (1 - AwerySettings.expandRepositoriesList.collectAsState().value.toInt())
							)
							
							Icon(
								modifier = Modifier
									.width(48.dp)
									.height(24.dp)
									.padding(horizontal = 12.dp)
									.rotate(iconDegree),
								painter = painterResource(Res.drawable.ic_back),
								tint = MaterialTheme.colorScheme.onBackground,
								contentDescription = null,
							)
						}
					}
				}
				
				items(
					items = if(showRepos) repositories else emptyList(),
					key = { it.url },
					contentType = { "repo" }
				) { repo ->
					SettingsDefaults.itemCustom(
						modifier = Modifier
							.clickable { onOpenPage(SettingsPages.Repository(repo)) }
							.animateItem(),

						title = { Text(repo.name) },
						description = { Text(repo.url) }
					) {
						Box {
							var showDropdown by remember { mutableStateOf(false) }

							IconButton(
								padding = 10.dp,
								painter = painterResource(Res.drawable.ic_more_vertical),
								contentDescription = null,
								onClick = { showDropdown = true }
							)
							
							ContextMenu(
								isVisible = showDropdown,
								onDismissRequest = { showDropdown = false }
							) {
								item(
									icon = { painterResource(Res.drawable.ic_link) },
									text = { stringResource(Res.string.copy_link_to_clipboard) },
									onClick = { Awery.copyToClipboard(repo.url) }
								)

								item(
									icon = { painterResource(Res.drawable.ic_delete_outlined) },
									text = { stringResource(Res.string.remove) },
									onClick = { runBlocking { Awery.database.repositories.delete(repo) } }
								)
							}
						}
					}
				}
				
				if(repositories.isNotEmpty() && !showRepos) {
					singleItem("collapsedReposSpace") {
						Spacer(Modifier.height(8.dp).animateItem())
					}
				}

				// TODO: Check for updates
//				if(updates.isNotEmpty()) {
//					singleItem("updatesHeader") {
//						Text(
//							modifier = Modifier
//								.thenIf(repositories.isNotEmpty()) { padding(top = 8.dp) }
//								.padding(horizontal = 18.dp)
//								.animateItem(),
//							style = MaterialTheme.typography.titleMedium,
//							color = MaterialTheme.colorScheme.onBackground,
//							text = "Updates available"
//						)
//					}
//				}

				if(extensions.isNotEmpty()) {
					singleItem("extensionsHeader") {
						Text(
							modifier = Modifier
								.thenIf(repositories.isNotEmpty()) { padding(top = 8.dp) }
								.padding(horizontal = 18.dp)
								.animateItem(),
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.onBackground,
							text = "Installed extensions"
						)
					}
				}
				
				items(
					items = extensions,
					key = { it.id },
					contentType = { "ext" }
				) { extension ->
					SettingsDefaults.itemCustom(
						modifier = Modifier
							.clickable {
								onOpenPage(SettingsPages.Extension(extension.id))
							}
							.animateItem(),
						
						icon = extension.icon.let {{
							if(it != null) {
								ExtImage(Modifier.size(40.dp), it)
							} else {
								DefaultExtImage(Modifier.size(40.dp))
							}
						}},
						
						title = {
							Text(extension.name)
						},
						
						description = {
							Column(
								verticalArrangement = Arrangement.spacedBy(2.dp)
							) {
								Text(
									inlineContent = inlineContent,
									text = buildAnnotatedString {
										if(extension.isNsfw) {
											appendInlineContent("nsfw", "NSFW")
											append(" ")
										}

										extension.lang?.also {
											append(it.formatAsLanguage())
											append(" ")
										}

										append(extension.version)
										append(" ")

										if(AwerySettings.showIds.collectAsState().value) {
											append(" ")
											append(extension.id)
										}
										
										if(extension.loadException != null) {
											append(" Failed to load!")
										}
									}
								)
								
								extension.loadException?.also { 
									Text(
										color = MaterialTheme.colorScheme.error,
										text = it.stackTraceToString()
									)
								}
							}
						}
					)
				}
			}
		}
	}
}