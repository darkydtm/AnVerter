package com.anverter.app.feature.converter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anverter.app.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class PickerTarget { FROM, TO }

@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var picker by remember { mutableStateOf<PickerTarget?>(null) }

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
            CurrencyField(
                state = state,
                selectedCode = state.fromCode,
                onClick = { picker = PickerTarget.FROM },
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
            CurrencyField(
                state = state,
                selectedCode = state.toCode,
                onClick = { picker = PickerTarget.TO },
            )

            Spacer(Modifier.height(12.dp))
            ResultCard(state)

            Spacer(Modifier.height(8.dp))
            StatusLine(state)

            RecentConversions(state, viewModel)

            Spacer(Modifier.height(bottomPadding + 16.dp))
        }
    }

    picker?.let { target ->
        val selectedCode = if (target == PickerTarget.FROM) state.fromCode else state.toCode
        CurrencyPickerDialog(
            title = stringResource(
                if (target == PickerTarget.FROM) R.string.converter_from else R.string.converter_to,
            ),
            currencies = state.currencies,
            favorites = state.favorites,
            selectedCode = selectedCode,
            onToggleFavorite = viewModel::toggleFavorite,
            onSelect = { code ->
                if (target == PickerTarget.FROM) viewModel.setFrom(code) else viewModel.setTo(code)
                picker = null
            },
            onDismiss = { picker = null },
        )
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
private fun CurrencyField(
    state: ConverterUiState,
    selectedCode: String,
    onClick: () -> Unit,
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
    val selectedLabel = state.currencies.firstOrNull { it.code == selectedCode }?.label
        ?: selectedCode.uppercase()
    Card(modifier = Modifier.fillMaxWidth()) {
        ArrowPreference(
            title = selectedLabel,
            onClick = onClick,
        )
    }
}

@Composable
private fun RecentConversions(state: ConverterUiState, viewModel: ConverterViewModel) {
    if (state.recents.isEmpty()) return
    SmallTitle(text = stringResource(R.string.converter_recent))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.recents.forEach { pair ->
            Surface(
                onClick = { viewModel.applyRecent(pair) },
                shape = RoundedCornerShape(16.dp),
                color = MiuixTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = pair.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = MiuixTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    title: String,
    currencies: List<CurrencyOption>,
    favorites: Set<String>,
    selectedCode: String,
    onToggleFavorite: (String) -> Unit,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val filtered = remember(query.text, currencies, favorites) {
        val q = query.text.trim()
        if (q.isEmpty()) {
            currencies
        } else {
            currencies.filter {
                it.label.contains(q, ignoreCase = true) || it.code.contains(q, ignoreCase = true)
            }
        }
    }
    val favs = filtered.filter { it.code in favorites }
    val others = filtered.filter { it.code !in favorites }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MiuixTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(12.dp))
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    label = stringResource(R.string.converter_search),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.converter_no_results),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                    if (favs.isNotEmpty()) {
                        item { SmallTitle(text = stringResource(R.string.converter_favorites)) }
                        items(favs, key = { "fav-${it.code}" }) { option ->
                            CurrencyRow(option, true, selectedCode, onSelect, onToggleFavorite)
                        }
                        if (others.isNotEmpty()) {
                            item { SmallTitle(text = stringResource(R.string.converter_all)) }
                        }
                    }
                    items(others, key = { it.code }) { option ->
                        CurrencyRow(option, option.code in favorites, selectedCode, onSelect, onToggleFavorite)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    option: CurrencyOption,
    isFavorite: Boolean,
    selectedCode: String,
    onSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = option.label,
            modifier = Modifier
                .weight(1f)
                .clickable { onSelect(option.code) }
                .padding(top = 12.dp, bottom = 12.dp),
            color = if (option.code == selectedCode) {
                MiuixTheme.colorScheme.primary
            } else {
                MiuixTheme.colorScheme.onSurface
            },
        )
        IconButton(onClick = { onToggleFavorite(option.code) }) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = stringResource(R.string.converter_favorite),
                tint = if (isFavorite) {
                    MiuixTheme.colorScheme.primary
                } else {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                },
            )
        }
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
