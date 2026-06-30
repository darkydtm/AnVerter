package com.anverter.app.data.local

import kotlinx.serialization.Serializable

@Serializable
data class CalculatorHistoryItem(
	val expression: String,
	val result: String,
	val timestampEpochMs: Long,
)

internal const val MAX_CALCULATOR_HISTORY = 20
