package com.anverter.app.feature.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.anverter.app.ui.adaptive.AppColors
import com.anverter.app.ui.adaptive.AppIcon
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

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = stringResource(R.string.calculator_title))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
        ) {
            AppText(
                text = state.expression.ifEmpty { "0" },
                fontSize = 44.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppColors.onBackground,
            )
            AppText(
                text = if (state.error) stringResource(R.string.calculator_error) else state.preview,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (state.error) {
                    AppColors.error
                } else {
                    AppColors.onBackgroundVariant
                },
            )
        }

        Keypad(viewModel, bottomPadding)
    }
}

@Composable
private fun Keypad(viewModel: CalculatorViewModel, bottomPadding: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = bottomPadding + 8.dp),
    ) {
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
            Key(CalculatorKey("0", KeyKind.DIGIT) { viewModel.input("0") })
            Key(CalculatorKey(".", KeyKind.DIGIT) { viewModel.input(".") })
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
private fun RowScope.Key(key: CalculatorKey) {
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
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(6.dp),
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
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = foreground,
                )
            }
        }
    }
}
