package com.anverter.app.feature.converter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.using
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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

private val converterSpring = spring<Float>(
	dampingRatio = Spring.DampingRatioMediumBouncy,
	stiffness = Spring.StiffnessMediumLow,
)

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

	BoxWithConstraints(modifier = modifier.fillMaxSize()) {
		val compact = maxHeight < 620.dp
		Column(modifier = Modifier.fillMaxSize().imePadding()) {
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
				ConversionFields(
					state = state,
					compact = compact,
					onFromValueChange = viewModel::setFromAmount,
					onToValueChange = viewModel::setToAmount,
					onFromCurrencyClick = { picker = PickerTarget.FROM },
					onToCurrencyClick = { picker = PickerTarget.TO },
					onSwap = viewModel::swap,
				)

				Spacer(Modifier.height(8.dp))
				AnimatedContent(
					targetState = state.statusText(),
					transitionSpec = {
						(fadeIn(converterSpring) + scaleIn(converterSpring, initialScale = 0.97f))
							.togetherWith(fadeOut(converterSpring) + scaleOut(converterSpring, targetScale = 1.03f))
							.using(SizeTransform(clip = false))
					},
					label = "converter-status",
				) { status ->
					StatusLine(status)
				}
				AnimatedVisibility(
					visible = state.recents.isNotEmpty(),
					enter = fadeIn(converterSpring) + scaleIn(converterSpring, initialScale = 0.97f),
					exit = fadeOut(converterSpring) + scaleOut(converterSpring, targetScale = 0.97f),
				) {
					RecentConversions(state, viewModel)
				}
				Spacer(Modifier.height(bottomPadding + 16.dp))
			}
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
private fun ConversionFields(
	state: ConverterUiState,
	compact: Boolean,
	onFromValueChange: (String) -> Unit,
	onToValueChange: (String) -> Unit,
	onFromCurrencyClick: () -> Unit,
	onToCurrencyClick: () -> Unit,
	onSwap: () -> Unit,
) {
	if (compact) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalAlignment = Alignment.Top,
		) {
			ConversionField(
				title = stringResource(R.string.converter_from),
				value = state.fromAmountInput,
				onValueChange = onFromValueChange,
				currencyCode = state.fromCode,
				currencies = state.currencies,
				onCurrencyClick = onFromCurrencyClick,
				modifier = Modifier.weight(1f),
			)
			ConversionField(
				title = stringResource(R.string.converter_to),
				value = state.toAmountInput,
				onValueChange = onToValueChange,
				currencyCode = state.toCode,
				currencies = state.currencies,
				onCurrencyClick = onToCurrencyClick,
				modifier = Modifier.weight(1f),
			)
		}
		Row(
			modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
			horizontalArrangement = Arrangement.Center,
		) {
			SwapButton(onSwap)
		}
	} else {
		ConversionField(
			title = stringResource(R.string.converter_from),
			value = state.fromAmountInput,
			onValueChange = onFromValueChange,
			currencyCode = state.fromCode,
			currencies = state.currencies,
			onCurrencyClick = onFromCurrencyClick,
		)
		Row(
			modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
			horizontalArrangement = Arrangement.Center,
		) {
			SwapButton(onSwap)
		}
		ConversionField(
			title = stringResource(R.string.converter_to),
			value = state.toAmountInput,
			onValueChange = onToValueChange,
			currencyCode = state.toCode,
			currencies = state.currencies,
			onCurrencyClick = onToCurrencyClick,
		)
	}
}

@Composable
private fun SwapButton(onSwap: () -> Unit) {
	AppIconButton(
		onClick = onSwap,
		backgroundColor = AppColors.primary,
	) {
		AppIcon(
			imageVector = Icons.Filled.SwapVert,
			contentDescription = stringResource(R.string.converter_swap),
			tint = AppColors.onPrimary,
		)
	}
}

@Composable
private fun ConversionField(
	title: String,
	value: String,
	onValueChange: (String) -> Unit,
	currencyCode: String,
	currencies: List<CurrencyOption>,
	onCurrencyClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
		AppSmallTitle(text = title)
		AppTextField(
			value = TextFieldValue(value),
			onValueChange = { onValueChange(it.text) },
			label = title,
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
			modifier = Modifier.fillMaxWidth(),
		)
		AnimatedContent(
			targetState = currencyLabel(currencies, currencyCode),
			transitionSpec = {
				(fadeIn(converterSpring) + scaleIn(converterSpring, initialScale = 0.97f))
					.togetherWith(fadeOut(converterSpring) + scaleOut(converterSpring, targetScale = 1.03f))
					.using(SizeTransform(clip = false))
			},
			label = "converter-currency-field",
		) { label ->
			AppCard(modifier = Modifier.fillMaxWidth()) {
				AppPreferenceRow(
					title = label,
					onClick = onCurrencyClick,
				)
			}
		}
	}
}

private fun currencyLabel(currencies: List<CurrencyOption>, currencyCode: String): String =
	currencies.firstOrNull { it.code == currencyCode }?.label ?: currencyCode.uppercase()

@Composable
private fun RecentConversions(state: ConverterUiState, viewModel: ConverterViewModel) {
	AppSmallTitle(text = stringResource(R.string.converter_recent))
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState()),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		state.recents.forEach { pair ->
			AnimatedContent(
				targetState = pair,
				transitionSpec = {
					(fadeIn(converterSpring) + scaleIn(converterSpring, initialScale = 0.95f))
						.togetherWith(fadeOut(converterSpring))
						.using(SizeTransform(clip = false))
				},
				label = "converter-recent",
			) { recent ->
				AppSurface(
					onClick = { viewModel.applyRecent(recent) },
					shape = RoundedCornerShape(16.dp),
					color = AppColors.secondaryContainer,
					contentColor = AppColors.onSecondaryContainer,
				) {
					AppText(
						text = recent.label,
						modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
						color = AppColors.onSecondaryContainer,
					)
				}
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
private fun LazyItemScope.CurrencyRow(
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
			.animateItem()
			.padding(start = 20.dp, end = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		AppText(
			text = option.label,
			modifier = Modifier
				.weight(1f)
				.clickable { selectClick() }
				.padding(top = 12.dp, bottom = 12.dp),
			color = if (option.code == selectedCode) AppColors.primary else AppColors.onSurface,
		)
		AppIconButton(onClick = { onToggleFavorite(option.code) }) {
			AppIcon(
				imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
				contentDescription = stringResource(R.string.converter_favorite),
				tint = if (isFavorite) AppColors.primary else AppColors.onSurfaceVariant,
			)
		}
	}
}

@Composable
private fun StatusLine(text: String) {
	AppText(
		text = text,
		modifier = Modifier.fillMaxWidth(),
		textAlign = TextAlign.Center,
		color = AppColors.onBackgroundVariant,
	)
}

@Composable
private fun ConverterUiState.statusText(): String = when {
	isLoading -> stringResource(R.string.converter_loading)
	error -> stringResource(R.string.converter_error)
	!online -> stringResource(R.string.converter_offline)
	updatedAtEpochMs != null ->
		stringResource(R.string.converter_updated, formatTimestamp(updatedAtEpochMs))
	else -> stringResource(R.string.converter_updated_never)
}
