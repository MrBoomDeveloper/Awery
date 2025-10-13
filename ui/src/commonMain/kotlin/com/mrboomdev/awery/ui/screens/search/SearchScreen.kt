package com.mrboomdev.awery.ui.screens.search

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.adult_content
import com.mrboomdev.awery.resources.ic_explict_outlined
import com.mrboomdev.awery.resources.ic_search
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.DefaultExtImage
import com.mrboomdev.awery.ui.components.ExtImage
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.utils.formatAsLanguage
import com.mrboomdev.awery.ui.utils.niceSideInset
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.viewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private enum class SearchStatus {
	LOADING,
	LOADED,
	EMPTY
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
	viewModel: SearchViewModel = viewModel { SearchViewModel() },
	contentPadding: PaddingValues
) {
	val navigation = Navigation.current()
	val foundExtensions by viewModel.extensionsFound.collectAsState()
	val isLoadingExtensions by Extensions.observeIsLoading().collectAsState()

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

	Crossfade(when {
		foundExtensions?.isNotEmpty() ?: false -> SearchStatus.LOADED
		!isLoadingExtensions && (foundExtensions?.isEmpty() ?: false) -> SearchStatus.EMPTY
		else -> SearchStatus.LOADING
	}) { status ->
		when(status) {
			SearchStatus.LOADING -> {
				LoadingIndicator(
					modifier = Modifier
						.fillMaxSize()
						.wrapContentSize(Alignment.Center)
				)
			}
			
			SearchStatus.EMPTY -> {
				InfoBox(
					modifier = Modifier
						.fillMaxSize()
						.wrapContentSize(Alignment.Center),
					icon = painterResource(Res.drawable.ic_search),
					title = "Nothing found",
					message = "No results were found matching your query. Check if you've entered everything correctly."
				)
			}
			
			SearchStatus.LOADED -> {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = contentPadding
				) {
					singleItem("scrollFix")

					items(
						items = foundExtensions ?: emptyList(),
						key = { "ext_${it.id}"},
						contentType = { "extension" }
					) { extension ->
						val subtitle = remember(extension) {
							buildAnnotatedString {
								if(extension.isNsfw) {
									appendInlineContent("nsfw")
									append(" NSFW ")
								}
								
								append(listOfNotNull(
									extension.lang?.formatAsLanguage(),
									extension.id.takeIf { AwerySettings.showIds.value }
								).also {
									if(it.isNotEmpty() && extension.isNsfw) {
										append(" • ")
									}
								}.joinToString(" • "))
							}.takeIf { it.text.isNotBlank() }
						}
						
						Row(
							modifier = Modifier
								.clickable { navigation.push(Routes.Extension(extension.id, extension.name)) }
								.fillMaxWidth()
								.padding(vertical = 8.dp, horizontal = niceSideInset())
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

					if(isLoadingExtensions) {
						singleItem("loading") {
							LoadingIndicator(
								modifier = Modifier
									.fillMaxWidth()
									.padding(64.dp)
									.wrapContentSize(Alignment.Center)
									.animateItem()
							)
						}
					}
				}
			}
		}
	}
}