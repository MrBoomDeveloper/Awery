package com.mrboomdev.awery.ui.screens.main

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.MainRoutes
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.FlexibleTopAppBar
import com.mrboomdev.awery.ui.screens.home.HomeScreen
import com.mrboomdev.awery.ui.screens.home.HomeViewModel
import com.mrboomdev.awery.ui.screens.intro.IntroStep
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.*
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

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
	val showNavLabels by AwerySettings.showNavigationLabels.collectAsState()
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
                                WindowInsetsSides.Start + WindowInsetsSides.Vertical
                            ).add(WindowInsets(horizontal = 16.dp)),
                            
                            containerColor = NavigationRailDefaults.ContainerColor.let {
                                if(isAmoledTheme()) it.copy(alpha = .75f) else it
                            }
                        ) {
                            Spacer(Modifier.weight(.75f))

                            MainRoutes.entries.forEachIndexed { index, tab ->
                                if(tab.desktopOnly) return@forEachIndexed
                                
                                NavigationRailItem(
                                    selected = index == pagerState.currentPage,
                                    alwaysShowLabel = showNavLabels == AwerySettings.NavigationLabels.SHOW,
                                    
                                    icon = {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            imageVector = vectorResource(tab.getIcon(index == pagerState.currentPage)),
                                            contentDescription = null
                                        )
                                    },

                                    label = if(showNavLabels != AwerySettings.NavigationLabels.HIDE) {{ 
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
                            .thenIf(useRail) { 
                                consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)) }
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
                                        .thenIfElse(useRail, {
                                            padding(horizontal = 16.dp)
                                        }, { padding(start = 16.dp, end = 8.dp) })
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
                                                            .background(
                                                                MaterialTheme.colorScheme.surfaceContainerHighest.let {
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
                                    
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(32.dp))
                                            .clickable { 
                                                coroutineScope.launch { 
                                                    drawerState.open() 
                                                } 
                                            }.thenIf(windowSize.width >= WindowSizeType.Large) {
                                                padding(start = 16.dp)
                                            },
                                        
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if(windowSize.width >= WindowSizeType.Large) {
                                            Text(
                                                modifier = Modifier.padding(end = 2.dp),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontFamily = AweryFonts.poppins,
                                                fontWeight = FontWeight.Normal,
                                                text = AwerySettings.username.collectAsState().value
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
                                        alwaysShowLabel = showNavLabels == AwerySettings.NavigationLabels.SHOW,
                                        
                                        icon = {
                                            Icon(
                                                modifier = Modifier.size(26.dp),
                                                imageVector = vectorResource(tab.getIcon(index == pagerState.currentPage)),
                                                contentDescription = null
                                            )
                                        },
                                        
                                        label = if(showNavLabels != AwerySettings.NavigationLabels.HIDE) {{
											Text(stringResource(tab.title))
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
                                1 -> SearchPage(viewModel, contentPadding, searchQuery.text.toString())
                                2 -> NotificationsPage(contentPadding).let {{}}
                                3 -> LibraryPage(viewModel, contentPadding)
                                else -> HomeScreen(viewModel { HomeViewModel() }, contentPadding).let {{}}
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
                                text = AwerySettings.username.collectAsState().value
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