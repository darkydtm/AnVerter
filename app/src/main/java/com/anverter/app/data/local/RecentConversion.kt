package com.anverter.app.data.local

import kotlinx.serialization.Serializable

@Serializable
data class RecentConversion(
    val from: String,
    val to: String,
    val fromValue: String = "",
    val toValue: String = "",
    val timestampEpochMs: Long = 0L,
)

internal const val MAX_RECENT_CONVERSIONS = 8

internal fun List<RecentConversion>.withMostRecent(
    conversion: RecentConversion,
    limit: Int = MAX_RECENT_CONVERSIONS,
): List<RecentConversion> =
    (listOf(conversion) + filterNot { it == conversion }).take(limit)
