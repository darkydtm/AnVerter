package com.anverter.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRatesResponse(
    val rates: Map<String, RateDto> = emptyMap(),
)

@Serializable
data class RateDto(
    val name: String = "",
    val unit: String = "",
    val value: Double = 0.0,
    val type: String = "",
)
