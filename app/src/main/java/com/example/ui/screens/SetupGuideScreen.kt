package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.ShieldUiState
import com.example.ui.UiStrings
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.TvActionPill
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.vpn.UpstreamDnsProvider

@Composable
fun SetupGuideScreen(
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onSelectDns: (String) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onToggleStrictBlock: (Boolean) -> Unit,
    onToggleHindi: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = if (isWideScreen) 28.dp else 16.dp,
            vertical = if (isWideScreen) 24.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = strings.setupHeader,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.setupSubheader,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sansui Google TV Checklist Card
        item {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Tv,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.sansuiGuideTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.sansuiGuideSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    GuideStepRow(strings.guideStep1Title, strings.guideStep1Body)
                    GuideStepRow(strings.guideStep2Title, strings.guideStep2Body)
                    GuideStepRow(strings.guideStep3Title, strings.guideStep3Body)
                    GuideStepRow(strings.guideStep4Title, strings.guideStep4Body)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TvActionPill(
                            label = strings.openTvSettings,
                            icon = Icons.Filled.Settings,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            },
                            selected = true,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.testTag("open_tv_settings_button")
                        )
                        TvActionPill(
                            label = strings.openWifiSettings,
                            icon = Icons.Filled.Wifi,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            },
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("open_wifi_settings_button")
                        )
                    }
                }
            }
        }

        // Upstream DNS Picker
        item {
            Column {
                Text(
                    text = strings.upstreamDnsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.upstreamDnsDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        UpstreamDnsProvider.entries.forEach { provider ->
            item(key = provider.id) {
                val isSelected = state.settings.upstreamDnsId == provider.id
                TvFocusableCard(
                    onClick = { onSelectDns(provider.id) },
                    borderColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dns_provider_${provider.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = provider.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusPillBadge(
                                    text = provider.badge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (state.settings.isHindi) provider.descriptionHi else provider.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = provider.primaryIp,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        // TV Shield Behavior Settings
        item {
            Text(
                text = strings.tvBehaviorTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            SettingsToggleRow(
                title = strings.autoStartTitle,
                description = strings.autoStartDesc,
                icon = Icons.Filled.PowerSettingsNew,
                checked = state.settings.autoStartOnBoot,
                onToggle = { onToggleAutoStart(!state.settings.autoStartOnBoot) },
                testTag = "toggle_auto_start_boot"
            )
        }

        item {
            SettingsToggleRow(
                title = strings.strictBlockTitle,
                description = strings.strictBlockDesc,
                icon = Icons.Filled.Bolt,
                checked = state.settings.strictZeroIpBlock,
                onToggle = { onToggleStrictBlock(!state.settings.strictZeroIpBlock) },
                testTag = "toggle_strict_zero_ip"
            )
        }

        item {
            SettingsToggleRow(
                title = strings.languageTitle,
                description = strings.languageDesc,
                icon = Icons.Filled.Language,
                checked = state.settings.isHindi,
                onToggle = { onToggleHindi(!state.settings.isHindi) },
                testTag = "toggle_hindi_language"
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun GuideStepRow(title: String, body: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    TvFocusableCard(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
