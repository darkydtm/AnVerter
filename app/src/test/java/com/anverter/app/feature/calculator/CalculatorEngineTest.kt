package com.anverter.app.feature.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculatorEngineTest {

    private fun eval(expr: String) = CalculatorEngine.evaluate(expr)

    @Test
    fun `respects operator precedence`() {
        assertEquals(14.0, eval("2 + 3 * 4"), 1e-9)
    }

    @Test
    fun `honors parentheses`() {
        assertEquals(20.0, eval("(2 + 3) * 4"), 1e-9)
    }

    @Test
    fun `handles unary minus`() {
        assertEquals(-5.0, eval("-2 - 3"), 1e-9)
        assertEquals(6.0, eval("-2 * -3"), 1e-9)
        assertEquals(5.0, eval("3 - -2"), 1e-9)
    }

    @Test
    fun `handles decimals and unicode operators`() {
        assertEquals(7.5, eval("2.5 × 3"), 1e-9)
        assertEquals(2.5, eval("5 ÷ 2"), 1e-9)
        assertEquals(1.0, eval("0,5 + 0,5"), 1e-9)
    }

    @Test
    fun `nested parentheses`() {
        assertEquals(2.0, eval("((1 + 1))"), 1e-9)
        assertEquals(9.0, eval("3 * (1 + (4 - 2))"), 1e-9)
    }

    @Test
    fun `division by zero throws`() {
        assertThrows(CalculatorEngine.CalculatorException::class.java) { eval("1 / 0") }
    }

    @Test
    fun `mismatched parentheses throws`() {
        assertThrows(CalculatorEngine.CalculatorException::class.java) { eval("(1 + 2") }
        assertThrows(CalculatorEngine.CalculatorException::class.java) { eval("1 + 2)") }
    }

    @Test
    fun `empty expression throws`() {
        assertThrows(CalculatorEngine.CalculatorException::class.java) { eval("   ") }
    }

    @Test
    fun `evaluateOrNull swallows errors`() {
        assertNull(CalculatorEngine.evaluateOrNull("1 +"))
        assertEquals(3.0, CalculatorEngine.evaluateOrNull("1 + 2")!!, 1e-9)
    }

    @Test
    fun `formats results without trailing zeros`() {
        assertEquals("4", formatCalcResult(eval("2 + 2")))
        assertEquals("2.5", formatCalcResult(eval("5 / 2")))
    }
}
