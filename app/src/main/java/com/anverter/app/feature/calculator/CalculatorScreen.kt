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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anverter.app.R
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class KeyKind { DIGIT, FUNCTION, OPERATOR, EQUALS }

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = stringResource(R.string.calculator_title))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = state.expression.ifEmpty { "0" },
                fontSize = 44.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MiuixTheme.colorScheme.onBackground,
            )
            Text(
                text = if (state.error) stringResource(R.string.calculator_error) else state.preview,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (state.error) {
                    MiuixTheme.colorScheme.error
                } else {
                    MiuixTheme.colorScheme.onBackgroundVariant
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
            Key("C", KeyKind.FUNCTION) { viewModel.clear() }
            Key("(", KeyKind.FUNCTION) { viewModel.input("(") }
            Key(")", KeyKind.FUNCTION) { viewModel.input(")") }
            Key("÷", KeyKind.OPERATOR) { viewModel.input("÷") }
        }
        Row(Modifier.fillMaxWidth()) {
            Key("7", KeyKind.DIGIT) { viewModel.input("7") }
            Key("8", KeyKind.DIGIT) { viewModel.input("8") }
            Key("9", KeyKind.DIGIT) { viewModel.input("9") }
            Key("×", KeyKind.OPERATOR) { viewModel.input("×") }
        }
        Row(Modifier.fillMaxWidth()) {
            Key("4", KeyKind.DIGIT) { viewModel.input("4") }
            Key("5", KeyKind.DIGIT) { viewModel.input("5") }
            Key("6", KeyKind.DIGIT) { viewModel.input("6") }
            Key("−", KeyKind.OPERATOR) { viewModel.input("−") }
        }
        Row(Modifier.fillMaxWidth()) {
            Key("1", KeyKind.DIGIT) { viewModel.input("1") }
            Key("2", KeyKind.DIGIT) { viewModel.input("2") }
            Key("3", KeyKind.DIGIT) { viewModel.input("3") }
            Key("+", KeyKind.OPERATOR) { viewModel.input("+") }
        }
        Row(Modifier.fillMaxWidth()) {
            Key("0", KeyKind.DIGIT) { viewModel.input("0") }
            Key(".", KeyKind.DIGIT) { viewModel.input(".") }
            Key("⌫", KeyKind.FUNCTION) { viewModel.backspace() }
            Key("=", KeyKind.EQUALS) { viewModel.equals() }
        }
    }
}

@Composable
private fun RowScope.Key(label: String, kind: KeyKind, onClick: () -> Unit) {
    val colorScheme = MiuixTheme.colorScheme
    val background: Color = when (kind) {
        KeyKind.DIGIT -> colorScheme.secondaryContainer
        KeyKind.FUNCTION -> colorScheme.secondaryContainerVariant
        KeyKind.OPERATOR -> colorScheme.primaryContainer
        KeyKind.EQUALS -> colorScheme.primary
    }
    val foreground: Color = when (kind) {
        KeyKind.DIGIT -> colorScheme.onSecondaryContainer
        KeyKind.FUNCTION -> colorScheme.onSecondaryContainerVariant
        KeyKind.OPERATOR -> colorScheme.onPrimaryContainer
        KeyKind.EQUALS -> colorScheme.onPrimary
    }
    Surface(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(6.dp),
        shape = RoundedCornerShape(28.dp),
        color = background,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = foreground,
            )
        }
    }
}
