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
        findViewById<Button>(R.id.button1_1).setOnClickListener { appendNumber("AC") }
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
        findViewById<Button>(R.id.button1_2).setOnClickListener { appendOperator("±") }
        findViewById<Button>(R.id.button1_3).setOnClickListener { appendOperator("%") }
        findViewById<Button>(R.id.button1_4).setOnClickListener { appendOperator("/") }
        findViewById<Button>(R.id.button2_4).setOnClickListener { appendOperator("*") }
        findViewById<Button>(R.id.button3_4).setOnClickListener { appendOperator("-") }
        findViewById<Button>(R.id.button4_4).setOnClickListener { appendOperator("+") }
        findViewById<Button>(R.id.button5_4).setOnClickListener { calculateResult() }
    }

    private fun appendNumber(number: String) {
        if (isResultDisplayed) {
            expressionTextView.text = ""
            expression = ""
            currentInput = ""
            result = null
            isResultDisplayed = false
        }

        if (number == "AC") {
            resetCalculator()
            return
        }

        if (currentInput == "0" && number != "0") {
            currentInput = number
            expression = if (expression == "0") number else expression.dropLast(1) + number
        } else if (!(currentInput == "0" && number == "0")) {
            currentInput += number
            expression += number
        }

        resultTextView.text = expression.replace(".", ",")
        adjustTextSize()
    }

    private fun appendDecimal() {
        if (isResultDisplayed) {
            expressionTextView.text = ""
            expression = ""
            currentInput = ""
            result = null
            isResultDisplayed = false
        }

        if (!currentInput.contains(",")) {
            currentInput += ","
            expression += ","
            resultTextView.text = expression.replace(".", ",")
            adjustTextSize()
        }
    }

    private fun appendOperator(op: String) {
        if (isResultDisplayed) {
            expressionTextView.text = ""
            expression = result?.stripTrailingZeros()?.toPlainString()?.replace(".", ",") ?: "0"
            currentInput = expression
            resultTextView.text = expression
            isResultDisplayed = false
        }

        // Если введено только "0" и нажата "-", заменяем на "-"
        if (currentInput == "0" && op == "-") {
            currentInput = "-"
            expression = "-"
            resultTextView.text = expression
            adjustTextSize()
            return
        }

        // Предотвращаем ввод подряд идущих операторов
        if (expression.isNotEmpty() && "+-*/".contains(expression.last())) return

        if (op == "±") {
            currentInput = if (currentInput.startsWith("-")) currentInput.substring(1) else "-$currentInput"
            val expressionTokens = expression.trim().split(" ").toMutableList()
            expressionTokens[expressionTokens.size - 1] = currentInput
            expression = expressionTokens.joinToString(" ")
            resultTextView.text = expression.replace(".", ",")
            adjustTextSize()
            return
        }

        currentInput = ""
        expression += "$op"
        resultTextView.text = expression.replace(".", ",")
        adjustTextSize()
    }

    private fun calculateResult() {
        if (expression.trim().isEmpty() || currentInput.trim().isEmpty()) return

        try {
            val expressionWithDots = expression.replace(",", ".")
            val result = evalExpression(expressionWithDots)
            this.result = result

            val formattedResult = result.stripTrailingZeros().toPlainString().replace(".", ",")
            resultTextView.text = formattedResult
            expressionTextView.text = expression.replace(".", ",")

            isResultDisplayed = true
        } catch (e: ArithmeticException) {
            expressionTextView.text = "Ошибка: деление на 0"
            isResultDisplayed = false
        } catch (e: Exception) {
            expressionTextView.text = "Ошибка"
            isResultDisplayed = false
        }
    }

    private fun evalExpression(expression: String): BigDecimal {
        val operators = Stack<String>()
        val values = Stack<BigDecimal>()
        val tokens = expression.split(" ")

        for (token in tokens) {
            when (token) {
                "+", "-", "*", "/" -> {
                    while (operators.isNotEmpty() && precedence(token) <= precedence(operators.peek())) {
                        val operator = operators.pop()
                        val right = values.pop()
                        val left = values.pop()
                        values.push(applyOperation(operator, left, right))
                    }
                    operators.push(token)
                }
                else -> values.push(BigDecimal(token))
            }
        }

        while (operators.isNotEmpty()) {
            val operator = operators.pop()
            val right = values.pop()
            val left = values.pop()
            values.push(applyOperation(operator, left, right))
        }

        return values.pop()
    }

    private fun precedence(operator: String): Int = when (operator) {
        "+", "-" -> 1
        "*", "/" -> 2
        else -> 0
    }

    private fun applyOperation(operator: String, left: BigDecimal, right: BigDecimal): BigDecimal = when (operator) {
        "+" -> left.add(right)
        "-" -> left.subtract(right)
        "*" -> left.multiply(right)
        "/" -> if (right == BigDecimal.ZERO) throw ArithmeticException("Division by zero")
        else left.divide(right, 10, RoundingMode.HALF_UP)
        else -> BigDecimal.ZERO
    }

    private fun adjustTextSize() {
        val length = resultTextView.text.length
        resultTextView.textSize = when {
            length > 12 -> 36f
            length > 6 -> 56f
            else -> 80f
        }
    }

    private fun resetCalculator() {
        currentInput = "0"
        expression = "0"
        result = null
        expressionTextView.text = ""
        resultTextView.text = "0"
        resultTextView.textSize = 80f
        isResultDisplayed = false
    }
}
