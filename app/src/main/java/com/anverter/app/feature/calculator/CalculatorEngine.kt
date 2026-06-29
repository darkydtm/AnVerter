package com.anverter.app.feature.calculator

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure infix expression evaluator (shunting-yard). Supports + - * / with parentheses,
 * decimals and unary minus. Android-free, fully unit-testable.
 */
object CalculatorEngine {

    class CalculatorException(message: String) : Exception(message)

    private sealed interface Token {
        data class Num(val value: Double) : Token
        data class Op(val symbol: Char, val precedence: Int, val rightAssoc: Boolean, val unary: Boolean) : Token
        data object LParen : Token
        data object RParen : Token
    }

    fun evaluate(expression: String): Double {
        val normalized = expression
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(',', '.')
        val tokens = tokenize(normalized)
        if (tokens.isEmpty()) throw CalculatorException("Empty expression")
        return evalRpn(toRpn(tokens))
    }

    /** Convenience: returns null instead of throwing, for live previews. */
    fun evaluateOrNull(expression: String): Double? =
        runCatching { evaluate(expression) }.getOrNull()

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++

                c.isDigit() || c == '.' -> {
                    val start = i
                    var dots = if (c == '.') 1 else 0
                    i++
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                        if (input[i] == '.' && ++dots > 1) throw CalculatorException("Malformed number")
                        i++
                    }
                    val text = input.substring(start, i)
                    val value = text.toDoubleOrNull() ?: throw CalculatorException("Malformed number: $text")
                    tokens += Token.Num(value)
                }

                c == '(' -> { tokens += Token.LParen; i++ }
                c == ')' -> { tokens += Token.RParen; i++ }

                c == '+' || c == '-' || c == '*' || c == '/' -> {
                    val prev = tokens.lastOrNull()
                    val isUnary = prev == null || prev is Token.Op || prev is Token.LParen
                    when {
                        isUnary && c == '-' -> tokens += Token.Op('u', 4, rightAssoc = true, unary = true)
                        isUnary && c == '+' -> { /* unary plus is a no-op */ }
                        isUnary -> throw CalculatorException("Unexpected operator: $c")
                        c == '+' || c == '-' -> tokens += Token.Op(c, 2, rightAssoc = false, unary = false)
                        else -> tokens += Token.Op(c, 3, rightAssoc = false, unary = false)
                    }
                    i++
                }

                else -> throw CalculatorException("Unexpected character: $c")
            }
        }
        return tokens
    }

    private fun toRpn(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()
        for (token in tokens) {
            when (token) {
                is Token.Num -> output += token
                is Token.Op -> {
                    while (true) {
                        val top = stack.lastOrNull()
                        if (top is Token.Op &&
                            (top.precedence > token.precedence ||
                                (top.precedence == token.precedence && !token.rightAssoc))
                        ) {
                            output += stack.removeLast()
                        } else {
                            break
                        }
                    }
                    stack.addLast(token)
                }
                Token.LParen -> stack.addLast(token)
                Token.RParen -> {
                    while (stack.lastOrNull() != null && stack.last() != Token.LParen) {
                        output += stack.removeLast()
                    }
                    if (stack.lastOrNull() != Token.LParen) throw CalculatorException("Mismatched parentheses")
                    stack.removeLast()
                }
            }
        }
        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top == Token.LParen || top == Token.RParen) throw CalculatorException("Mismatched parentheses")
            output += top
        }
        return output
    }

    private fun evalRpn(rpn: List<Token>): Double {
        val stack = ArrayDeque<Double>()
        for (token in rpn) {
            when (token) {
                is Token.Num -> stack.addLast(token.value)
                is Token.Op -> {
                    if (token.unary) {
                        val a = stack.removeLastOrNull() ?: throw CalculatorException("Invalid expression")
                        stack.addLast(-a)
                    } else {
                        val b = stack.removeLastOrNull() ?: throw CalculatorException("Invalid expression")
                        val a = stack.removeLastOrNull() ?: throw CalculatorException("Invalid expression")
                        stack.addLast(apply(token.symbol, a, b))
                    }
                }
                else -> throw CalculatorException("Invalid expression")
            }
        }
        if (stack.size != 1) throw CalculatorException("Invalid expression")
        return stack.single()
    }

    private fun apply(symbol: Char, a: Double, b: Double): Double = when (symbol) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> if (b == 0.0) throw CalculatorException("Division by zero") else a / b
        else -> throw CalculatorException("Unknown operator: $symbol")
    }
}

/** Formats a calculator result: integers without a fraction, otherwise trimmed decimals. */
fun formatCalcResult(value: Double): String {
    if (!value.isFinite()) return ""
    val rounded = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
    return rounded.toPlainString()
}
