package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * TV D-Pad & Touch friendly elevated surface card.
 * Shows an unmistakable cyan focus ring and gentle lift when highlighted by a Google TV remote.
 */
@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    focusedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
    focusedBorderColor: Color = MaterialTheme.colorScheme.secondary,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tvCardScale"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isFocused) focusedBorderColor else borderColor,
        label = "tvCardBorder"
    )
    val animatedBg by animateColorAsState(
        targetValue = if (isFocused) focusedContainerColor else containerColor,
        label = "tvCardBg"
    )

    Surface(
        shape = shape,
        color = animatedBg,
        border = BorderStroke(if (isFocused) 2.5.dp else 1.dp, animatedBorder),
        tonalElevation = if (isFocused) 6.dp else 1.dp,
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content(isFocused)
        }
    }
}

@Composable
fun TvActionPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    minHeight: Dp = 48.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            selected -> accentColor.copy(alpha = 0.22f)
            isFocused -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        label = "pillBg"
    )
    val strokeColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.secondary
            selected -> accentColor
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        },
        label = "pillBorder"
    )
    val contentColor = if (selected) accentColor else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(if (isFocused) 2.dp else 1.dp, strokeColor),
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = minHeight)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatusPillBadge(
    text: String,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
