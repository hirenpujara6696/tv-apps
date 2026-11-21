package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.DomainTestResult
import com.example.ui.ShieldUiState
import com.example.ui.UiStrings
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.TvActionPill
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun DashboardScreen(
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onToggleShield: () -> Unit,
    onToggleTvAds: (Boolean) -> Unit,
    onToggleAdNetworks: (Boolean) -> Unit,
    onToggleStreamingAds: (Boolean) -> Unit,
    onToggleTrackers: (Boolean) -> Unit,
    onTestDomain: (String) -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenSetupGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var testInput by rememberSaveable { mutableStateOf("doubleclick.net") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = if (isWideScreen) 28.dp else 16.dp,
            vertical = if (isWideScreen) 24.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            DashboardTopHeader(
                strings = strings,
                isHindi = state.settings.isHindi,
                isProtected = state.vpn.isRunning,
                onToggleLanguage = onToggleLanguage
            )
        }

        if (state.bannerMessage != null) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.bannerMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item {
            HeroShieldCockpit(
                state = state,
                strings = strings,
                isWideScreen = isWideScreen,
                onToggleShield = onToggleShield
            )
        }

        item {
            MetricsStrip(
                state = state,
                strings = strings,
                isWideScreen = isWideScreen,
                onOpenSetupGuide = onOpenSetupGuide
            )
        }

        item {
            Text(
                text = strings.quickProtections,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            ProtectionLayersGrid(
                state = state,
                strings = strings,
                isWideScreen = isWideScreen,
                onToggleTvAds = onToggleTvAds,
                onToggleAdNetworks = onToggleAdNetworks,
                onToggleStreamingAds = onToggleStreamingAds,
                onToggleTrackers = onToggleTrackers
            )
        }

        item {
            LiveDomainTesterCard(
                strings = strings,
                input = testInput,
                onInputChange = { testInput = it },
                onTest = { onTestDomain(it) },
                testResult = state.testResult
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DashboardTopHeader(
    strings: UiStrings,
    isHindi: Boolean,
    isProtected: Boolean,
    onToggleLanguage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Tv,
                    contentDescription = null,
                    tint = if (isProtected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strings.appSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        TvActionPill(
            label = if (isHindi) "EN" else "हिन्दी",
            icon = Icons.Outlined.Language,
            onClick = onToggleLanguage,
            selected = isHindi,
            accentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.testTag("language_toggle_button")
        )
    }
}

@Composable
private fun HeroShieldCockpit(
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onToggleShield: () -> Unit
) {
    val isProtected = state.vpn.isRunning
    val accentColor by animateColorAsState(
        targetValue = if (isProtected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        label = "shieldAccent"
    )
    val accentContainer by animateColorAsState(
        targetValue = if (isProtected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        label = "shieldContainer"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shieldRadar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    TvFocusableCard(
        onClick = onToggleShield,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        borderColor = accentColor.copy(alpha = 0.45f),
        focusedBorderColor = MaterialTheme.colorScheme.secondary,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("master_shield_toggle")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_shield_hero_1795229382362),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(if (isProtected) 0.55f else 0.28f)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                accentColor.copy(alpha = 0.14f)
                            )
                        )
                    )
            )

            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeroShieldCopy(
                        state = state,
                        strings = strings,
                        accentColor = accentColor,
                        accentContainer = accentContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    ShieldPowerOrb(
                        isProtected = isProtected,
                        accentColor = accentColor,
                        pulseRadius = pulseRadius,
                        pulseAlpha = pulseAlpha,
                        actionLabel = if (isProtected) strings.tapToPause else strings.tapToEnable
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShieldPowerOrb(
                        isProtected = isProtected,
                        accentColor = accentColor,
                        pulseRadius = pulseRadius,
                        pulseAlpha = pulseAlpha,
                        actionLabel = if (isProtected) strings.tapToPause else strings.tapToEnable
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    HeroShieldCopy(
                        state = state,
                        strings = strings,
                        accentColor = accentColor,
                        accentContainer = accentContainer,
                        centered = true
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroShieldCopy(
    state: ShieldUiState,
    strings: UiStrings,
    accentColor: Color,
    accentContainer: Color,
    modifier: Modifier = Modifier,
    centered: Boolean = false
) {
    val isProtected = state.vpn.isRunning
    Column(
        modifier = modifier,
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        StatusPillBadge(
            text = if (isProtected) strings.statusProtected else strings.statusPaused,
            color = accentColor,
            backgroundColor = accentContainer
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isProtected) strings.tapToPause else strings.tapToEnable,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isProtected) strings.shieldOnSubtitle else strings.shieldOffSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Dns,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${state.settings.activeProvider.displayName} (${state.settings.activeProvider.primaryIp})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (isProtected && state.vpn.lastBlockedDomain != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Last blocked: ${state.vpn.lastBlockedDomain}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ShieldPowerOrb(
    isProtected: Boolean,
    accentColor: Color,
    pulseRadius: Float,
    pulseAlpha: Float,
    actionLabel: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(136.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseRadius = size.minDimension / 2f
            if (isProtected) {
                drawCircle(
                    color = accentColor.copy(alpha = pulseAlpha),
                    radius = baseRadius * pulseRadius,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            drawCircle(
                color = accentColor.copy(alpha = 0.22f),
                radius = baseRadius * 0.78f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = if (isProtected) 0.22f else 0.14f),
            border = BorderStroke(2.5.dp, accentColor),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isProtected) Icons.Filled.GppGood else Icons.Filled.PowerSettingsNew,
                    contentDescription = actionLabel,
                    tint = accentColor,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricsStrip(
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onOpenSetupGuide: () -> Unit
) {
    val blockedToday = maxOf(state.blockedToday, state.vpn.sessionBlockedCount)
    val totalBlocked = maxOf(state.totalBlocked, state.vpn.sessionBlockedCount)

    if (isWideScreen) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MetricTile(
                label = strings.adsBlockedToday,
                value = blockedToday.toString(),
                icon = Icons.Filled.Block,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = strings.totalBlocked,
                value = totalBlocked.toString(),
                icon = Icons.Filled.VerifiedUser,
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = strings.activeRules,
                value = "${state.activeRuleCount}+",
                icon = Icons.Filled.Security,
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            TvFocusableCard(
                onClick = onOpenSetupGuide,
                modifier = Modifier
                    .weight(1f)
                    .testTag("dns_metric_card"),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Dns,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.activeDns,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.settings.activeProvider.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = strings.adsBlockedToday,
                    value = blockedToday.toString(),
                    icon = Icons.Filled.Block,
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = strings.totalBlocked,
                    value = totalBlocked.toString(),
                    icon = Icons.Filled.VerifiedUser,
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = strings.activeRules,
                    value = "${state.activeRuleCount}+",
                    icon = Icons.Filled.Security,
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                TvFocusableCard(
                    onClick = onOpenSetupGuide,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dns_metric_card"),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.activeDns,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.settings.activeProvider.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProtectionLayersGrid(
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onToggleTvAds: (Boolean) -> Unit,
    onToggleAdNetworks: (Boolean) -> Unit,
    onToggleStreamingAds: (Boolean) -> Unit,
    onToggleTrackers: (Boolean) -> Unit
) {
    val cards = listOf(
        ProtectionLayerItem(
            tag = "toggle_smart_tv_ads",
            title = strings.packSmartTvTitle,
            description = strings.packSmartTvDesc,
            icon = Icons.Filled.LiveTv,
            checked = state.settings.blockTvAds,
            onToggle = { onToggleTvAds(!state.settings.blockTvAds) }
        ),
        ProtectionLayerItem(
            tag = "toggle_ad_networks",
            title = strings.packAdNetworksTitle,
            description = strings.packAdNetworksDesc,
            icon = Icons.Filled.Block,
            checked = state.settings.blockAdNetworks,
            onToggle = { onToggleAdNetworks(!state.settings.blockAdNetworks) }
        ),
        ProtectionLayerItem(
            tag = "toggle_streaming_ads",
            title = strings.packStreamingTitle,
            description = strings.packStreamingDesc,
            icon = Icons.Filled.PlayCircle,
            checked = state.settings.blockStreamingAds,
            onToggle = { onToggleStreamingAds(!state.settings.blockStreamingAds) }
        ),
        ProtectionLayerItem(
            tag = "toggle_trackers",
            title = strings.packTrackersTitle,
            description = strings.packTrackersDesc,
            icon = Icons.Filled.Security,
            checked = state.settings.blockTrackers,
            onToggle = { onToggleTrackers(!state.settings.blockTrackers) }
        )
    )

    if (isWideScreen) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProtectionToggleCard(cards[0], modifier = Modifier.weight(1f))
                ProtectionToggleCard(cards[1], modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProtectionToggleCard(cards[2], modifier = Modifier.weight(1f))
                ProtectionToggleCard(cards[3], modifier = Modifier.weight(1f))
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cards.forEach { item ->
                ProtectionToggleCard(item, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private data class ProtectionLayerItem(
    val tag: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val checked: Boolean,
    val onToggle: () -> Unit
)

@Composable
private fun ProtectionToggleCard(
    item: ProtectionLayerItem,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = item.onToggle,
        borderColor = if (item.checked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        modifier = modifier.testTag(item.tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (item.checked) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.checked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = item.checked,
                onCheckedChange = { item.onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun LiveDomainTesterCard(
    strings: UiStrings,
    input: String,
    onInputChange: (String) -> Unit,
    onTest: (String) -> Unit,
    testResult: DomainTestResult?
) {
    val quickSamples = listOf(
        "doubleclick.net",
        "ads.sansui.tv",
        "ads.hotstar.com",
        "google.com"
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = strings.domainTesterTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.domainTesterSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = strings.domainInputPlaceholder,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("domain_tester_input")
                )

                Button(
                    onClick = { onTest(input) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier.testTag("domain_tester_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.testButton,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickSamples) { sample ->
                    TvActionPill(
                        label = sample,
                        icon = Icons.Filled.Search,
                        onClick = {
                            onInputChange(sample)
                            onTest(sample)
                        },
                        accentColor = MaterialTheme.colorScheme.secondary,
                        minHeight = 40.dp
                    )
                }
            }

            AnimatedVisibility(visible = testResult != null) {
                testResult?.let { result ->
                    val isBlocked = result.decision.blocked
                    val badgeColor = if (isBlocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                    val bg = if (isBlocked) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = bg,
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = if (isBlocked) Icons.Filled.CheckCircle else Icons.Filled.GppMaybe,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${result.domain} → ${if (isBlocked) strings.resultBlocked else strings.resultAllowed}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = badgeColor
                                )
                                Text(
                                    text = buildString {
                                        append(result.decision.category.key)
                                        result.decision.matchedRule?.let { append(" • matched $it") }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
