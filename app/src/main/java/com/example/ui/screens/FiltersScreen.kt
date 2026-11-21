package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CustomDomainRule
import com.example.ui.ShieldUiState
import com.example.ui.UiStrings
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.TvActionPill
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.vpn.DomainFilterEngine

@Composable
fun FiltersScreen(
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onToggleTvAds: (Boolean) -> Unit,
    onToggleAdNetworks: (Boolean) -> Unit,
    onToggleStreamingAds: (Boolean) -> Unit,
    onToggleTrackers: (Boolean) -> Unit,
    onAddRule: (domain: String, isAllowlist: Boolean) -> Unit,
    onDeleteRule: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = if (isWideScreen) 28.dp else 16.dp,
            vertical = if (isWideScreen) 24.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = strings.filtersHeader,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.filtersSubheader,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            FilterPackRow(
                title = strings.packSmartTvTitle,
                description = strings.packSmartTvDesc,
                count = DomainFilterEngine.SMART_TV_DOMAINS.size,
                countSuffix = strings.rulesCountSuffix,
                icon = Icons.Filled.LiveTv,
                checked = state.settings.blockTvAds,
                onToggle = { onToggleTvAds(!state.settings.blockTvAds) },
                testTag = "filter_pack_smart_tv"
            )
        }

        item {
            FilterPackRow(
                title = strings.packAdNetworksTitle,
                description = strings.packAdNetworksDesc,
                count = DomainFilterEngine.AD_NETWORK_DOMAINS.size,
                countSuffix = strings.rulesCountSuffix,
                icon = Icons.Filled.Block,
                checked = state.settings.blockAdNetworks,
                onToggle = { onToggleAdNetworks(!state.settings.blockAdNetworks) },
                testTag = "filter_pack_ad_networks"
            )
        }

        item {
            FilterPackRow(
                title = strings.packStreamingTitle,
                description = strings.packStreamingDesc,
                count = DomainFilterEngine.STREAMING_AD_DOMAINS.size,
                countSuffix = strings.rulesCountSuffix,
                icon = Icons.Filled.PlayCircle,
                checked = state.settings.blockStreamingAds,
                onToggle = { onToggleStreamingAds(!state.settings.blockStreamingAds) },
                testTag = "filter_pack_streaming"
            )
        }

        item {
            FilterPackRow(
                title = strings.packTrackersTitle,
                description = strings.packTrackersDesc,
                count = DomainFilterEngine.TRACKER_DOMAINS.size,
                countSuffix = strings.rulesCountSuffix,
                icon = Icons.Filled.Security,
                checked = state.settings.blockTrackers,
                onToggle = { onToggleTrackers(!state.settings.blockTrackers) },
                testTag = "filter_pack_trackers"
            )
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.customRulesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TvActionPill(
                    label = strings.addRuleButton,
                    icon = Icons.Filled.Add,
                    onClick = { showAddDialog = true },
                    selected = true,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_custom_rule_button")
                )
            }
        }

        if (state.customRules.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.emptyCustomRules,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(state.customRules, key = { it.id }) { rule ->
                CustomRuleItemCard(
                    rule = rule,
                    strings = strings,
                    onDelete = { onDeleteRule(rule.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showAddDialog) {
        AddCustomDomainDialog(
            strings = strings,
            onDismiss = { showAddDialog = false },
            onSave = { domain, isAllowlist ->
                onAddRule(domain, isAllowlist)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FilterPackRow(
    title: String,
    description: String,
    count: Int,
    countSuffix: String,
    icon: ImageVector,
    checked: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    TvFocusableCard(
        onClick = onToggle,
        borderColor = if (checked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusPillBadge(
                        text = "$count $countSuffix",
                        color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        backgroundColor = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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

@Composable
private fun CustomRuleItemCard(
    rule: CustomDomainRule,
    strings: UiStrings,
    onDelete: () -> Unit
) {
    val badgeColor = if (rule.isAllowlist) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    val badgeBg = if (rule.isAllowlist) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (rule.isAllowlist) Icons.Filled.CheckCircle else Icons.Filled.Block,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.domain,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                StatusPillBadge(
                    text = if (rule.isAllowlist) strings.allowDomainTab else strings.blockDomainTab,
                    color = badgeColor,
                    backgroundColor = badgeBg
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_rule_${rule.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete ${rule.domain}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddCustomDomainDialog(
    strings: UiStrings,
    onDismiss: () -> Unit,
    onSave: (domain: String, isAllowlist: Boolean) -> Unit
) {
    var domainText by rememberSaveable { mutableStateOf("") }
    var isAllowlist by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = strings.addDialogTitle,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TvActionPill(
                        label = strings.blockDomainTab,
                        icon = Icons.Filled.Block,
                        onClick = { isAllowlist = false },
                        selected = !isAllowlist,
                        accentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("dialog_rule_block_pill")
                    )
                    TvActionPill(
                        label = strings.allowDomainTab,
                        icon = Icons.Filled.CheckCircle,
                        onClick = { isAllowlist = true },
                        selected = isAllowlist,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("dialog_rule_allow_pill")
                    )
                }

                OutlinedTextField(
                    value = domainText,
                    onValueChange = { domainText = it },
                    placeholder = { Text(strings.addDialogHint) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_domain_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(domainText, isAllowlist) },
                enabled = domainText.trim().contains('.'),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("dialog_save_rule_button")
            ) {
                Text(strings.saveButton)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_rule_button")
            ) {
                Text(strings.cancelButton)
            }
        }
    )
}
