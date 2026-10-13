/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2026 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared metrics for the translucent "liquid glass" surfaces used across the app.
 */
object GlassDefaults {
    /** Rounded card/panel shape used for list items, editors, and sheets. */
    val CardShape: Shape = RoundedCornerShape(24.dp)

    /** Hairline edge that catches the light. */
    val BorderWidth: Dp = 1.dp

    /** Soft tinted shadow that keeps glass panels floating above the background. */
    val CardElevation: Dp = 10.dp
}

/**
 * Resolved colors for a glass surface. Values are derived from the active
 * [MaterialTheme.colorScheme], so dynamic color and forced light/dark themes
 * are respected.
 */
@Immutable
data class GlassPalette(
    val fill: Color,
    val dialogFill: Color,
    val borderStart: Color,
    val borderEnd: Color,
    val shadow: Color,
    val sheen: Color,
)

@Composable
fun glassPalette(): GlassPalette {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    return remember(scheme, dark) {
        if (dark) {
            GlassPalette(
                fill = scheme.surface.copy(alpha = 0.58f),
                dialogFill = scheme.surfaceContainerHigh.copy(alpha = 0.94f),
                borderStart = Color.White.copy(alpha = 0.34f),
                borderEnd = Color.White.copy(alpha = 0.06f),
                shadow = Color.Black.copy(alpha = 0.55f),
                sheen = Color.White.copy(alpha = 0.09f),
            )
        } else {
            GlassPalette(
                fill = scheme.surface.copy(alpha = 0.66f),
                dialogFill = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                borderStart = Color.White.copy(alpha = 0.9f),
                borderEnd = scheme.outlineVariant.copy(alpha = 0.35f),
                shadow = Color.Black.copy(alpha = 0.18f),
                sheen = Color.White.copy(alpha = 0.36f),
            )
        }
    }
}

/**
 * Draws the characteristic glass edge: a bright specular highlight on the
 * top-left fading into a soft shaded rim on the bottom-right.
 */
@Composable
fun Modifier.glassBorder(
    shape: Shape = GlassDefaults.CardShape,
    width: Dp = GlassDefaults.BorderWidth,
    palette: GlassPalette = glassPalette(),
): Modifier = border(
    width = width,
    brush = Brush.linearGradient(
        colors = listOf(palette.borderStart, palette.borderEnd, palette.borderStart.copy(alpha = palette.borderStart.alpha * 0.25f)),
    ),
    shape = shape,
)

/**
 * A translucent, grounded panel with a specular edge and a top sheen — the
 * building block for the liquid glass look on cards, bars, and sheets.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = GlassDefaults.CardShape,
    tint: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
    elevation: Dp = GlassDefaults.CardElevation,
    borderWidth: Dp = GlassDefaults.BorderWidth,
    content: @Composable () -> Unit,
) {
    val palette = glassPalette()
    val fillColor = if (tint == Color.Unspecified) palette.fill else tint
    Surface(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = palette.shadow,
                spotColor = palette.shadow,
            )
            .glassBorder(shape = shape, width = borderWidth, palette = palette),
        shape = shape,
        color = fillColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(listOf(palette.sheen, Color.Transparent)),
                        shape = shape,
                    ),
            )
            content()
        }
    }
}

/**
 * A padded [GlassSurface] intended as a content card.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassDefaults.CardShape,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(modifier = modifier, shape = shape) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Adds iOS-style tactile feedback to a clickable glass element: a quick
 * springy press-down that eases back on release, plus a ripple.
 */
@Composable
fun Modifier.glassClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 700f),
        label = "glassPressScale",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick,
        )
}

/**
 * Full-bleed ambient backdrop: a near-white (or deep charcoal) canvas washed
 * with soft color blooms so translucent panels have something to float over.
 */
@Composable
fun GlassBackground(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    val tintAlpha = if (dark) 1f else 0.75f
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .drawBehind {
                val width = size.width
                val height = size.height
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(scheme.primary.copy(alpha = 0.22f * tintAlpha), Color.Transparent),
                        center = Offset(width * 0.12f, height * 0.02f),
                        radius = width * 1.15f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(scheme.tertiary.copy(alpha = 0.18f * tintAlpha), Color.Transparent),
                        center = Offset(width * 1.02f, height * 0.16f),
                        radius = width * 0.95f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(scheme.secondary.copy(alpha = 0.16f * tintAlpha), Color.Transparent),
                        center = Offset(width * 0.5f, height * 1.08f),
                        radius = width * 1.25f,
                    ),
                )
            },
    )
}
