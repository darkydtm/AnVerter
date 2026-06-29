package com.anverter.app.feature.converter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anverter.app.R
import androidx.compose.foundation.text.KeyboardOptions
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { viewModel.onPaused() }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = stringResource(R.string.converter_title),
            actions = {
                IconButton(onClick = viewModel::refresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.converter_refresh),
                        tint = MiuixTheme.colorScheme.onBackground,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            AmountField(state, viewModel)

            SmallTitle(text = stringResource(R.string.converter_from))
            CurrencyDropdown(
                title = stringResource(R.string.converter_from),
                state = state,
                selectedCode = state.fromCode,
                onSelect = viewModel::setFrom,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = viewModel::swap,
                    backgroundColor = MiuixTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = stringResource(R.string.converter_swap),
                        tint = MiuixTheme.colorScheme.onPrimary,
                    )
                }
            }

            SmallTitle(text = stringResource(R.string.converter_to))
            CurrencyDropdown(
                title = stringResource(R.string.converter_to),
                state = state,
                selectedCode = state.toCode,
                onSelect = viewModel::setTo,
            )

            Spacer(Modifier.height(12.dp))
            ResultCard(state)

            Spacer(Modifier.height(8.dp))
            StatusLine(state)

            Spacer(Modifier.height(bottomPadding + 16.dp))
        }
    }
}

@Composable
private fun AmountField(state: ConverterUiState, viewModel: ConverterViewModel) {
    var field by remember { mutableStateOf(TextFieldValue(state.amountInput)) }
    LaunchedEffect(state.amountInput) {
        if (state.amountInput != field.text) {
            field = field.copy(
                text = state.amountInput,
                selection = TextRange(state.amountInput.length),
            )
        }
    }
    SmallTitle(text = stringResource(R.string.converter_amount))
    TextField(
        value = field,
        onValueChange = {
            field = it
            viewModel.setAmount(it.text)
        },
        label = stringResource(R.string.converter_amount),
        useLabelAsPlaceholder = true,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CurrencyDropdown(
    title: String,
    state: ConverterUiState,
    selectedCode: String,
    onSelect: (String) -> Unit,
) {
    if (state.currencies.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.converter_loading),
                modifier = Modifier.padding(16.dp),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        return
    }
    val labels = state.currencies.map { it.label }
    val selectedIndex = state.currencies.indexOfFirst { it.code == selectedCode }.coerceAtLeast(0)
    Card(modifier = Modifier.fillMaxWidth()) {
        WindowDropdownPreference(
            title = title,
            items = labels,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = { index -> onSelect(state.currencies[index].code) },
        )
    }
}

@Composable
private fun ResultCard(state: ConverterUiState) {
    val toCode = state.toCode.uppercase()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (state.result.isEmpty()) "-" else state.result,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = toCode,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun StatusLine(state: ConverterUiState) {
    val text = when {
        state.isLoading -> stringResource(R.string.converter_loading)
        state.error -> stringResource(R.string.converter_error)
        !state.online -> stringResource(R.string.converter_offline)
        state.updatedAtEpochMs != null ->
            stringResource(R.string.converter_updated, formatTimestamp(state.updatedAtEpochMs))
        else -> stringResource(R.string.converter_updated_never)
    }
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MiuixTheme.colorScheme.onBackgroundVariant,
    )
}
