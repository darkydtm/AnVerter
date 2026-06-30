package com.anverter.app.ui.adaptive

import android.media.AudioManager
import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.using
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme
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
	dampingRatio = Spring.DampingRatioMediumBouncy,
	stiffness = Spring.StiffnessMediumLow,
)

private class AppActionMotionState(
	private val scope: CoroutineScope,
	private val scale: Animatable<Float, AnimationVector1D>,
	private val rotation: Animatable<Float, AnimationVector1D>,
) {
	val scaleValue: Float get() = scale.value
	val rotationValue: Float get() = rotation.value

	fun bounce() {
		scope.launch {
			scale.stop()
			scale.snapTo(0.965f)
			scale.animateTo(
				targetValue = 1.035f,
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioLowBouncy,
					stiffness = Spring.StiffnessMedium,
				),
			)
			scale.animateTo(
				targetValue = 1f,
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessMediumLow,
				),
			)
		}
		scope.launch {
			rotation.stop()
			rotation.snapTo(-0.7f)
			rotation.animateTo(
				targetValue = 0.45f,
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioLowBouncy,
					stiffness = Spring.StiffnessMedium,
				),
			)
			rotation.animateTo(
				targetValue = 0f,
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioMediumBouncy,
					stiffness = Spring.StiffnessMediumLow,
				),
			)
		}
	}
}

@Composable
private fun rememberAppActionMotionState(): AppActionMotionState {
	val scope = rememberCoroutineScope()
	val scale = remember { Animatable(1f) }
	val rotation = remember { Animatable(0f) }
	return remember(scope, scale, rotation) {
		AppActionMotionState(scope, scale, rotation)
	}
}

private fun Modifier.appActionMotion(state: AppActionMotionState): Modifier =
	graphicsLayer {
		scaleX = state.scaleValue
		scaleY = state.scaleValue
		rotationZ = state.rotationValue
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
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixCard(modifier = animatedModifier, content = content)
        UiStyle.MATERIAL3 -> MaterialCard(
            modifier = animatedModifier,
            shape = RoundedCornerShape(8.dp),
            colors = MaterialCardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
    val motion = rememberAppActionMotionState()
    val motionModifier = if (click != null) modifier.appActionMotion(motion) else modifier
    val motionClick = click?.let {
        {
            motion.bounce()
            it()
        }
    }
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> {
            if (motionClick != null) {
                MiuixSurface(
                    onClick = motionClick,
                    modifier = motionModifier,
                    shape = shape,
                    color = color,
                    contentColor = contentColor,
                    content = content,
                )
            } else {
                MiuixSurface(
                    modifier = motionModifier,
                    shape = shape,
                    color = color,
                    contentColor = contentColor,
                    content = content,
                )
            }
        }

        UiStyle.MATERIAL3 -> {
            if (motionClick != null) {
                MaterialSurface(
                    onClick = motionClick,
                    modifier = motionModifier,
                    shape = shape,
                    color = color,
                    contentColor = contentColor,
                    content = content,
                )
            } else {
                MaterialSurface(
                    modifier = motionModifier,
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
    val motion = rememberAppActionMotionState()
    val motionClick = {
        motion.bounce()
        click()
    }
    val motionModifier = modifier.appActionMotion(motion)
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixIconButton(
            onClick = motionClick,
            modifier = motionModifier,
            backgroundColor = backgroundColor ?: Color.Unspecified,
            content = content,
        )

        UiStyle.MATERIAL3 -> MaterialIconButton(
            onClick = motionClick,
            modifier = motionModifier,
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
    val motion = rememberAppActionMotionState()
    val miuixClick = onClick?.let { originalClick ->
        val click = appClick(originalClick)
        {
            motion.bounce()
            click()
        }
    }
    val motionModifier = if (onClick != null) modifier.appActionMotion(motion) else modifier
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixArrowPreference(
            title = title,
            modifier = motionModifier,
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
            onClick = miuixClick,
        )

        UiStyle.MATERIAL3 -> AppSurface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
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
									.togetherWith(fadeOut(appContentSpring) + scaleOut(appContentSpring, targetScale = 1.03f))
									.using(SizeTransform(clip = false))
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
    val motion = rememberAppActionMotionState()
    val motionModifier = modifier.appActionMotion(motion)
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixWindowDropdownPreference(
            title = title,
            items = items,
            selectedIndex = selectedIndex,
            modifier = motionModifier,
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
                motion.bounce()
                playSound()
                onSelectedIndexChange(index)
            },
            onExpandedChange = { expanded ->
                if (expanded) {
                    motion.bounce()
                    playSound()
                }
            },
        )

        UiStyle.MATERIAL3 -> {
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = motionModifier.fillMaxWidth()) {
                AppPreferenceRow(
                    title = title,
                    summary = items.getOrNull(selectedIndex),
                    icon = icon,
                    onClick = {
                        expanded = true
                    },
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
                                motion.bounce()
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
                val motion = rememberAppActionMotionState()
                MiuixFloatingNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = appClick {
                        motion.bounce()
                        onItemClick(index)
                    },
                    icon = item.icon,
                    label = item.label,
                )
            }
        }

        UiStyle.MATERIAL3 -> MaterialNavigationBar {
            items.forEachIndexed { index, item ->
                val motion = rememberAppActionMotionState()
                MaterialNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = appClick {
                        motion.bounce()
                        onItemClick(index)
                    },
                    modifier = Modifier.appActionMotion(motion),
                    icon = { MaterialIcon(item.icon, contentDescription = item.label) },
                    label = { MaterialText(item.label) },
                )
            }
        }
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
    val motion = rememberAppActionMotionState()
    val motionClick = {
        motion.bounce()
        click()
    }
    val motionModifier = Modifier.appActionMotion(motion)
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixNavigationBarItem(
            selected = selected,
            onClick = motionClick,
            icon = icon,
            label = label,
        )

        UiStyle.MATERIAL3 -> MaterialNavigationBarItem(
            selected = selected,
            onClick = motionClick,
            modifier = motionModifier,
            icon = { MaterialIcon(icon, contentDescription = label) },
            label = { MaterialText(label) },
        )
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
    val motion = rememberAppActionMotionState()
    val motionClick = {
        motion.bounce()
        click()
    }
    val motionModifier = Modifier.appActionMotion(motion)
    when (LocalUiStyle.current) {
        UiStyle.MIUIX -> MiuixFloatingNavigationBarItem(
            selected = selected,
            onClick = motionClick,
            icon = icon,
            label = label,
        )

        UiStyle.MATERIAL3 -> MaterialNavigationBarItem(
            selected = selected,
            onClick = motionClick,
            modifier = motionModifier,
            icon = { MaterialIcon(icon, contentDescription = label) },
            label = { MaterialText(label) },
        )
    }
}

object AppColors {
    val primary: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.primary
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.primary
        }

    val onPrimary: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onPrimary
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onPrimary
        }

    val surface: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.surface
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.surface
        }

    val onSurface: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onSurface
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onSurface
        }

    val onSurfaceVariant: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onSurfaceVariantSummary
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    val onBackground: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onBackground
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onBackground
        }

    val onBackgroundVariant: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onBackgroundVariant
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    val error: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.error
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.error
        }

    val secondaryContainer: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.secondaryContainer
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.secondaryContainer
        }

    val onSecondaryContainer: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onSecondaryContainer
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onSecondaryContainer
        }

    val secondaryContainerVariant: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.secondaryContainerVariant
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.surfaceVariant
        }

    val onSecondaryContainerVariant: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onSecondaryContainerVariant
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    val primaryContainer: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.primaryContainer
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.primaryContainer
        }

    val onPrimaryContainer: Color
        @Composable get() = when (LocalUiStyle.current) {
            UiStyle.MIUIX -> MiuixTheme.colorScheme.onPrimaryContainer
            UiStyle.MATERIAL3 -> MaterialTheme.colorScheme.onPrimaryContainer
        }
}
