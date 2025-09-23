package com.mrboomdev.awery.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.AweryFonts
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.extensions
import com.mrboomdev.awery.resources.home
import com.mrboomdev.awery.resources.ic_account_outlined
import com.mrboomdev.awery.resources.ic_bookmarks_outlined
import com.mrboomdev.awery.resources.ic_close
import com.mrboomdev.awery.resources.ic_collections_bookmark_filled
import com.mrboomdev.awery.resources.ic_collections_bookmark_outlined
import com.mrboomdev.awery.resources.ic_edit_outlined
import com.mrboomdev.awery.resources.ic_extension_outlined
import com.mrboomdev.awery.resources.ic_home_filled
import com.mrboomdev.awery.resources.ic_home_outlined
import com.mrboomdev.awery.resources.ic_notifications_filled
import com.mrboomdev.awery.resources.ic_notifications_outlined
import com.mrboomdev.awery.resources.ic_search
import com.mrboomdev.awery.resources.ic_settings_outlined
import com.mrboomdev.awery.resources.library
import com.mrboomdev.awery.resources.logo_awery
import com.mrboomdev.awery.resources.notifications
import com.mrboomdev.awery.resources.search
import com.mrboomdev.awery.resources.settings
import com.mrboomdev.awery.ui.MainRoutes
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.FlexibleTopAppBar
import com.mrboomdev.awery.ui.screens.intro.IntroDefaults.navigation
import com.mrboomdev.awery.ui.screens.intro.IntroStep
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.WindowInsets
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.bottom
import com.mrboomdev.awery.ui.utils.collapse
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.awery.ui.utils.expand
import com.mrboomdev.awery.ui.utils.none
import com.mrboomdev.awery.ui.utils.right
import com.mrboomdev.awery.ui.utils.thenIf
import com.mrboomdev.awery.ui.utils.thenIfElse
import com.mrboomdev.awery.ui.utils.viewModel
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import java.util.WeakHashMap

@Composable
expect fun MainScreen(
    viewModel: MainScreenViewModel = viewModel(::MainScreenViewModel)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DefaultMainScreen(
    viewModel: MainScreenViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val searchQuery = rememberTextFieldState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    
    var onTabReClick by remember { 
        mutableStateOf({})
    }

    val pagerState = rememberPagerState(MainRoutes.entries.indexOf(
        when(AwerySettings.defaultMainTab.value) {
            AwerySettings.MainTab.HOME -> MainRoutes.HOME
            AwerySettings.MainTab.SEARCH -> MainRoutes.SEARCH
            AwerySettings.MainTab.NOTIFICATIONS -> MainRoutes.NOTIFICATIONS
            AwerySettings.MainTab.LIBRARY -> MainRoutes.LIBRARY
        }
    )) { MainRoutes.entries.count() }

    val focusManager = LocalFocusManager.current
    val windowSize = currentWindowSize()
    val useRail = windowSize.width >= WindowSizeType.Large

    val topBarBehavior = if(useRail && windowSize.height >= WindowSizeType.Medium) {
        TopAppBarDefaults.pinnedScrollBehavior()
    } else TopAppBarDefaults.enterAlwaysScrollBehavior()

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        ModalNavigationDrawer(
            modifier = Modifier.fillMaxSize(),
            drawerState = drawerState,
            drawerContent = { DrawerContent(drawerState, coroutineScope) }
        ) {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Ltr
            ) {
                Row(Modifier.fillMaxSize()) {
                    if(useRail) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            windowInsets = WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Start + WindowInsetsSides.Vertical).add(WindowInsets(horizontal = 16.dp)),
                            
                            containerColor = NavigationRailDefaults.ContainerColor.let {
                                if(isAmoledTheme()) it.copy(alpha = .75f) else it
                            }
                        ) {
                            Spacer(Modifier.weight(.75f))

                            MainRoutes.entries.forEachIndexed { index, tab ->
                                if(tab.desktopOnly) return@forEachIndexed
                                
                                NavigationRailItem(
                                    selected = index == pagerState.currentPage,
                                    
                                    alwaysShowLabel = AwerySettings.showNavigationLabels.state.value == 
                                            AwerySettings.NavigationLabels.SHOW,
                                    
                                    icon = {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            imageVector = vectorResource(tab.getIcon(index == pagerState.currentPage)),
                                            contentDescription = null
                                        )
                                    },

                                    label = if(AwerySettings.showNavigationLabels.state.value !=
                                        AwerySettings.NavigationLabels.HIDE
                                    ) {{ 
                                        Text(
                                            text = stringResource(tab.title),
                                            fontSize = 11.sp
                                        ) 
                                    }} else null,
                                    
                                    onClick = { 
                                        coroutineScope.launch { 
                                            if(pagerState.currentPage == index) {
                                                onTabReClick()
                                            }
                                            
                                            pagerState.scrollToPage(index)
                                        } 
                                        
                                        focusManager.clearFocus(force = true)
                                    }
                                )
                            }

                            Spacer(Modifier.weight(1f))
                        }
                    }

                    Scaffold(
                        modifier = Modifier
                            .weight(1f)
                            .thenIf(useRail) { consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)) }
                            .nestedScroll(topBarBehavior.nestedScrollConnection),
                        
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        
                        topBar = {
                            FlexibleTopAppBar(
                                scrollBehavior = topBarBehavior,
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    scrolledContainerColor = if(pagerState.currentPage != 1 || topBarBehavior.isPinned) {
                                        MaterialTheme.colorScheme.background.let {
                                            if(isAmoledTheme()) it.copy(alpha = .9f) else it
                                        }
                                    } else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .thenIfElse(useRail, { padding(horizontal = 16.dp) }, { padding(start = 16.dp, end = 8.dp) })
                                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top +
                                                if(useRail) WindowInsetsSides.End else WindowInsetsSides.Horizontal)),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if(pagerState.currentPage != 1 || windowSize.width >= WindowSizeType.Large) {
                                        Image(
                                            modifier = Modifier.size(36.dp),
                                            painter = painterResource(Res.drawable.logo_awery),
                                            contentDescription = null
                                        )

                                        Text(
                                            style = MaterialTheme.typography.titleLarge,
                                            text = "Awery"
                                        )

                                        Spacer(Modifier.width(24.dp))
                                    }

                                    Box(Modifier.weight(1f)) {
                                        if(!(pagerState.currentPage == 1 || windowSize.width >= WindowSizeType.Large)) {
                                            // We need to simulate searchbar height so that layout won't jump
                                            Box(Modifier.padding(vertical = 8.dp)) {
                                                Spacer(Modifier.height(24.dp))
                                                Text(
                                                    modifier = Modifier.padding(vertical = 12.dp),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = Color.Transparent,
                                                    text = "A"
                                                )
                                            }
                                        } else {
                                            val focusRequester = remember { FocusRequester() }

                                            BasicTextField(
                                                state = searchQuery,
                                                modifier = Modifier
                                                    .focusRequester(focusRequester)
                                                    .widthIn(max = 450.dp)
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),

                                                keyboardOptions = KeyboardOptions(
                                                    imeAction = ImeAction.Search
                                                ),

                                                onKeyboardAction = {
                                                    focusManager.clearFocus(force = true)
                                                },

                                                textStyle = MaterialTheme.typography.bodyLarge
                                                    .copy(color = MaterialTheme.colorScheme.onSurface),

                                                lineLimits = TextFieldLineLimits.SingleLine,
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                decorator = { innerTextField ->
                                                    val query = searchQuery.takeIf { 
                                                        pagerState.currentPage == 1
                                                    }?.text?.toString()?.takeIf { 
                                                        it.isNotEmpty()
                                                    }
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(32.dp))
                                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.let {
                                                                if(isAmoledTheme()) it.copy(alpha = .5f) else it
                                                            })
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 16.dp, end = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                modifier = Modifier.size(24.dp),
                                                                imageVector = vectorResource(Res.drawable.ic_search),
                                                                contentDescription = null
                                                            )

                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                                                            ) {
                                                                if(query == null) {
                                                                    Text(
                                                                        style = MaterialTheme.typography.bodyLarge,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                        text = "Search anything"
                                                                    )
                                                                }
                                                                
                                                                innerTextField()
                                                            }

                                                            if(query != null) {
                                                                IconButton(onClick = { searchQuery.clearText() }) {
                                                                    Icon(
                                                                        modifier = Modifier.size(24.dp),
                                                                        imageVector = vectorResource(Res.drawable.ic_close),
                                                                        contentDescription = null
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        
                                                        if(pagerState.currentPage != 1) {
                                                            Spacer(
                                                                modifier = Modifier
                                                                    .matchParentSize()
                                                                    .clickable {
                                                                        coroutineScope.launch {
                                                                            pagerState.scrollToPage(1)
                                                                            focusRequester.requestFocus()
                                                                        }
                                                                    }
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    
                                    if(windowSize.width >= WindowSizeType.Large) {
                                        Text(
                                            modifier = Modifier.padding(end = 2.dp),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontFamily = AweryFonts.poppins,
                                            fontWeight = FontWeight.Normal,
                                            text = AwerySettings.username.state.value
                                        )
                                    }

                                    FilledIconButton(
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        
                                        onClick = {
                                            coroutineScope.launch { 
                                                drawerState.open() 
                                            } 
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.padding(6.dp),
                                            painter = painterResource(Res.drawable.ic_account_outlined),
                                            contentDescription = null
                                        )

                                        AsyncImage(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .fillMaxSize(),

                                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                                .addLastModifiedToFileCacheKey(true)
                                                .data(FileKit.filesDir / "avatar.png")
                                                .build(),

                                            contentDescription = null,
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        },

                        bottomBar = {
                            if(useRail) return@Scaffold

                            FlexibleBottomAppBar(
                                containerColor = BottomAppBarDefaults.containerColor.let {
                                    if(isAmoledTheme()) it.copy(alpha = .9f) else it
                                }
                            ) {
                                MainRoutes.entries.forEachIndexed { index, tab ->
                                    if(tab.desktopOnly) return@forEachIndexed
                                    
                                    NavigationBarItem(
                                        selected = index == pagerState.currentPage,

                                        alwaysShowLabel = AwerySettings.showNavigationLabels.state.value ==
                                                AwerySettings.NavigationLabels.SHOW,
                                        
                                        icon = {
                                            Icon(
                                                modifier = Modifier.size(26.dp),
                                                imageVector = vectorResource(tab.getIcon(index == pagerState.currentPage)),
                                                contentDescription = null
                                            )
                                        },
                                        
                                        label = if(AwerySettings.showNavigationLabels.state.value != 
                                            AwerySettings.NavigationLabels.HIDE
                                        ) {{ Text(stringResource(tab.title)) }} else null,
                                        
                                        onClick = {
                                            coroutineScope.launch {
                                                if(pagerState.currentPage == index) {
                                                    onTabReClick()
                                                }

                                                pagerState.scrollToPage(index)
                                            }

                                            focusManager.clearFocus(force = true)
                                        }
                                    )
                                }
                            }
                        }
                    ) { contentPadding ->
                        // Hacky hack to hack the stuff
                        val contentPadding = contentPadding.add(bottom = WindowInsets.navigationBars.bottom)
                        
                        val fuck = remember {
                            mutableListOf({}, {}, {}, {})
                        }
                        
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = false,
                            key = { it }
                        ) { page ->
                            fuck[page] = when(page) {
                                0 -> HomePage(viewModel, contentPadding)
                                1 -> SearchPage(viewModel, contentPadding, searchQuery.text.toString())
                                2 -> NotificationsPage(contentPadding).let {{}}
                                3 -> LibraryPage(viewModel, contentPadding)
                                else -> throw IllegalStateException()
                            }
                        }

                        LaunchedEffect(pagerState.currentPage) {
                            onTabReClick = {
                                fuck[pagerState.currentPage]()
                                
                                coroutineScope.launch { 
                                    topBarBehavior.expand()
                                    topBarBehavior.state.heightOffset = 0f
                                    topBarBehavior.state.contentOffset = 0f
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    drawerState: DrawerState,
    coroutineScope: CoroutineScope
) {
    val navigation = Navigation.current()
    
    ModalDrawerSheet(
        drawerState = drawerState,
        windowInsets = WindowInsets.none
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Ltr
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(275.dp)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Bottom
                    )).padding(bottom = 12.dp)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    AsyncImage(
                        modifier = Modifier
                            .matchParentSize(),

                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .addLastModifiedToFileCacheKey(true)
                            .data(FileKit.filesDir / "wallpaper.png")
                            .build(),

                        alpha = .5f,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.End
                            ))
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),

                                    painter = painterResource(Res.drawable.ic_account_outlined),
                                    contentDescription = null
                                )

                                AsyncImage(
                                    modifier = Modifier.fillMaxSize(),

                                    model = ImageRequest.Builder(LocalPlatformContext.current)
                                        .addLastModifiedToFileCacheKey(true)
                                        .data(FileKit.filesDir / "avatar.png")
                                        .build(),

                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                text = AwerySettings.username.state.value
                            )
                        }

                        com.mrboomdev.awery.ui.components.IconButton(
                            modifier = Modifier
                                .offset(9.dp, (-9).dp)
                                .size(40.dp),
                            padding = 9.dp,
                            painter = painterResource(Res.drawable.ic_edit_outlined),
                            contentDescription = null,
                            onClick = {
                                navigation.push(Routes.Intro(
                                    step = IntroStep.UserCreation,
                                    singleStep = true
                                ))
                            }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(end = WindowInsets.safeDrawing.right)
                        .padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )

                SettingsDefaults.itemClickable(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    icon = painterResource(Res.drawable.ic_bookmarks_outlined),
                    title = "Lists",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navigation.push(Routes.Settings(SettingsPages.Lists))
                    }
                )

                SettingsDefaults.itemClickable(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    icon = painterResource(Res.drawable.ic_extension_outlined),
                    title = stringResource(Res.string.extensions),
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navigation.push(Routes.Settings(SettingsPages.Extensions))
                    }
                )

                //                            Spacer(Modifier.weight(1f))

                SettingsDefaults.itemClickable(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    icon = painterResource(Res.drawable.ic_settings_outlined),
                    title = stringResource(Res.string.settings),
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navigation.push(Routes.Settings())
                    }
                )
            }
        }
    }
}