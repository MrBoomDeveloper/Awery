package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State.Empty.painter
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.AweryContributors
import com.mrboomdev.awery.data.AwerySocials
import com.mrboomdev.awery.data.appVersion
import com.mrboomdev.awery.data.sdkVersion
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.about
import com.mrboomdev.awery.resources.logo_awery
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.awery.ui.utils.singleItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsAboutPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.about)) }
	) { contentPadding ->
		val contributors by AweryContributors.getAll().collectAsState(emptyList())
		val windowSize = currentWindowSize()
		
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding.add(bottom = 16.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) { 
			singleItem("header") {
				Column(
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.padding(top = when {
							windowSize.height >= WindowSizeType.Large -> 32.dp
							windowSize.height >= WindowSizeType.Medium -> 16.dp
							else -> 8.dp
						}),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
					) {
						Image(
							modifier = Modifier.size(64.dp),
							painter = painterResource(Res.drawable.logo_awery),
							contentDescription = null
						)
						
						Text(
							style = MaterialTheme.typography.displayLarge,
							color = MaterialTheme.colorScheme.onBackground,
							text = "Awery"
						)
					}
					
					Text(
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						text = "App version: ${Awery.appVersion}  SDK version: ${Awery.sdkVersion}"
					)
				}
			}
			
			singleItem("socialsHeader") {
				Text(
					modifier = Modifier.padding(top = 32.dp),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onBackground,
					text = "Follow us"
				)
			}
			
			singleItem("socials") { 
				FlowRow(
					modifier = Modifier.padding(horizontal = 16.dp),
					horizontalArrangement = Arrangement.Center,
					verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
				) {
					for(social in AwerySocials) {
						IconButton(
							modifier = Modifier.size(64.dp),
							padding = 16.dp,
							painter = painterResource(social.logo),
							contentDescription = null,
							onClick = { Awery.openUrl(social.url) },
							colors = IconButtonDefaults.iconButtonColors(
								contentColor = MaterialTheme.colorScheme.primary
							)
						)
					}
				}
			}
			
			singleItem("contributorsHeader") { 
				Text(
					modifier = Modifier
						.padding(top = 16.dp)
						.padding(horizontal = 12.dp),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onBackground,
					text = "Contributors"
				)
			}
			
			items(
				items = contributors,
				key = { it.url }
			) { contributor ->
				Surface(
					modifier = Modifier
						.clip(RoundedCornerShape(16.dp))
						.fillMaxWidth(),
					onClick = { Awery.openUrl(contributor.url) },
					color = Color.Transparent 
				) {
					Row(
						modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
						horizontalArrangement = Arrangement.spacedBy(16.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						AsyncImage(
							modifier = Modifier
								.clip(CircleShape)
								.size(48.dp),
							model = contributor.avatar,
							contentDescription = null
						)

						Column(
							verticalArrangement = Arrangement.spacedBy(3.dp)
						) { 
							Text(
								style = MaterialTheme.typography.titleMedium,
								text = contributor.name
							)
							
							contributor.role?.also { role ->
								Text(
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									text = role
								)
							}
						}
					}
				}
			}
		}
	}
}