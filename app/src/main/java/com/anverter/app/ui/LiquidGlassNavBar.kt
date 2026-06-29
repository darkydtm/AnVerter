package com.anverter.app.ui

/*
 * The floating draggable "liquid glass" pill is adapted from the KernelSU-Next
 * manager (github.com/rifsxd/KernelSU-Next, GPL-3.0). Ported from Material3 to
 * the miuix color scheme and reduced to index-based selection.
 */

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class NavBarItem(
	val selectedIcon: ImageVector,
	val unselectedIcon: ImageVector,
	val contentDescription: String,
)

private val ItemSize = 56.dp
private val ItemSpacing = 4.dp
private val ContainerPadding = 7.dp
private val BarHeight = 72.dp

@Composable
fun LiquidGlassNavBar(
	items: List<NavBarItem>,
	selectedIndex: Int,
	onSelect: (Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	var isDragging by remember { mutableStateOf(false) }
	var dragTargetIndex by remember { mutableStateOf(selectedIndex) }

	val animatedIndex by animateFloatAsState(
		targetValue = (if (isDragging) dragTargetIndex else selectedIndex).toFloat(),
		animationSpec = if (isDragging) {
			spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
		} else {
			spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
		},
		label = "navIndicator",
	)

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.windowInsetsPadding(WindowInsets.navigationBars),
	) {
		val horizontalPadding = when {
			maxWidth > 600.dp -> 32.dp
			maxWidth > 400.dp -> 24.dp
			else -> 16.dp
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = horizontalPadding, vertical = 14.dp),
			contentAlignment = Alignment.Center,
		) {
			val barWidth = (ItemSize * items.size) +
				(ItemSpacing * (items.size - 1)) +
				(ContainerPadding * 2)

			val density = LocalDensity.current
			val itemSizePx = with(density) { ItemSize.toPx() }
			val itemSpacingPx = with(density) { ItemSpacing.toPx() }
			val containerPaddingPx = with(density) { ContainerPadding.toPx() }

			Box(
				modifier = Modifier
					.width(barWidth)
					.height(BarHeight)
					.shadow(8.dp, RoundedCornerShape(24.dp))
					.background(MiuixTheme.colorScheme.surfaceContainer, RoundedCornerShape(24.dp))
					.pointerInput(items.size, selectedIndex) {
						detectDragGestures(
							onDragStart = { offset ->
								val extra = with(density) { 20.dp.toPx() }
								val pillLeft = containerPaddingPx +
									selectedIndex * (itemSizePx + itemSpacingPx) - extra
								val pillRight = pillLeft + itemSizePx + extra * 2
								if (offset.x in pillLeft..pillRight) {
									isDragging = true
									dragTargetIndex = selectedIndex
								}
							},
							onDragEnd = {
								if (isDragging) {
									onSelect(dragTargetIndex)
									isDragging = false
								}
							},
							onDragCancel = { isDragging = false },
							onDrag = { change, _ ->
								if (isDragging) {
									change.consume()
									dragTargetIndex = ((change.position.x - containerPaddingPx) /
										(itemSizePx + itemSpacingPx))
										.toInt()
										.coerceIn(0, items.lastIndex)
								}
							},
						)
					},
			) {
				var measuredWidth by remember { mutableStateOf(0) }

				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(horizontal = ContainerPadding)
						.onSizeChanged { measuredWidth = it.width },
				) {
					if (measuredWidth > 0 && items.isNotEmpty()) {
						val indicatorOffset = (itemSizePx + itemSpacingPx) * animatedIndex
						Box(
							modifier = Modifier
								.fillMaxHeight()
								.padding(vertical = 8.dp)
								.offset { IntOffset(x = indicatorOffset.toInt(), y = 0) }
								.width(ItemSize)
								.background(
									color = MiuixTheme.colorScheme.secondaryContainer,
									shape = RoundedCornerShape(16.dp),
								),
						)
					}

					Row(
						modifier = Modifier.fillMaxSize(),
						horizontalArrangement = Arrangement.spacedBy(ItemSpacing),
						verticalAlignment = Alignment.CenterVertically,
					) {
						items.forEachIndexed { index, item ->
							val active = index == (if (isDragging) dragTargetIndex else selectedIndex)
							Box(
								modifier = Modifier
									.size(ItemSize)
									.clip(RoundedCornerShape(16.dp))
									.clickable { onSelect(index) },
								contentAlignment = Alignment.Center,
							) {
								Icon(
									imageVector = if (active) item.selectedIcon else item.unselectedIcon,
									contentDescription = item.contentDescription,
									tint = if (active) {
										MiuixTheme.colorScheme.primary
									} else {
										MiuixTheme.colorScheme.onSurfaceVariantSummary
									},
								)
							}
						}
					}
				}
			}
		}
	}
}
