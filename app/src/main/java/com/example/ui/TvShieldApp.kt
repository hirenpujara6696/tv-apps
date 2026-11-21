package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.ActivityLogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FiltersScreen
import com.example.ui.screens.SetupGuideScreen

enum class ShieldTab(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    SHIELD("shield_dashboard", Icons.Filled.Shield, Icons.Outlined.Shield),
    FILTERS("shield_filters", Icons.Filled.Tune, Icons.Outlined.Tune),
    ACTIVITY("shield_activity", Icons.Filled.History, Icons.Outlined.History),
    SETUP("shield_setup", Icons.Filled.Tv, Icons.Outlined.Tv)
}

@Composable
fun TvShieldApp(
    state: ShieldUiState,
    onToggleShield: () -> Unit,
    onSelectDns: (String) -> Unit,
    onToggleTvAds: (Boolean) -> Unit,
    onToggleAdNetworks: (Boolean) -> Unit,
    onToggleStreamingAds: (Boolean) -> Unit,
    onToggleTrackers: (Boolean) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onToggleStrictBlock: (Boolean) -> Unit,
    onToggleLanguage: () -> Unit,
    onSetHindi: (Boolean) -> Unit,
    onAddRule: (domain: String, isAllowlist: Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onClearLogs: () -> Unit,
    onTestDomain: (String) -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(ShieldTab.SHIELD) }
    val strings = BilingualStrings.forLanguage(state.settings.isHindi)

    // TV remote Back button returns to Home Shield tab from secondary tabs
    BackHandler(enabled = currentTab != ShieldTab.SHIELD) {
        currentTab = ShieldTab.SHIELD
    }

    fun labelFor(tab: ShieldTab): String = when (tab) {
        ShieldTab.SHIELD -> strings.navShield
        ShieldTab.FILTERS -> strings.navFilters
        ShieldTab.ACTIVITY -> strings.navActivity
        ShieldTab.SETUP -> strings.navSetup
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // Android TV / Google TV / Tablet Landscape Layout with Left NavigationRail
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (state.vpn.isRunning) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                border = BorderStroke(
                                    1.5.dp,
                                    if (state.vpn.isRunning) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = strings.appTitle,
                                        tint = if (state.vpn.isRunning) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(96.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ShieldTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = labelFor(tab)
                                )
                            },
                            label = {
                                Text(
                                    text = labelFor(tab),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .testTag("nav_rail_${tab.route}")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    TabContent(
                        currentTab = currentTab,
                        state = state,
                        strings = strings,
                        isWideScreen = true,
                        onToggleShield = onToggleShield,
                        onSelectDns = onSelectDns,
                        onToggleTvAds = onToggleTvAds,
                        onToggleAdNetworks = onToggleAdNetworks,
                        onToggleStreamingAds = onToggleStreamingAds,
                        onToggleTrackers = onToggleTrackers,
                        onToggleAutoStart = onToggleAutoStart,
                        onToggleStrictBlock = onToggleStrictBlock,
                        onToggleLanguage = onToggleLanguage,
                        onSetHindi = onSetHindi,
                        onAddRule = onAddRule,
                        onDeleteRule = onDeleteRule,
                        onClearLogs = onClearLogs,
                        onTestDomain = onTestDomain,
                        onOpenSetupGuide = { currentTab = ShieldTab.SETUP }
                    )
                }
            }
        } else {
            // Compact Phone / Portrait Layout with Bottom NavigationBar
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        ShieldTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = labelFor(tab)
                                    )
                                },
                                label = {
                                    Text(
                                        text = labelFor(tab),
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_bar_${tab.route}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabContent(
                        currentTab = currentTab,
                        state = state,
                        strings = strings,
                        isWideScreen = false,
                        onToggleShield = onToggleShield,
                        onSelectDns = onSelectDns,
                        onToggleTvAds = onToggleTvAds,
                        onToggleAdNetworks = onToggleAdNetworks,
                        onToggleStreamingAds = onToggleStreamingAds,
                        onToggleTrackers = onToggleTrackers,
                        onToggleAutoStart = onToggleAutoStart,
                        onToggleStrictBlock = onToggleStrictBlock,
                        onToggleLanguage = onToggleLanguage,
                        onSetHindi = onSetHindi,
                        onAddRule = onAddRule,
                        onDeleteRule = onDeleteRule,
                        onClearLogs = onClearLogs,
                        onTestDomain = onTestDomain,
                        onOpenSetupGuide = { currentTab = ShieldTab.SETUP }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabContent(
    currentTab: ShieldTab,
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onToggleShield: () -> Unit,
    onSelectDns: (String) -> Unit,
    onToggleTvAds: (Boolean) -> Unit,
    onToggleAdNetworks: (Boolean) -> Unit,
    onToggleStreamingAds: (Boolean) -> Unit,
    onToggleTrackers: (Boolean) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onToggleStrictBlock: (Boolean) -> Unit,
    onToggleLanguage: () -> Unit,
    onSetHindi: (Boolean) -> Unit,
    onAddRule: (domain: String, isAllowlist: Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onClearLogs: () -> Unit,
    onTestDomain: (String) -> Unit,
    onOpenSetupGuide: () -> Unit
) {
    Crossfade(targetState = currentTab, label = "tabCrossfade") { tab ->
        when (tab) {
            ShieldTab.SHIELD -> DashboardScreen(
                state = state,
                strings = strings,
                isWideScreen = isWideScreen,
                onToggleShield = onToggleShield,
                onToggleTvAds = onToggleTvAds,
                onToggleAdNetworks = onToggleAdNetworks,
                onToggleStreamingAds = onToggleStreamingAds,
                onToggleTrackers = onToggleTrackers,
                onTestDomain = onTestDomain,
                onToggleLanguage = onToggleLanguage,
                onOpenSetupGuide = onOpenSetupGuide
            )

            ShieldTab.FILTERS -> FiltersScreen(
                state = state,
                strings = strings,
                isWideScreen = isWideScreen,
                onToggleTvAds = onToggleTvAds,
                onToggleAdNetworks = onToggleAdNetworks,
                onToggleStreamingAds = onToggleStreamingAds,
                onToggleTrackers = onToggleTrackers,
                onAddRule = onAddRule,
                onDeleteRule = onDeleteRule
            )

            ShieldTab.ACTIVITY -> ActivityLogScreen(
                state = state,
                strings = strings,
                isWideScreen = isWideScreen,
                onQuickAddRule = onAddRule,
                onClearLogs = onClearLogs
            )

            ShieldTab.SETUP -> SetupGuideScreen(
                state = state,
                strings = strings,
                isWideScreen = isWideScreen,
                onSelectDns = onSelectDns,
                onToggleAutoStart = onToggleAutoStart,
                onToggleStrictBlock = onToggleStrictBlock,
                onToggleHindi = onSetHindi
            )
        }
    }
}
