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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DnsQueryLog
import com.example.ui.ShieldUiState
import com.example.ui.UiStrings
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.TvActionPill
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityLogScreen(
    state: ShieldUiState,
    strings: UiStrings,
    isWideScreen: Boolean,
    onQuickAddRule: (domain: String, isAllowlist: Boolean) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = All, 1 = Blocked, 2 = Allowed
    var selectedFilter by rememberSaveable { mutableIntStateOf(0) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val filteredLogs = remember(state.recentLogs, selectedFilter) {
        when (selectedFilter) {
            1 -> state.recentLogs.filter { it.blocked }
            2 -> state.recentLogs.filter { !it.blocked }
            else -> state.recentLogs
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = if (isWideScreen) 28.dp else 16.dp,
            vertical = if (isWideScreen) 24.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.activityHeader,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.activitySubheader,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.recentLogs.isNotEmpty()) {
                    TvActionPill(
                        label = strings.clearLogs,
                        icon = Icons.Filled.DeleteSweep,
                        onClick = onClearLogs,
                        accentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("clear_logs_button")
                    )
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TvActionPill(
                    label = strings.filterAll,
                    icon = Icons.Filled.FilterList,
                    onClick = { selectedFilter = 0 },
                    selected = selectedFilter == 0,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("log_filter_all")
                )
                TvActionPill(
                    label = strings.filterBlocked,
                    icon = Icons.Filled.Block,
                    onClick = { selectedFilter = 1 },
                    selected = selectedFilter == 1,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("log_filter_blocked")
                )
                TvActionPill(
                    label = strings.filterAllowed,
                    icon = Icons.Filled.CheckCircle,
                    onClick = { selectedFilter = 2 },
                    selected = selectedFilter == 2,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("log_filter_allowed")
                )
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Radar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.emptyLogsTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.emptyLogsBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { entry ->
                DnsLogCard(
                    entry = entry,
                    formattedTime = timeFormatter.format(Date(entry.timestamp)),
                    strings = strings,
                    onQuickAction = {
                        // If currently blocked, quick action whitelists; if allowed, quick action blocks
                        onQuickAddRule(entry.domain, entry.blocked)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DnsLogCard(
    entry: DnsQueryLog,
    formattedTime: String,
    strings: UiStrings,
    onQuickAction: () -> Unit
) {
    val accent = if (entry.blocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val bg = if (entry.blocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (entry.blocked) Icons.Filled.Block else Icons.Filled.CheckCircle,
                contentDescription = if (entry.blocked) "Blocked" else "Allowed",
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.domain,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPillBadge(
                        text = entry.category,
                        color = accent,
                        backgroundColor = bg
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            TvActionPill(
                label = if (entry.blocked) strings.actionWhitelist else strings.actionBlock,
                icon = if (entry.blocked) Icons.Filled.CheckCircle else Icons.Filled.Block,
                onClick = onQuickAction,
                accentColor = if (entry.blocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                minHeight = 40.dp
            )
        }
    }
}
