package com.example.lab3

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.HorizontalScrollView
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
//        findViewById<HorizontalScrollView>(R.id.resultScrollView).setOnTouchListener { v, event ->
//            var initialX = 0f
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    initialX = event.x
//                }
//                MotionEvent.ACTION_UP -> {
//                    val deltaX = initialX - event.x
//                    if (deltaX > 0 && isScrolledToStart(v as HorizontalScrollView)) {
//                        // Ваш код для смахивания влево, если скролл на 0
//                        deleteSymbol()
//                    }
//                }
//            }
//            false
//        }

        fun isScrolledToStart(scrollView: HorizontalScrollView): Boolean {
            return scrollView.scrollX == 0
        }

        fun performActionOnSwipeLeft() {
            // Ваш код для действия при смахивании влево
            Log.d("Swipe", "Смахивание влево с позиции 0")
        }


    }

    private fun appendNumber(number: String) {
        if (isResultDisplayed) {
            expression = ""
            isResultDisplayed = false
        }
        appendExpression("")

        if (currentInput == "0" && number != "0") {
            currentInput = number
            expression = expression.dropLast(1) + number
        } else if (!(currentInput == "0" && number == "0")) {
            currentInput += number
            expression += number
        }

        appendResult(expression)
    }

    private fun appendDecimal() {
        if (isResultDisplayed) {
            expression = ""
            currentInput = "0."
            expression = "0."
            isResultDisplayed = false
        } else if (!currentInput.contains(".")) {
            currentInput += "."
            expression += "."
        }
        appendExpression("")
        appendResult(expression)
    }

    private fun appendOperator(op: String) {
        if (isResultDisplayed) {
            currentInput = expression
            appendResult(expression)
            isResultDisplayed = false
        }

        // Если currentInput равно "0" и нажата кнопка "-", начинаем с "-"
        if (currentInput == "0" && op == "-") {
            currentInput = "-"
            expression = "-"
            appendResult(expression)
            return
        }
        if (expression.last() != '-'){
            val expressionLast = expression.trim().last()
            if (expression.isNotEmpty() && "+-*/".contains(expressionLast)) {
                if (expressionLast == '*' || expressionLast == '/') {
                    if (op == "-") {
                        expression += op
                    } else {
                        expression = expression.trim().dropLast(1) + "$op "
                    }
                } else {
                    expression = expression.trim().dropLast(1) + "$op "
                }
                appendResult(expression)
                return
            }
        }
        else return

        appendExpression("")
        currentInput = ""
        expression += " $op "
        appendResult(expression)
    }


    private fun appendPercent() {
        if (isResultDisplayed) {
            currentInput = expression
            appendResult(expression)
            isResultDisplayed = false
        }
        if (currentInput.isNotEmpty() && currentInput != "0") {
            currentInput += "%"
            expression += "%"
            appendResult(expression)
        }
        appendExpression("")
    }

    private fun toggleSign() {
        val parts = expression.trim().split(" ").toMutableList()
        appendExpression("")

        if (parts.isNotEmpty()) {
            val lastIndex = parts.size - 1
            val lastPart = parts[lastIndex]

            if (lastPart.matches(Regex("-?\\d+(\\.\\d+)?"))) {
                parts[lastIndex] = if (lastPart.startsWith("-")) lastPart.drop(1) else "-${lastPart}"
                expression = parts.joinToString(" ")
                appendResult(expression)
            }
        }
    }


    private fun calculateResult() {
        if (expression.trim().isEmpty() || currentInput.trim().isEmpty() || !expression.contains(" ")) {
            if (!(expression.contains(Regex("\\d+%")) && !expression.contains(Regex("[+\\-*/]")))) return
        }

        try {
            val formattedExpression = convertPercent(expression)
            val result = evalExpression(formattedExpression)
            this.result = result

            appendExpression(expression)
            expression = result.stripTrailingZeros().toPlainString()

            appendResult(expression)

            isResultDisplayed = true
        } catch (e: ArithmeticException) {
            appendExpression("Ошибка: деление на 0")
        } catch (e: Exception) {
            appendExpression("Ошибка")
        }
    }

    private fun convertPercent(expr: String): String {
        return Regex("(\\d+(?:\\.\\d+)?)%").replace(expr) { matchResult ->
            "${matchResult.groupValues[1].toFloat() / 100}"
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

    private fun appendResult(result: String) {
        resultTextView.text = result.replace(".", ",").replace("*", "×").replace("/", "÷")
        resultTextView.textSize = when (result.length) {
            in 0..6 -> 80f
            in 7..12 -> 56f
            else -> 36f
        }
    }
    private fun appendExpression(text: String) {
        expressionTextView.text = text.replace(".", ",").replace("*", "×").replace("/", "÷")
    }

    private fun resetCalculator() {
        currentInput = "0"
        expression = "0"
        result = null
        appendExpression("")
        appendResult("0")
        isResultDisplayed = false
    }

    private fun deleteSymbol(){
    }
}
