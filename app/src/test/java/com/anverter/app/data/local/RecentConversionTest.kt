package com.anverter.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentConversionTest {

    @Test
    fun `new conversion is placed first`() {
        val current = listOf(
            RecentConversion("usd", "btc"),
            RecentConversion("eur", "usd"),
        )

        val updated = current.withMostRecent(RecentConversion("btc", "eur"))

        assertEquals(
            listOf(
                RecentConversion("btc", "eur"),
                RecentConversion("usd", "btc"),
                RecentConversion("eur", "usd"),
            ),
            updated,
        )
    }

    @Test
    fun `existing conversion moves to front without duplicate`() {
        val current = listOf(
            RecentConversion("usd", "btc"),
            RecentConversion("eur", "usd"),
            RecentConversion("btc", "eur"),
        )

        val updated = current.withMostRecent(RecentConversion("eur", "usd"))

        assertEquals(
            listOf(
                RecentConversion("eur", "usd"),
                RecentConversion("usd", "btc"),
                RecentConversion("btc", "eur"),
            ),
            updated,
        )
    }

    @Test
    fun `recent conversions are capped`() {
        val current = (1..8).map { index ->
            RecentConversion("from$index", "to$index")
        }

        val updated = current.withMostRecent(RecentConversion("new", "pair"))

        assertEquals(8, updated.size)
        assertEquals(RecentConversion("new", "pair"), updated.first())
        assertEquals(RecentConversion("from7", "to7"), updated.last())
    }
}
