package com.anverter.app.feature.converter

import com.anverter.app.domain.model.CurrencyType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/** Parses user input tolerant to comma decimals and spaces. */
fun parseAmount(input: String): Double {
    val normalized = input.trim().replace(',', '.').replace(" ", "")
    return normalized.toDoubleOrNull() ?: 0.0
}

/** Formats a converted value: more precision for crypto, 2 decimals for fiat/commodity. */
fun formatAmount(value: Double, type: CurrencyType): String {
    if (!value.isFinite()) return "0"
    val scale = if (type == CurrencyType.CRYPTO) 8 else 2
    val rounded = BigDecimal(value).setScale(scale, RoundingMode.HALF_UP)
    val trimmed = if (type == CurrencyType.CRYPTO) rounded.stripTrailingZeros() else rounded
    return trimmed.toPlainString().let {
        if (type == CurrencyType.CRYPTO) it else groupThousands(it)
    }
}

private fun groupThousands(plain: String): String {
    val negative = plain.startsWith("-")
    val unsigned = if (negative) plain.substring(1) else plain
    val parts = unsigned.split(".")
    val intPart = parts[0]
    val grouped = intPart.reversed().chunked(3).joinToString(" ").reversed()
    val result = if (parts.size > 1) "$grouped.${parts[1]}" else grouped
    return if (negative) "-$result" else result
}

fun formatTimestamp(epochMs: Long): String {
    val formatter = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(java.util.Date(epochMs))
}
