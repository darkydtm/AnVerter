package com.anverter.app.data.remote

import com.anverter.app.data.remote.dto.ExchangeRatesResponse
import com.anverter.app.domain.model.CurrencyRate
import com.anverter.app.domain.model.CurrencyType
import kotlinx.serialization.json.Json

/** Parses a CoinGecko `/exchange_rates` JSON body into domain [CurrencyRate]s keyed by code. */
object RatesParser {
    val json: Json = Json { ignoreUnknownKeys = true }

    fun parse(body: String): Map<String, CurrencyRate> {
        val response = json.decodeFromString<ExchangeRatesResponse>(body)
        return response.rates.mapValues { (code, dto) ->
            CurrencyRate(
                code = code,
                name = dto.name,
                unit = dto.unit,
                value = dto.value,
                type = CurrencyType.from(dto.type),
            )
        }
    }
}
