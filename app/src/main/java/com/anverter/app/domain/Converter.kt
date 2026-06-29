package com.anverter.app.domain

import com.anverter.app.domain.model.CurrencyRate

/**
 * Pure conversion logic. All rates are anchored to BTC (`value` = units per 1 BTC), so any pair
 * converts as `amount * to.value / from.value`.
 */
object Converter {
    fun convert(amount: Double, from: CurrencyRate, to: CurrencyRate): Double {
        if (from.value == 0.0 || !amount.isFinite()) return 0.0
        return amount * to.value / from.value
    }
}
