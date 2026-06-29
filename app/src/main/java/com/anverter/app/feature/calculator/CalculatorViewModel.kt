package com.anverter.app.feature.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
    val expression: String = "",
    val preview: String = "",
)

class CalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    fun input(token: String) {
        _state.update { it.copy(expression = it.expression + token) }
        recomputePreview()
    }

    fun clear() {
        _state.value = CalculatorUiState()
    }

    fun backspace() {
        _state.update { it.copy(expression = it.expression.dropLast(1)) }
        recomputePreview()
    }

    fun equals() {
        val expression = _state.value.expression
        val result = CalculatorEngine.evaluateOrNull(expression)
        _state.value = if (result != null) {
            CalculatorUiState(expression = formatCalcResult(result), preview = "")
        } else {
            _state.value.copy(preview = ERROR)
        }
    }

    private fun recomputePreview() {
        val expression = _state.value.expression
        val preview = CalculatorEngine.evaluateOrNull(expression)?.let(::formatCalcResult).orEmpty()
        _state.update { it.copy(preview = preview) }
    }

    private companion object {
        const val ERROR = "Ошибка"
    }
}
