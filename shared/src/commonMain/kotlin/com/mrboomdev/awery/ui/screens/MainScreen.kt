package com.mrboomdev.awery.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.mrboomdev.awery.data.DEFAULT_APP_TABS_GROUP
import com.mrboomdev.awery.ui.utils.isAtLeast
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	modifier: Modifier = Modifier
) {
	var currentTab by rememberSaveable { mutableIntStateOf(0) }
	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	
	val tabs = DEFAULT_APP_TABS_GROUP
	
	Row(
		modifier = modifier
	) { 
		if(windowSizeClass.windowWidthSizeClass.isAtLeast(WindowWidthSizeClass.MEDIUM)) {
			NavigationRail { 
				tabs.forEachIndexed { index, tab ->
					NavigationRailItem(
						selected = index == currentTab,
						onClick = { currentTab = index },
						
						icon = {
							Icon(
								modifier = Modifier
									.width(32.dp),
								painter = painterResource(if(index == currentTab) {
									tab.tab.activeIcon
								} else tab.tab.inActiveIcon ?: tab.tab.activeIcon),
								contentDescription = null
							)
						},
						
						label = {
							Text(stringResource(tab.tab.title))
						}
					)
				}
			}
		}
		
		Scaffold(
			topBar = {
				TopAppBar(
					title = {
						Text("Awery")
					}
				)
			},
			
			bottomBar = {
				if(windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
					NavigationBar { 
						tabs.forEachIndexed { index, tab ->
							NavigationBarItem(
								selected = index == currentTab,
								onClick = { currentTab = index },
								
								icon = {
									Icon(
										modifier = Modifier
											.width(32.dp),
										painter = painterResource(if(index == currentTab) {
											tab.tab.activeIcon
										} else tab.tab.inActiveIcon ?: tab.tab.activeIcon),
										contentDescription = null
									)
								},
								
								label = {
									Text(stringResource(tab.tab.title))
								}
							)
						}
					}
				}
			}
		) {
			if(tabs.isEmpty()) {
				Text("There's nothing.")
				return@Scaffold
			}
			
			tabs[currentTab].content()
		}
	}
}