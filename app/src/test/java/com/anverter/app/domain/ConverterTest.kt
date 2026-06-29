package com.anverter.app.domain

import com.anverter.app.domain.model.CurrencyRate
import com.anverter.app.domain.model.CurrencyType
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterTest {

    private fun rate(code: String, value: Double, type: CurrencyType) =
        CurrencyRate(code = code, name = code, unit = code, value = value, type = type)

    // 1 BTC = 60000 USD, 1 BTC = 50000 EUR  =>  USD/EUR derived through the BTC anchor.
    private val btc = rate("btc", 1.0, CurrencyType.CRYPTO)
    private val usd = rate("usd", 60_000.0, CurrencyType.FIAT)
    private val eur = rate("eur", 50_000.0, CurrencyType.FIAT)

    @Test
    fun `converts fiat to fiat through btc anchor`() {
        val result = Converter.convert(120.0, from = usd, to = eur)
        assertEquals(100.0, result, 1e-9)
    }

    @Test
    fun `converts fiat to btc`() {
        val result = Converter.convert(60_000.0, from = usd, to = btc)
        assertEquals(1.0, result, 1e-9)
    }

    @Test
    fun `converts btc to fiat`() {
        val result = Converter.convert(2.0, from = btc, to = usd)
        assertEquals(120_000.0, result, 1e-9)
    }

    @Test
    fun `same currency is identity`() {
        assertEquals(42.0, Converter.convert(42.0, from = eur, to = eur), 1e-9)
    }

    @Test
    fun `zero source rate yields zero`() {
        val broken = rate("xxx", 0.0, CurrencyType.FIAT)
        assertEquals(0.0, Converter.convert(10.0, from = broken, to = usd), 1e-9)
    }
}
