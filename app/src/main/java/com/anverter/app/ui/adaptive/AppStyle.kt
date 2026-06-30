package com.anverter.app.ui.adaptive

import android.media.AudioManager
import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anverter.app.ui.SoundFeedback
import com.anverter.app.ui.UiStyle
import com.anverter.app.ui.theme.LocalAppDarkTheme
import androidx.compose.material3.Card as MaterialCard
import androidx.compose.material3.CardDefaults as MaterialCardDefaults
import androidx.compose.material3.DropdownMenu as MaterialDropdownMenu
import androidx.compose.material3.DropdownMenuItem as MaterialDropdownMenuItem
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.NavigationBar as MaterialNavigationBar
import androidx.compose.material3.NavigationBarItem as MaterialNavigationBarItem
import androidx.compose.material3.Scaffold as MaterialScaffold
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextField as MaterialTextField
import androidx.compose.material3.TopAppBar as MaterialTopAppBar
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar as MiuixFloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem as MiuixFloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.SmallTitle as MiuixSmallTitle
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference as MiuixArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference as MiuixWindowDropdownPreference

private val LocalUiStyle = compositionLocalOf { UiStyle.MIUIX }
private val LocalSoundController = compositionLocalOf { SoundController(play = {}) }

private data class SoundController(
	val play: () -> Unit,
)

private val appContentSpring = spring<Float>(
	dampingRatio = Spring.DampingRatioNoBouncy,
	stiffness = Spring.StiffnessLow,
)

private fun Modifier.appPressedMotion(
	enabled: Boolean = true,
	interactionSource: InteractionSource? = null,
): Modifier = composed {
	var pointerPressed by remember { mutableStateOf(false) }
	val interactionPressed = interactionSource?.collectIsPressedAsState()?.value ?: false
	val scale by animateFloatAsState(
		targetValue = if (enabled && (pointerPressed || interactionPressed)) 1.045f else 1f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioMediumBouncy,
			stiffness = Spring.StiffnessLow,
		),
		label = "app-pressed-scale",
	)
	Modifier
		.then(
			if (enabled) {
				Modifier.pointerInput(Unit) {
					awaitEachGesture {
						awaitFirstDown(requireUnconsumed = false)
						pointerPressed = true
						waitForUpOrCancellation()
						pointerPressed = false
					}
				}
			} else {
				Modifier
			},
		)
		.graphicsLayer {
			scaleX = scale
			scaleY = scale
		}
}

data class AppNavigationItem(
	val icon: ImageVector,
	val label: String,
)

@Composable
fun ProvideAppStyle(
    uiStyle: UiStyle,
    soundFeedback: SoundFeedback,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val audioManager = remember(context) {
        context.getSystemService(AudioManager::class.java)
    }
    val soundController = remember(soundFeedback, audioManager, view) {
        SoundController(
            play = {
                if (
                    soundFeedback == SoundFeedback.ON &&
                    audioManager?.ringerMode == AudioManager.RINGER_MODE_NORMAL
                ) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                }
            },
        )
    }

    CompositionLocalProvider(
        LocalUiStyle provides uiStyle,
        LocalSoundController provides soundController,
        content = content,
    )
}

@Composable
fun appClick(onClick: () -> Unit): () -> Unit {
    val soundController = LocalSoundController.current
    return {
        soundController.play()
        onClick()
    }
}

@Composable
fun AppScaffold(
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixScaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = bottomBar,
            content = content,
        )

        UiStyle.MATERIAL3 -> MaterialScaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = bottomBar,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
) {
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixTopAppBar(title = title, actions = actions)
        UiStyle.MATERIAL3 -> MaterialTopAppBar(
            title = { MaterialText(title) },
            actions = actions,
        )
    }
}

@Composable
fun AppSmallTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixSmallTitle(text = text, modifier = modifier)
        UiStyle.MATERIAL3 -> MaterialText(
            text = text,
            modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
            color = AppColors.onBackgroundVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun AppCard(
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit,
) {
	val animatedModifier = modifier.animateContentSize(
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioNoBouncy,
			stiffness = Spring.StiffnessLow,
		),
	)
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> MiuixCard(modifier = animatedModifier, content = content)
		UiStyle.MATERIAL3 -> MaterialCard(
			modifier = animatedModifier,
			shape = RoundedCornerShape(8.dp),
			colors = MaterialCardDefaults.cardColors(containerColor = AppColors.surface),
			content = content,
		)
	}
}

@Composable
fun AppSurface(
	onClick: (() -> Unit)? = null,
	modifier: Modifier = Modifier,
	shape: Shape = RoundedCornerShape(8.dp),
	color: Color = AppColors.surface,
	contentColor: Color = AppColors.onSurface,
	content: @Composable () -> Unit,
) {
	val click = onClick?.let { appClick(it) }
	val interactionSource = remember { MutableInteractionSource() }
	val pressedModifier = modifier.appPressedMotion(
		enabled = click != null,
		interactionSource = interactionSource,
	)
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> {
			if (click != null) {
				MiuixSurface(
					onClick = click,
					modifier = pressedModifier,
					shape = shape,
					color = color,
					contentColor = contentColor,
					content = content,
				)
			} else {
				MiuixSurface(
					modifier = pressedModifier,
					shape = shape,
					color = color,
					contentColor = contentColor,
					content = content,
				)
			}
		}

		UiStyle.MATERIAL3 -> {
			if (click != null) {
				MaterialSurface(
					onClick = click,
					modifier = pressedModifier,
					shape = shape,
					color = color,
					contentColor = contentColor,
					interactionSource = interactionSource,
					content = content,
				)
			} else {
				MaterialSurface(
					modifier = pressedModifier,
					shape = shape,
					color = color,
					contentColor = contentColor,
					content = content,
				)
			}
		}
	}
}

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.onSurface,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixText(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
        )

        UiStyle.MATERIAL3 -> MaterialText(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}

@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = AppColors.onSurface,
) {
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint,
        )

        UiStyle.MATERIAL3 -> MaterialIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint,
        )
    }
}

@Composable
fun AppIconButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	backgroundColor: Color? = null,
	content: @Composable () -> Unit,
) {
	val click = appClick(onClick)
	val interactionSource = remember { MutableInteractionSource() }
	val pressedModifier = modifier.appPressedMotion(interactionSource = interactionSource)
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> MiuixIconButton(
			onClick = click,
			modifier = pressedModifier,
			backgroundColor = backgroundColor ?: Color.Unspecified,
			content = content,
		)

		UiStyle.MATERIAL3 -> MaterialIconButton(
			onClick = click,
			modifier = pressedModifier,
			interactionSource = interactionSource,
			colors = backgroundColor?.let {
				IconButtonDefaults.iconButtonColors(
					containerColor = it,
					contentColor = AppColors.onPrimary,
				)
			} ?: IconButtonDefaults.iconButtonColors(),
			content = content,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            useLabelAsPlaceholder = true,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon,
            modifier = modifier,
        )

        UiStyle.MATERIAL3 -> MaterialTextField(
            value = value,
            onValueChange = onValueChange,
            label = { MaterialText(label) },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon,
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(),
        )
    }
}

@Composable
fun AppPreferenceRow(
	title: String,
	modifier: Modifier = Modifier,
	summary: String? = null,
	icon: ImageVector? = null,
	onClick: (() -> Unit)? = null,
) {
	val click = onClick?.let { appClick(it) }
	val interactionSource = remember { MutableInteractionSource() }
	val pressedModifier = modifier.appPressedMotion(
		enabled = click != null,
		interactionSource = interactionSource,
	)
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> MiuixArrowPreference(
			title = title,
			modifier = pressedModifier,
			summary = summary,
			startAction = icon?.let {
				{
					AppIcon(
						imageVector = it,
						contentDescription = null,
						modifier = Modifier.size(22.dp),
						tint = AppColors.onSurfaceVariant,
					)
				}
			},
			onClick = click,
		)

		UiStyle.MATERIAL3 -> AppSurface(
			onClick = onClick,
			modifier = modifier.fillMaxWidth(),
			color = AppColors.surface,
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(min = 64.dp)
					.padding(horizontal = 16.dp, vertical = 12.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				if (icon != null) {
					AppIcon(
						imageVector = icon,
						contentDescription = null,
						modifier = Modifier
							.padding(end = 16.dp)
							.size(22.dp),
						tint = AppColors.onSurfaceVariant,
					)
				}
				androidx.compose.foundation.layout.Column(
					modifier = Modifier.weight(1f),
				) {
					AppText(text = title, color = AppColors.onSurface)
					if (summary != null) {
						AnimatedContent(
							targetState = summary,
							transitionSpec = {
								(fadeIn(appContentSpring) + scaleIn(appContentSpring, initialScale = 0.97f))
									.togetherWith(fadeOut(appContentSpring) + scaleOut(appContentSpring, targetScale = 1.01f))
							},
							label = "preference-summary",
						) { value ->
							AppText(
								text = value,
								color = AppColors.onSurfaceVariant,
								fontSize = 13.sp,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
							)
						}
					}
				}
				if (onClick != null) {
					AppIcon(
						imageVector = Icons.Filled.KeyboardArrowRight,
						contentDescription = null,
						tint = AppColors.onSurfaceVariant,
					)
				}
			}
		}
	}
}

@Composable
fun AppDropdownPreference(
	title: String,
	items: List<String>,
	selectedIndex: Int,
	onSelectedIndexChange: (Int) -> Unit,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
) {
	val playSound = LocalSoundController.current.play
	val interactionSource = remember { MutableInteractionSource() }
	val pressedModifier = modifier.appPressedMotion(interactionSource = interactionSource)
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> MiuixWindowDropdownPreference(
			title = title,
			items = items,
			selectedIndex = selectedIndex,
			modifier = pressedModifier,
			startAction = icon?.let {
				{
					AppIcon(
						imageVector = it,
						contentDescription = null,
						modifier = Modifier.size(22.dp),
						tint = AppColors.onSurfaceVariant,
					)
				}
			},
			onSelectedIndexChange = { index ->
				playSound()
				onSelectedIndexChange(index)
			},
			onExpandedChange = { expanded ->
				if (expanded) {
					playSound()
				}
			},
		)

		UiStyle.MATERIAL3 -> {
			var expanded by remember { mutableStateOf(false) }
			Box(modifier = modifier.fillMaxWidth()) {
				AppPreferenceRow(
					title = title,
					summary = items.getOrNull(selectedIndex),
					icon = icon,
					onClick = { expanded = true },
				)
				MaterialDropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false },
				) {
					items.forEachIndexed { index, item ->
						MaterialDropdownMenuItem(
							text = { MaterialText(item) },
							leadingIcon = if (index == selectedIndex) {
								{
									MaterialIcon(
										imageVector = Icons.Filled.Check,
										contentDescription = null,
									)
								}
							} else {
								null
							},
							onClick = {
								playSound()
								expanded = false
								onSelectedIndexChange(index)
							},
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHalfSheet(
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit,
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.5f
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = AppColors.surface,
		contentColor = AppColors.onSurface,
		shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
		windowInsets = WindowInsets(0, 0, 0, 0),
	) {
		androidx.compose.foundation.layout.Column(
			modifier = modifier
				.fillMaxWidth()
				.height(sheetHeight),
			content = content,
		)
	}
}

@Composable
fun AppNavigationBar(content: @Composable RowScope.() -> Unit) {
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixNavigationBar(content = content)
        UiStyle.MATERIAL3 -> MaterialNavigationBar(content = content)
    }
}

@Composable
fun AppFloatingNavigationBar(
	items: List<AppNavigationItem>,
	selectedIndex: Int,
	onItemClick: (Int) -> Unit,
) {
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> MiuixFloatingNavigationBar {
			items.forEachIndexed { index, item ->
				MiuixFloatingNavigationItemContent(
					selected = selectedIndex == index,
					onClick = appClick { onItemClick(index) },
					icon = item.icon,
					label = item.label,
				)
			}
		}

		UiStyle.MATERIAL3 -> MaterialNavigationBar {
			items.forEachIndexed { index, item ->
				AppFloatingNavigationBarItem(
					selected = selectedIndex == index,
					onClick = { onItemClick(index) },
					icon = item.icon,
					label = item.label,
				)
			}
		}
	}
}

@Composable
private fun AppNavigationItemMotionBox(
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit,
) {
	Box(
		modifier = modifier.appPressedMotion(),
		contentAlignment = Alignment.Center,
	) {
		content()
	}
}

@Composable
private fun MiuixFloatingNavigationItemContent(
	selected: Boolean,
	onClick: () -> Unit,
	icon: ImageVector,
	label: String,
) {
	AppNavigationItemMotionBox {
		MiuixFloatingNavigationBarItem(
			selected = selected,
			onClick = onClick,
			icon = icon,
			label = label,
		)
	}
}

@Composable
fun RowScope.AppNavigationBarItem(
	selected: Boolean,
	onClick: () -> Unit,
	icon: ImageVector,
	label: String,
) {
	val click = appClick(onClick)
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> AppNavigationItemMotionBox(modifier = Modifier.weight(1f)) {
			MiuixNavigationBarItem(
				selected = selected,
				onClick = click,
				icon = icon,
				label = label,
			)
		}

		UiStyle.MATERIAL3 -> {
			val interactionSource = remember { MutableInteractionSource() }
			MaterialNavigationBarItem(
				selected = selected,
				onClick = click,
				modifier = Modifier.appPressedMotion(interactionSource = interactionSource),
				icon = { MaterialIcon(icon, contentDescription = label) },
				label = { MaterialText(label) },
				interactionSource = interactionSource,
			)
		}
	}
}

@Composable
fun RowScope.AppFloatingNavigationBarItem(
	selected: Boolean,
	onClick: () -> Unit,
	icon: ImageVector,
	label: String,
) {
	val click = appClick(onClick)
	when (LocalUiStyle.current) {
		UiStyle.MIUIX -> AppNavigationItemMotionBox(modifier = Modifier.weight(1f)) {
			MiuixFloatingNavigationBarItem(
				selected = selected,
				onClick = click,
				icon = icon,
				label = label,
			)
		}

		UiStyle.MATERIAL3 -> {
			val interactionSource = remember { MutableInteractionSource() }
			MaterialNavigationBarItem(
				selected = selected,
				onClick = click,
				modifier = Modifier.appPressedMotion(interactionSource = interactionSource),
				icon = { MaterialIcon(icon, contentDescription = label) },
				label = { MaterialText(label) },
				interactionSource = interactionSource,
			)
		}
	}
}

object AppColors {
	private val lightBackground = Color(0xFFF8FAFF)
	private val lightSurface = Color(0xFFFFFFFF)
	private val lightPrimary = Color(0xFF0E9F6E)
	private val lightPrimaryContainer = Color(0xFFBAF7D0)
	private val lightSecondaryContainer = Color(0xFFFFF2A8)
	private val lightSecondaryContainerVariant = Color(0xFFFFC7B8)
	private val lightOnPrimary = Color(0xFFFFFFFF)
	private val lightOnSurface = Color(0xFF182129)
	private val lightOnSurfaceVariant = Color(0xFF5A6472)
	private val lightOnContainer = Color(0xFF173124)
	private val lightOnVariantContainer = Color(0xFF4A2B20)
	private val lightError = Color(0xFFBA1A1A)

	private val darkBackground = Color(0xFF101418)
	private val darkSurface = Color(0xFF1A222B)
	private val darkPrimary = Color(0xFF55D89A)
	private val darkPrimaryContainer = Color(0xFF1F5A3D)
	private val darkSecondaryContainer = Color(0xFF5A4B0B)
	private val darkSecondaryContainerVariant = Color(0xFF63392E)
	private val darkOnPrimary = Color(0xFF053122)
	private val darkOnSurface = Color(0xFFE3EAF2)
	private val darkOnSurfaceVariant = Color(0xFFB0BCC9)
	private val darkOnContainer = Color(0xFFD7F8E3)
	private val darkOnVariantContainer = Color(0xFFFFDBD1)
	private val darkError = Color(0xFFFFB4AB)

	private val isDarkTheme: Boolean
		@Composable get() = LocalAppDarkTheme.current

	val background: Color
		@Composable get() = if (isDarkTheme) darkBackground else lightBackground

	val primary: Color
		@Composable get() = if (isDarkTheme) darkPrimary else lightPrimary

	val onPrimary: Color
		@Composable get() = if (isDarkTheme) darkOnPrimary else lightOnPrimary

	val surface: Color
		@Composable get() = if (isDarkTheme) darkSurface else lightSurface

	val onSurface: Color
		@Composable get() = if (isDarkTheme) darkOnSurface else lightOnSurface

	val onSurfaceVariant: Color
		@Composable get() = if (isDarkTheme) darkOnSurfaceVariant else lightOnSurfaceVariant

	val onBackground: Color
		@Composable get() = if (isDarkTheme) darkOnSurface else lightOnSurface

	val onBackgroundVariant: Color
		@Composable get() = if (isDarkTheme) darkOnSurfaceVariant else lightOnSurfaceVariant

	val error: Color
		@Composable get() = if (isDarkTheme) darkError else lightError

	val secondaryContainer: Color
		@Composable get() = if (isDarkTheme) darkSecondaryContainer else lightSecondaryContainer

	val onSecondaryContainer: Color
		@Composable get() = if (isDarkTheme) darkOnContainer else lightOnContainer

	val secondaryContainerVariant: Color
		@Composable get() = if (isDarkTheme) darkSecondaryContainerVariant else lightSecondaryContainerVariant

	val onSecondaryContainerVariant: Color
		@Composable get() = if (isDarkTheme) darkOnVariantContainer else lightOnVariantContainer

	val primaryContainer: Color
		@Composable get() = if (isDarkTheme) darkPrimaryContainer else lightPrimaryContainer

	val onPrimaryContainer: Color
		@Composable get() = if (isDarkTheme) darkOnContainer else lightOnContainer
}
