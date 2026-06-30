package com.anverter.app.feature.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anverter.app.R
import com.anverter.app.data.local.CalculatorHistoryItem
import com.anverter.app.ui.adaptive.AppColors
import com.anverter.app.ui.adaptive.AppHalfSheet
import com.anverter.app.ui.adaptive.AppIcon
import com.anverter.app.ui.adaptive.AppIconButton
import com.anverter.app.ui.adaptive.AppSurface
import com.anverter.app.ui.adaptive.AppText
import com.anverter.app.ui.adaptive.AppTopBar

private enum class KeyKind { DIGIT, FUNCTION, OPERATOR, EQUALS }

private data class CalculatorKey(
	val label: String,
	val kind: KeyKind,
	val icon: ImageVector? = null,
	val contentDescription: Int? = null,
	val onClick: () -> Unit,
)

private val calculatorSpring = spring<Float>(
	dampingRatio = Spring.DampingRatioMediumBouncy,
	stiffness = Spring.StiffnessMediumLow,
)

@Composable
fun CalculatorScreen(
	viewModel: CalculatorViewModel,
	bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
	modifier: Modifier = Modifier,
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	var extendedVisible by rememberSaveable { mutableStateOf(false) }
	var historyVisible by rememberSaveable { mutableStateOf(false) }

	Column(modifier = modifier.fillMaxSize()) {
		AppTopBar(
			title = stringResource(R.string.calculator_title),
			actions = {
				if (state.history.isNotEmpty()) {
					AppIconButton(onClick = { historyVisible = true }) {
						AppIcon(
							imageVector = Icons.Filled.History,
							contentDescription = stringResource(R.string.calculator_history),
							tint = AppColors.onBackground,
						)
					}
				}
				AppIconButton(onClick = viewModel::clear) {
					AppIcon(
						imageVector = Icons.Filled.Delete,
						contentDescription = stringResource(R.string.calculator_clear),
						tint = AppColors.onBackground,
					)
				}
			},
		)

		Column(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.padding(horizontal = 24.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.Bottom,
			horizontalAlignment = Alignment.End,
		) {
			AnimatedCalculatorText(
				text = state.expression.ifEmpty { "0" },
				fontSize = 44.sp,
				fontWeight = FontWeight.Light,
				color = AppColors.onBackground,
			)
			AnimatedCalculatorText(
				text = if (state.error) stringResource(R.string.calculator_error) else state.preview,
				fontSize = 24.sp,
				color = if (state.error) AppColors.error else AppColors.onBackgroundVariant,
			)
		}

		Keypad(
			extendedVisible = extendedVisible,
			onExtendedVisibleToggle = { extendedVisible = !extendedVisible },
			viewModel = viewModel,
			bottomPadding = bottomPadding,
		)
	}

	if (historyVisible) {
		CalculatorHistorySheet(
			history = state.history,
			onDismiss = { historyVisible = false },
		)
	}
}

@Composable
private fun AnimatedCalculatorText(
	text: String,
	fontSize: androidx.compose.ui.unit.TextUnit,
	color: Color,
	fontWeight: FontWeight? = null,
) {
	AnimatedContent(
		targetState = text,
		transitionSpec = {
			(fadeIn(calculatorSpring) + scaleIn(calculatorSpring, initialScale = 0.94f))
				.togetherWith(fadeOut(calculatorSpring) + scaleOut(calculatorSpring, targetScale = 1.04f))
		},
		label = "calculator-text",
	) { value ->
		AppText(
			text = value,
			fontSize = fontSize,
			fontWeight = fontWeight,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			color = color,
		)
	}
}

@Composable
private fun CalculatorHistorySheet(
	history: List<CalculatorHistoryItem>,
	onDismiss: () -> Unit,
) {
	AppHalfSheet(onDismiss = onDismiss) {
		AppText(
			text = stringResource(R.string.calculator_history),
			modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
			fontSize = 20.sp,
			fontWeight = FontWeight.Bold,
			color = AppColors.onSurface,
		)
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			contentPadding = PaddingValues(
				start = 16.dp,
				end = 16.dp,
				bottom = 24.dp,
			),
		) {
			items(history) { item ->
				CalculatorHistoryRow(item)
			}
		}
	}
}

@Composable
private fun CalculatorHistoryRow(item: CalculatorHistoryItem) {
	AnimatedContent(
		targetState = item,
		transitionSpec = {
			(fadeIn(calculatorSpring) + scaleIn(calculatorSpring, initialScale = 0.97f))
				.togetherWith(fadeOut(calculatorSpring))
		},
		label = "calculator-history-item",
	) { historyItem ->
		AppSurface(
			shape = RoundedCornerShape(16.dp),
			color = AppColors.secondaryContainer,
			contentColor = AppColors.onSecondaryContainer,
		) {
			AppText(
				text = "${historyItem.expression} = ${historyItem.result}",
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 14.dp),
				color = AppColors.onSecondaryContainer,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

@Composable
private fun Keypad(
	extendedVisible: Boolean,
	onExtendedVisibleToggle: () -> Unit,
	viewModel: CalculatorViewModel,
	bottomPadding: androidx.compose.ui.unit.Dp,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp)
			.padding(bottom = bottomPadding + 8.dp)
			.animateContentSize(
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioNoBouncy,
					stiffness = Spring.StiffnessLow,
				),
			),
	) {
		AnimatedVisibility(
			visible = extendedVisible,
			enter = fadeIn(calculatorSpring) + scaleIn(calculatorSpring, initialScale = 0.96f),
			exit = fadeOut(calculatorSpring) + scaleOut(calculatorSpring, targetScale = 0.96f),
		) {
			ExtendedFunctionPanel(viewModel)
		}
		Row(Modifier.fillMaxWidth()) {
			Key(CalculatorKey("C", KeyKind.FUNCTION) { viewModel.clear() })
			Key(CalculatorKey("(", KeyKind.FUNCTION) { viewModel.input("(") })
			Key(CalculatorKey(")", KeyKind.FUNCTION) { viewModel.input(")") })
			Key(CalculatorKey("÷", KeyKind.OPERATOR) { viewModel.input("÷") })
		}
		Row(Modifier.fillMaxWidth()) {
			Key(CalculatorKey("7", KeyKind.DIGIT) { viewModel.input("7") })
			Key(CalculatorKey("8", KeyKind.DIGIT) { viewModel.input("8") })
			Key(CalculatorKey("9", KeyKind.DIGIT) { viewModel.input("9") })
			Key(CalculatorKey("×", KeyKind.OPERATOR) { viewModel.input("×") })
		}
		Row(Modifier.fillMaxWidth()) {
			Key(CalculatorKey("4", KeyKind.DIGIT) { viewModel.input("4") })
			Key(CalculatorKey("5", KeyKind.DIGIT) { viewModel.input("5") })
			Key(CalculatorKey("6", KeyKind.DIGIT) { viewModel.input("6") })
			Key(CalculatorKey("−", KeyKind.OPERATOR) { viewModel.input("−") })
		}
		Row(Modifier.fillMaxWidth()) {
			Key(CalculatorKey("1", KeyKind.DIGIT) { viewModel.input("1") })
			Key(CalculatorKey("2", KeyKind.DIGIT) { viewModel.input("2") })
			Key(CalculatorKey("3", KeyKind.DIGIT) { viewModel.input("3") })
			Key(CalculatorKey("+", KeyKind.OPERATOR) { viewModel.input("+") })
		}
		Row(Modifier.fillMaxWidth()) {
			Key(CalculatorKey(".", KeyKind.DIGIT) { viewModel.input(".") })
			Key(CalculatorKey("0", KeyKind.DIGIT) { viewModel.input("0") })
			Key(
				CalculatorKey(
					label = "",
					kind = KeyKind.FUNCTION,
					icon = if (extendedVisible) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
					contentDescription = R.string.calculator_toggle_functions,
				) { onExtendedVisibleToggle() },
			)
			Key(
				CalculatorKey(
					label = "",
					kind = KeyKind.FUNCTION,
					icon = Icons.Filled.Backspace,
					contentDescription = R.string.calculator_backspace,
				) { viewModel.backspace() },
			)
			Key(CalculatorKey("=", KeyKind.EQUALS) { viewModel.equals() })
		}
	}
}

@Composable
private fun ExtendedFunctionPanel(viewModel: CalculatorViewModel) {
	Row(Modifier.fillMaxWidth()) {
		CompactKey(CalculatorKey("√", KeyKind.FUNCTION) { viewModel.input("√") })
		CompactKey(CalculatorKey("x²", KeyKind.FUNCTION) { viewModel.input("^2") })
		CompactKey(CalculatorKey("1/x", KeyKind.FUNCTION) { viewModel.input("^-1") })
		CompactKey(CalculatorKey("n!", KeyKind.FUNCTION) { viewModel.input("!") })
		CompactKey(CalculatorKey("%", KeyKind.FUNCTION) { viewModel.input("%") })
	}
}

@Composable
private fun RowScope.Key(key: CalculatorKey) {
	CalculatorKeySurface(
		key = key,
		modifier = Modifier
			.weight(1f)
			.aspectRatio(1f)
			.padding(6.dp),
		fontSize = 26.sp,
	)
}

@Composable
private fun RowScope.CompactKey(key: CalculatorKey) {
	CalculatorKeySurface(
		key = key,
		modifier = Modifier
			.weight(1f)
			.height(52.dp)
			.padding(4.dp),
		fontSize = 18.sp,
	)
}

@Composable
private fun CalculatorKeySurface(
	key: CalculatorKey,
	modifier: Modifier,
	fontSize: androidx.compose.ui.unit.TextUnit,
) {
	val background: Color = when (key.kind) {
		KeyKind.DIGIT -> AppColors.secondaryContainer
		KeyKind.FUNCTION -> AppColors.secondaryContainerVariant
		KeyKind.OPERATOR -> AppColors.primaryContainer
		KeyKind.EQUALS -> AppColors.primary
	}
	val foreground: Color = when (key.kind) {
		KeyKind.DIGIT -> AppColors.onSecondaryContainer
		KeyKind.FUNCTION -> AppColors.onSecondaryContainerVariant
		KeyKind.OPERATOR -> AppColors.onPrimaryContainer
		KeyKind.EQUALS -> AppColors.onPrimary
	}
	AppSurface(
		onClick = key.onClick,
		modifier = modifier,
		shape = RoundedCornerShape(28.dp),
		color = background,
	) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			if (key.icon != null) {
				AppIcon(
					imageVector = key.icon,
					contentDescription = key.contentDescription?.let { stringResource(it) },
					modifier = Modifier.size(26.dp),
					tint = foreground,
				)
			} else {
				AppText(
					text = key.label,
					fontSize = fontSize,
					fontWeight = FontWeight.Medium,
					textAlign = TextAlign.Center,
					color = foreground,
				)
			}
		}
	}
}
