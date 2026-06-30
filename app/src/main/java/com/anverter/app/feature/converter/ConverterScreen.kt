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
import com.anverter.app.ui.adaptive.AppCard
import com.anverter.app.ui.adaptive.AppColors
import com.anverter.app.ui.adaptive.AppIcon
import com.anverter.app.ui.adaptive.AppIconButton
import com.anverter.app.ui.adaptive.AppPreferenceRow
import com.anverter.app.ui.adaptive.AppSmallTitle
import com.anverter.app.ui.adaptive.AppSurface
import com.anverter.app.ui.adaptive.AppText
import com.anverter.app.ui.adaptive.AppTextField
import com.anverter.app.ui.adaptive.AppTopBar
import com.anverter.app.ui.adaptive.appClick

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
        AppTopBar(
            title = stringResource(R.string.converter_title),
            actions = {
                AppIconButton(onClick = viewModel::refresh) {
                    AppIcon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.converter_refresh),
                        tint = AppColors.onBackground,
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

            AppSmallTitle(text = stringResource(R.string.converter_from))
            CurrencyField(
                state = state,
                selectedCode = state.fromCode,
                onClick = { picker = PickerTarget.FROM },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                AppIconButton(
                    onClick = viewModel::swap,
                    backgroundColor = AppColors.primary,
                ) {
                    AppIcon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = stringResource(R.string.converter_swap),
                        tint = AppColors.onPrimary,
                    )
                }
            }

            AppSmallTitle(text = stringResource(R.string.converter_to))
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
    AppSmallTitle(text = stringResource(R.string.converter_amount))
    AppTextField(
        value = field,
        onValueChange = {
            field = it
            viewModel.setAmount(it.text)
        },
        label = stringResource(R.string.converter_amount),
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
        AppCard(modifier = Modifier.fillMaxWidth()) {
            AppText(
                text = stringResource(R.string.converter_loading),
                modifier = Modifier.padding(16.dp),
                color = AppColors.onSurfaceVariant,
            )
        }
        return
    }
    val selectedLabel = state.currencies.firstOrNull { it.code == selectedCode }?.label
        ?: selectedCode.uppercase()
    AppCard(modifier = Modifier.fillMaxWidth()) {
        AppPreferenceRow(
            title = selectedLabel,
            onClick = onClick,
        )
    }
}

@Composable
private fun RecentConversions(state: ConverterUiState, viewModel: ConverterViewModel) {
    if (state.recents.isEmpty()) return
    AppSmallTitle(text = stringResource(R.string.converter_recent))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.recents.forEach { pair ->
            AppSurface(
                onClick = { viewModel.applyRecent(pair) },
                shape = RoundedCornerShape(16.dp),
                color = AppColors.secondaryContainer,
                contentColor = AppColors.onSecondaryContainer,
            ) {
                AppText(
                    text = pair.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = AppColors.onSecondaryContainer,
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
        AppSurface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.surface,
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                AppText(
                    text = title,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.onSurface,
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = stringResource(R.string.converter_search),
                    singleLine = true,
                    leadingIcon = {
                        AppIcon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = AppColors.onSurfaceVariant,
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
                            AppText(
                                text = stringResource(R.string.converter_no_results),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                                color = AppColors.onSurfaceVariant,
                            )
                        }
                    }
                    if (favs.isNotEmpty()) {
                        item { AppSmallTitle(text = stringResource(R.string.converter_favorites)) }
                        items(favs, key = { "fav-${it.code}" }) { option ->
                            CurrencyRow(option, true, selectedCode, onSelect, onToggleFavorite)
                        }
                        if (others.isNotEmpty()) {
                            item { AppSmallTitle(text = stringResource(R.string.converter_all)) }
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
    val selectClick = appClick { onSelect(option.code) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            text = option.label,
            modifier = Modifier
                .weight(1f)
                .clickable { selectClick() }
                .padding(top = 12.dp, bottom = 12.dp),
            color = if (option.code == selectedCode) {
                AppColors.primary
            } else {
                AppColors.onSurface
            },
        )
        AppIconButton(onClick = { onToggleFavorite(option.code) }) {
            AppIcon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = stringResource(R.string.converter_favorite),
                tint = if (isFavorite) {
                    AppColors.primary
                } else {
                    AppColors.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun ResultCard(state: ConverterUiState) {
    val toCode = state.toCode.uppercase()
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppText(
                text = if (state.result.isEmpty()) "-" else state.result,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = AppColors.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            AppText(
                text = toCode,
                color = AppColors.onSurfaceVariant,
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
    AppText(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = AppColors.onBackgroundVariant,
    )
}
