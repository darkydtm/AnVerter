package com.anverter.app.data

import com.anverter.app.data.remote.RatesParser
import com.anverter.app.domain.model.CurrencyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RatesParserTest {

    private val sample = """
        {
          "rates": {
            "btc": { "name": "Bitcoin", "unit": "BTC", "value": 1.0, "type": "crypto" },
            "usd": { "name": "US Dollar", "unit": "$", "value": 60000.0, "type": "fiat" },
            "eur": { "name": "Euro", "unit": "€", "value": 50000.0, "type": "fiat" },
            "xau": { "name": "Gold - Troy Ounce", "unit": "XAU", "value": 25.0, "type": "commodity" },
            "future": { "name": "Unknown", "unit": "Z", "value": 7.0, "type": "brand-new-type" }
          }
        }
    """.trimIndent()

    @Test
    fun `parses all rates with mapped types`() {
        val rates = RatesParser.parse(sample)

        assertEquals(5, rates.size)
        assertEquals(60000.0, rates.getValue("usd").value, 1e-9)
        assertEquals(CurrencyType.CRYPTO, rates.getValue("btc").type)
        assertEquals(CurrencyType.FIAT, rates.getValue("eur").type)
        assertEquals(CurrencyType.COMMODITY, rates.getValue("xau").type)
    }

    @Test
    fun `unknown type falls back to commodity`() {
        val rates = RatesParser.parse(sample)
        assertEquals(CurrencyType.COMMODITY, rates.getValue("future").type)
    }

    @Test
    fun `ignores unknown json keys`() {
        val withExtra = """{ "rates": { "usd": { "name": "US Dollar", "unit": "$", "value": 1.0, "type": "fiat", "extra": 123 } }, "meta": "x" }"""
        val rates = RatesParser.parse(withExtra)
        assertTrue(rates.containsKey("usd"))
    }
}
