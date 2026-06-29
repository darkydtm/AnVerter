package com.anverter.app.domain.model

enum class CurrencyType {
    FIAT,
    CRYPTO,
    COMMODITY;

    companion object {
        fun from(raw: String): CurrencyType = when (raw.lowercase()) {
            "fiat" -> FIAT
            "crypto" -> CRYPTO
            else -> COMMODITY
        }
    }
}

/**
 * A currency as returned by CoinGecko `/exchange_rates`.
 *
 * @param value how many units of this currency equal 1 BTC (BTC is the anchor: its own value is 1.0).
 */
data class CurrencyRate(
    val code: String,
    val name: String,
    val unit: String,
    val value: Double,
    val type: CurrencyType,
)

data class RatesSnapshot(
    val rates: Map<String, CurrencyRate>,
    val updatedAtEpochMs: Long,
)
