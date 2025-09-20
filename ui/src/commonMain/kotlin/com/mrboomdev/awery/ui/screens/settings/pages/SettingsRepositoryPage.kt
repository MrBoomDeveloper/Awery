package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.data.database.entity.DBRepository
import com.mrboomdev.awery.data.repo.Repositories
import com.mrboomdev.awery.data.repo.Repository
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.ExtensionInstaller
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.adult_content
import com.mrboomdev.awery.resources.ic_download
import com.mrboomdev.awery.resources.ic_explict_outlined
import com.mrboomdev.awery.ui.components.DefaultExtImage
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.utils.classify
import com.mrboomdev.awery.ui.utils.formatAsLanguage
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.viewModel
import io.github.z4kn4fein.semver.Version
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class SettingsRepositoryPageViewModel(private val repository: DBRepository): ViewModel() {
	private val _isReloading = mutableStateOf(false)
	val isReloading by _isReloading
	
	private val _exception = mutableStateOf<Throwable?>(null)
	val exception by _exception
	
	private val _extensions = mutableStateListOf<Pair<Repository.Item, Version>>()
	val extensions: List<Pair<Repository.Item, Version>> = _extensions

	val installed = Extensions.observeAll().map { installed ->
		installed.map { extension ->
			extension to Version.parse(extension.version, false)
		}
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = emptyList()
	)
	
	init {
		load()
	}
	
	fun load() {
		viewModelScope.launchTrying(onCatch = {
			_exception.value = it
			_isReloading.value = false
		}) {
			val result = Repositories.fetch(repository.url).items.sortedBy { it.name }
			_extensions.clear()

			_extensions.addAll(result.map {
				it to Version.parse(it.version, false)
			})

			_exception.value = null
			_isReloading.value = false
		}
	}
	
	fun reload() {
		_isReloading.value = true
		load()
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRepositoryPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?,
	onOpenPage: (SettingsPages) -> Unit,
	repository: DBRepository,
	viewModel: SettingsRepositoryPageViewModel = viewModel { SettingsRepositoryPageViewModel(repository) }
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(repository.name) }
	) { contentPadding ->
		val toaster = LocalToaster.current
		val installed by viewModel.installed.collectAsState()
		val installing by ExtensionInstaller.observeInstalling().collectAsState()

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

		PullToRefreshBox(
			modifier = Modifier
				.fillMaxSize()
				.padding(contentPadding),
			isRefreshing = viewModel.isReloading,
			onRefresh = { viewModel.reload() }
		) {
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(bottom = 16.dp),
				verticalArrangement = if(viewModel.extensions.isEmpty()) {
					Arrangement.Center
				} else Arrangement.Top
			) {
				singleItem("scrollFixer")
				
				if(viewModel.extensions.isEmpty()) {
					if(viewModel.exception == null) {
						singleItem("loadingIndicator") {
							CircularProgressIndicator(
								modifier = Modifier
									.fillMaxSize()
									.wrapContentSize()
									.animateItem()
							)
						}
					} else {
						singleItem("error") {
							InfoBox(
								modifier = Modifier
									.fillMaxSize()
									.wrapContentSize()
									.animateItem(),
								contentPadding = PaddingValues(horizontal = 64.dp, vertical = 16.dp),
								throwable = viewModel.exception!!
							)
						}
					}
				}
				
				items(
					items = viewModel.extensions,
					key = { it.first.id }
				) { (extension, version) ->
					val installed = installed.firstOrNull { it.first.id == extension.id }
					val isInstalled = installed != null
					val isInstalling = installing.any { it == extension.url }
					
					SettingsDefaults.itemCustom(
						modifier = Modifier
							.clickable(enabled = !isInstalling || isInstalled) {
								if(isInstalled) {
									onOpenPage(SettingsPages.Extension(extension.id))
									return@clickable
								}
								
								launchGlobal { 
									try {
										ExtensionInstaller.install("yomi", extension.url)
										toaster.toast("Successfully installed \"${extension.name}\"!")
									} catch(_: CancellationException) {
									} catch(e: Exception) {
										Log.e("SettingsRepositoryPage", "Failed to fetch an repository!", e)
										
										toaster.toast(
											title = "Failed to install \"${extension.name}\"!",
											message = e.classify().message
										)
									}
								}
							}.animateItem(),

						icon = extension.icon.let {{
							if(it != null) {
								AsyncImage(
									modifier = Modifier.size(40.dp),
									model = it,
									contentDescription = null,
									contentScale = ContentScale.Fit
								)
							} else {
								DefaultExtImage(Modifier.size(40.dp))
							}
						}},

						title = {
							Text(extension.name)
						},

						description = {
							Text(
								inlineContent = inlineContent,
								text = buildAnnotatedString {
									if(extension.isNsfw) {
										appendInlineContent("nsfw", "NSFW")
										append(" ")
									}

									extension.lang.also {
										append(it.formatAsLanguage())
										append(" ")
									}

									if(isInstalled && version > installed.second) {
										append(installed.first.version)
										append(" > ")
									}
									
									append(extension.version)
									append(" ")

									if(isInstalled) {
										if(version > installed.second) {
											append("Update available ")
										} else {
											append("Installed ")
										}
									}

									if(AwerySettings.showIds.state.value) {
										append(extension.id)
									}
								}
							)
						}
					) {
						Box(
							modifier = Modifier.width(48.dp),
							contentAlignment = Alignment.Center
						) {
							when {
								isInstalling -> {
									CircularProgressIndicator(
										modifier = Modifier.size(24.dp)
									)
								}

								!isInstalled || version > installed.second -> {
									Icon(
										modifier = Modifier.size(24.dp),
										painter = painterResource(Res.drawable.ic_download),
										contentDescription = null
									)
								}
							}
						}
					}
				}
			}
		}
	}
}