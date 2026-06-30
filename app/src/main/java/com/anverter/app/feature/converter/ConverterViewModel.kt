package com.anverter.app.feature.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anverter.app.core.ConnectivityObserver
import com.anverter.app.data.RatesRepository
import com.anverter.app.data.local.RecentConversion
import com.anverter.app.data.local.SettingsStore
import com.anverter.app.domain.Converter
import com.anverter.app.domain.model.CurrencyRate
import com.anverter.app.domain.model.CurrencyType
import com.anverter.app.domain.model.RatesSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrencyOption(
    val code: String,
    val label: String,
    val type: CurrencyType,
)

data class RecentPair(
    val from: String,
    val to: String,
    val fromValue: String,
    val toValue: String,
    val label: String,
)

data class ConverterUiState(
    val isLoading: Boolean = false,
    val online: Boolean = true,
    val currencies: List<CurrencyOption> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val recents: List<RecentPair> = emptyList(),
    val fromCode: String = "usd",
    val toCode: String = "btc",
    val fromAmountInput: String = "1",
    val toAmountInput: String = "",
    val editingSide: ConversionSide = ConversionSide.FROM,
    val updatedAtEpochMs: Long? = null,
    val error: Boolean = false,
)

enum class ConversionSide {
    FROM,
    TO,
}

class ConverterViewModel(
    private val repository: RatesRepository,
    private val connectivity: ConnectivityObserver,
    private val settings: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(ConverterUiState())
    val state: StateFlow<ConverterUiState> = _state.asStateFlow()

    private var snapshot: RatesSnapshot? = null
    private var recentRaw: List<RecentConversion> = emptyList()
    private var resumed = false

    init {
        viewModelScope.launch {
            settings.favoriteCurrencies.collect { favorites ->
                _state.update { it.copy(favorites = favorites) }
            }
        }
        viewModelScope.launch {
            settings.recentConversions.collect { recents ->
                recentRaw = recents
                _state.update { it.copy(recents = recents.toPairs()) }
            }
        }
        viewModelScope.launch {
            repository.cachedRates.collect { snap ->
                snapshot = snap
                _state.update { current ->
                    val currencies = snap?.toOptions() ?: current.currencies
                    current.copy(
                        currencies = currencies,
                        updatedAtEpochMs = snap?.updatedAtEpochMs ?: current.updatedAtEpochMs,
                        recents = recentRaw.toPairs(),
                    )
                }
                recompute()
            }
        }
        viewModelScope.launch {
            connectivity.isOnline.collect { online ->
                val wasOffline = !_state.value.online
                _state.update { it.copy(online = online) }
                if (online && wasOffline && resumed && isStale()) refresh()
            }
        }
    }

    /** Called when the screen becomes visible. Syncs only if online and data is stale. */
    fun onResumed() {
        resumed = true
        if (_state.value.online && isStale()) refresh()
    }

    private fun isStale(): Boolean {
        val updatedAt = snapshot?.updatedAtEpochMs ?: return true
        return System.currentTimeMillis() - updatedAt > STALE_THRESHOLD_MS
    }

    fun onPaused() {
        resumed = false
    }

    fun refresh() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            val result = repository.refresh()
            _state.update { current ->
                result.fold(
                    onSuccess = { snap ->
                        snapshot = snap
                        current.copy(
                            isLoading = false,
                            error = false,
                            currencies = snap.toOptions(),
                            updatedAtEpochMs = snap.updatedAtEpochMs,
                        )
                    },
                    onFailure = { current.copy(isLoading = false, error = true) },
                )
            }
            recompute()
        }
    }

    fun setFromAmount(input: String) {
        _state.update { it.copy(fromAmountInput = input.filterAmountInput(), editingSide = ConversionSide.FROM) }
        recompute()
        recordRecent()
    }

    fun setToAmount(input: String) {
        _state.update { it.copy(toAmountInput = input.filterAmountInput(), editingSide = ConversionSide.TO) }
        recompute()
        recordRecent()
    }

    fun setFrom(code: String) {
        _state.update { it.copy(fromCode = code, editingSide = ConversionSide.FROM) }
        recompute()
        recordRecent()
    }

    fun setTo(code: String) {
        _state.update { it.copy(toCode = code, editingSide = ConversionSide.TO) }
        recompute()
        recordRecent()
    }

    fun swap() {
        _state.update {
            val swappedEditingSide = when (it.editingSide) {
                ConversionSide.FROM -> ConversionSide.TO
                ConversionSide.TO -> ConversionSide.FROM
            }
            it.copy(
                fromCode = it.toCode,
                toCode = it.fromCode,
                fromAmountInput = it.toAmountInput,
                toAmountInput = it.fromAmountInput,
                editingSide = swappedEditingSide,
            )
        }
        recompute()
        recordRecent()
    }

    fun toggleFavorite(code: String) {
        viewModelScope.launch { settings.toggleFavorite(code) }
    }

    fun applyRecent(pair: RecentPair) {
        _state.update {
            it.copy(
                fromCode = pair.from,
                toCode = pair.to,
                fromAmountInput = pair.fromValue,
                toAmountInput = pair.toValue,
                editingSide = ConversionSide.FROM,
            )
        }
        recompute()
        recordRecent()
    }

    private fun recordRecent() {
        val state = _state.value
        if (state.fromCode == state.toCode) return
        val rates = snapshot?.rates ?: return
        val from = rates[state.fromCode] ?: return
        val to = rates[state.toCode] ?: return
        val fromValue = state.fromAmountInput
        val toValue = when (state.editingSide) {
            ConversionSide.FROM -> formatAmount(Converter.convert(parseAmount(state.fromAmountInput), from, to), to.type)
            ConversionSide.TO -> state.toAmountInput
        }
        viewModelScope.launch {
            settings.addRecentConversion(
                RecentConversion(
                    from = state.fromCode,
                    to = state.toCode,
                    fromValue = fromValue,
                    toValue = toValue,
                    timestampEpochMs = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun recompute() {
        val rates = snapshot?.rates ?: return
        val state = _state.value
        val from = rates[state.fromCode]
        val to = rates[state.toCode]
        if (from == null || to == null) {
            _state.update {
                when (state.editingSide) {
                    ConversionSide.FROM -> it.copy(toAmountInput = "")
                    ConversionSide.TO -> it.copy(fromAmountInput = "")
                }
            }
            return
        }
        when (state.editingSide) {
            ConversionSide.FROM -> {
                val value = Converter.convert(parseAmount(state.fromAmountInput), from, to)
                _state.update {
                    it.copy(toAmountInput = formatAmount(value, to.type))
                }
            }

            ConversionSide.TO -> {
                val value = Converter.convert(parseAmount(state.toAmountInput), to, from)
                _state.update {
                    it.copy(fromAmountInput = formatAmount(value, from.type))
                }
            }
        }
    }

    private fun RatesSnapshot.toOptions(): List<CurrencyOption> =
        rates.values
            .map { it.toOption() }
            .sortedWith(compareBy({ it.type.ordinal }, { it.code }))

    private fun CurrencyRate.toOption(): CurrencyOption =
        CurrencyOption(code = code, label = "${code.uppercase()} · $name", type = type)

    private fun List<RecentConversion>.toPairs(): List<RecentPair> = map { recent ->
        RecentPair(
            from = recent.from,
            to = recent.to,
            fromValue = recent.fromValue,
            toValue = recent.toValue,
            label = "${recent.fromValue} ${recent.from.uppercase()} → ${recent.toValue} ${recent.to.uppercase()}",
        )
    }

    private fun String.filterAmountInput(): String {
        val cleaned = replace(',', '.').filter { it.isDigit() || it == '.' }
        val firstDot = cleaned.indexOf('.')
        if (firstDot == -1) return cleaned
        val head = cleaned.substring(0, firstDot + 1)
        val tail = cleaned.substring(firstDot + 1).replace(".", "")
        return head + tail
    }

    private companion object {
        const val STALE_THRESHOLD_MS = 120_000L
    }
}
