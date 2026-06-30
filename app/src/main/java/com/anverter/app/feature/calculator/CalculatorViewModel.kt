package com.anverter.app.feature.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anverter.app.data.local.CalculatorHistoryItem
import com.anverter.app.data.local.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
	val expression: String = "",
	val preview: String = "",
	val error: Boolean = false,
	val history: List<CalculatorHistoryItem> = emptyList(),
)

class CalculatorViewModel(
    private val store: SettingsStore,
) : ViewModel() {

	private val _state = MutableStateFlow(CalculatorUiState())
	val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

	init {
		viewModelScope.launch {
			store.calculatorHistory.collect { history ->
				_state.update { it.copy(history = history) }
			}
		}
	}

	fun input(token: String) {
		_state.update { it.copy(expression = it.expression + token, error = false) }
		recomputePreview()
	}

	fun clear() {
		_state.update { it.copy(expression = "", preview = "", error = false) }
	}

	fun backspace() {
		_state.update { it.copy(expression = it.expression.dropLast(1), error = false) }
		recomputePreview()
	}

	fun equals() {
		val expression = _state.value.expression
		val result = CalculatorEngine.evaluateOrNull(expression)
		_state.value = if (result != null) {
			val formatted = formatCalcResult(result)
			viewModelScope.launch {
				store.addCalculatorHistory(
					CalculatorHistoryItem(
						expression = expression,
						result = formatted,
						timestampEpochMs = System.currentTimeMillis(),
					),
				)
			}
			_state.value.copy(expression = formatted, preview = "", error = false)
		} else {
			_state.value.copy(error = true)
		}
	}

	private fun recomputePreview() {
		val expression = _state.value.expression
		val preview = CalculatorEngine.evaluateOrNull(expression)?.let(::formatCalcResult).orEmpty()
		_state.update { it.copy(preview = preview) }
	}
}
