package com.example.lab3

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var expressionTextView: TextView
    private lateinit var resultTextView: TextView

    private var currentInput: String = "0"
    private var expression: String = "0"
    private var result: BigDecimal? = null
    private var isResultDisplayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expressionTextView = findViewById(R.id.expressionTextView)
        resultTextView = findViewById(R.id.resultTextView)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.button1_1).setOnClickListener { resetCalculator() }
        findViewById<Button>(R.id.button2_1).setOnClickListener { appendNumber("7") }
        findViewById<Button>(R.id.button2_2).setOnClickListener { appendNumber("8") }
        findViewById<Button>(R.id.button2_3).setOnClickListener { appendNumber("9") }
        findViewById<Button>(R.id.button3_1).setOnClickListener { appendNumber("4") }
        findViewById<Button>(R.id.button3_2).setOnClickListener { appendNumber("5") }
        findViewById<Button>(R.id.button3_3).setOnClickListener { appendNumber("6") }
        findViewById<Button>(R.id.button4_1).setOnClickListener { appendNumber("1") }
        findViewById<Button>(R.id.button4_2).setOnClickListener { appendNumber("2") }
        findViewById<Button>(R.id.button4_3).setOnClickListener { appendNumber("3") }
        findViewById<Button>(R.id.button5_1).setOnClickListener { appendNumber("0") }
        findViewById<Button>(R.id.button5_3).setOnClickListener { appendDecimal() }
        findViewById<Button>(R.id.button1_2).setOnClickListener { toggleSign() }
        findViewById<Button>(R.id.button1_3).setOnClickListener { appendPercent() }
        findViewById<Button>(R.id.button1_4).setOnClickListener { appendOperator("/") }
        findViewById<Button>(R.id.button2_4).setOnClickListener { appendOperator("*") }
        findViewById<Button>(R.id.button3_4).setOnClickListener { appendOperator("-") }
        findViewById<Button>(R.id.button4_4).setOnClickListener { appendOperator("+") }
        findViewById<Button>(R.id.button5_4).setOnClickListener { calculateResult() }
    }

    private fun appendNumber(number: String) {
        if (isResultDisplayed) {
            expression = ""
            expressionTextView.text = ""
            isResultDisplayed = false
        }

        if (currentInput == "0" && number != "0") {
            currentInput = number
            expression = expression.dropLast(1) + number
        } else if (!(currentInput == "0" && number == "0")) {
            currentInput += number
            expression += number
        }

        resultTextView.text = expression.replace(".", ",")
        adjustTextSize()
    }

    private fun appendDecimal() {
        if (isResultDisplayed) {
            expression = ""
            expressionTextView.text = ""
            currentInput = "0,"
            expression = "0,"
            isResultDisplayed = false
        } else if (!currentInput.contains(",")) {
            currentInput += ","
            expression += ","
        }
        resultTextView.text = expression.replace(".", ",")
        adjustTextSize()
    }

    private fun appendOperator(op: String) {
        if (isResultDisplayed) {
            expressionTextView.text = ""
            isResultDisplayed = false
        }

        if (expression.isNotEmpty()) {
            if ("+-*/".contains(expression.last())) {
                expression = expression.dropLast(1) + op
            } else {
                expression += " $op "
            }
        } else if (op == "-") {
            expression = "-"
        }

        currentInput = ""
        resultTextView.text = expression.replace(".", ",")
        adjustTextSize()
    }

    private fun appendPercent() {
        if (currentInput.isNotEmpty() && currentInput != "0") {
            currentInput += "%"
            expression += "%"
            resultTextView.text = expression.replace(".", ",")
            adjustTextSize()
        }
    }

    private fun toggleSign() {
        val parts = expression.trim().split(" ").toMutableList()
        if (parts.isNotEmpty()) {
            val lastIndex = parts.size - 1
            val lastPart = parts[lastIndex]

            if (lastPart.matches(Regex("-?\\d+(,\\d+)?"))) {
                parts[lastIndex] = if (lastPart.startsWith("-")) lastPart.drop(1) else "-${lastPart}"
            } else if (lastIndex > 0 && parts[lastIndex - 1] in listOf("+", "-")) {
                parts[lastIndex - 1] = if (parts[lastIndex - 1] == "+") "-" else "+"
            }

            expression = parts.joinToString(" ")
            resultTextView.text = expression.replace(".", ",")
            adjustTextSize()
        }
    }

    private fun calculateResult() {
        if (!expression.contains(Regex("\\d"))) return

        try {
            val formattedExpression = convertPercent(expression.replace(",", "."))
            val result = evalExpression(formattedExpression)
            this.result = result

            expressionTextView.text = expression.replace(".", ",")
            adjustTextSize()

            resultTextView.text = result.stripTrailingZeros().toPlainString().replace(".", ",")
            adjustTextSize()

            isResultDisplayed = true
        } catch (e: ArithmeticException) {
            expressionTextView.text = "Ошибка: деление на 0"
            adjustTextSize()
        } catch (e: Exception) {
            expressionTextView.text = "Ошибка"
            adjustTextSize()
        }
    }

    private fun convertPercent(expr: String): String {
        return Regex("(\\d+(?:\\.\\d+)?)%").replace(expr) { matchResult ->
            "(${matchResult.groupValues[1]}/100)"
        }
    }

    private fun evalExpression(expression: String): BigDecimal {
        val operators = Stack<String>()
        val values = Stack<BigDecimal>()

        val tokens = expression.split(" ")

        for (token in tokens) {
            when {
                token == "+" || token == "-" || token == "*" || token == "/" -> {
                    while (operators.isNotEmpty() && precedence(token) <= precedence(operators.peek())) {
                        values.push(applyOperation(operators.pop(), values.pop(), values.pop()))
                    }
                    operators.push(token)
                }
                token.isNotBlank() -> values.push(BigDecimal(token))
            }
        }

        while (operators.isNotEmpty()) {
            values.push(applyOperation(operators.pop(), values.pop(), values.pop()))
        }

        return values.pop()
    }

    private fun precedence(operator: String): Int = when (operator) {
        "+", "-" -> 1
        "*", "/" -> 2
        else -> 0
    }

    private fun applyOperation(operator: String, right: BigDecimal, left: BigDecimal): BigDecimal = when (operator) {
        "+" -> left.add(right)
        "-" -> left.subtract(right)
        "*" -> left.multiply(right)
        "/" -> if (right == BigDecimal.ZERO) throw ArithmeticException("Division by zero") else left.divide(right, 10, RoundingMode.HALF_UP)
        else -> BigDecimal.ZERO
    }

    private fun adjustTextSize() {
        resultTextView.textSize = when (resultTextView.text.length) {
            in 0..6 -> 80f
            in 7..12 -> 56f
            else -> 36f
        }
    }

    private fun resetCalculator() {
        currentInput = "0"
        expression = "0"
        result = null
        expressionTextView.text = ""
        adjustTextSize()
        resultTextView.text = "0"
        resultTextView.textSize = 80f
        isResultDisplayed = false
    }
}
