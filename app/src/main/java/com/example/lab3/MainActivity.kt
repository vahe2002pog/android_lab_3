package com.example.lab3

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import android.view.GestureDetector


class MainActivity : AppCompatActivity() {

    private lateinit var expressionTextView: TextView
    private lateinit var resultTextView: TextView
    private lateinit var resultScrollView: HorizontalScrollView
    private lateinit var gestureDetector: GestureDetector

    private var currentInput: String = "0"
    private var expression: String = "0"
    private var result: BigDecimal? = null
    private var isResultDisplayed = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultScrollView = findViewById(R.id.resultScrollView)

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 200
            private val SWIPE_VELOCITY_THRESHOLD = 200
            private var isScrollAtZero = false

            override fun onDown(e: MotionEvent): Boolean {
                isScrollAtZero = resultScrollView.scrollX == 0
                return super.onDown(e)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && e2 != null && isScrollAtZero) {
                    val diffX = e2.x - e1.x
                    val diffY = e2.y - e1.y

                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                deleteSymbol()
                                return true
                            }
                        }
                    }
                }
                return false
            }
        })

        resultScrollView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }


        expressionTextView = findViewById(R.id.expressionTextView)
        resultTextView = findViewById(R.id.resultTextView)

        setupButtonListeners()
        setButtonSize()
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

    private fun setButtonSize(){
        val button4_1 = findViewById<Button>(R.id.button4_1)
        val button4_2 = findViewById<Button>(R.id.button4_2)
        val button5_1 = findViewById<Button>(R.id.button5_1)

        button4_1.post {
            val location4_1 = IntArray(2)
            button4_1.getLocationOnScreen(location4_1)
            val width4_1 = button4_1.width

            val location4_2 = IntArray(2)
            button4_2.getLocationOnScreen(location4_2)
            val width4_2 = button4_2.width

            val distanceBetweenButtons = location4_2[0] - (location4_1[0] + width4_1)

            val totalWidth = width4_1 + width4_2 + distanceBetweenButtons
            val layoutParams = button5_1.layoutParams
            layoutParams.width = totalWidth
            button5_1.layoutParams = layoutParams
        }
    }

    private fun appendNumber(number: String) {
        if (isResultDisplayed) {
            expression = ""
            currentInput = ""
            isResultDisplayed = false
        }
        appendExpression("")

        if (currentInput == "0" && number != "0") {
            currentInput = number
            expression = expression.dropLast(1) + number
        } else if (!(currentInput == "0" && number == "0") && !currentInput.contains("%")) {
            currentInput += number
            expression += number
        }

        appendResult(expression)
    }

    private fun appendDecimal() {
        if (isResultDisplayed) {
            isResultDisplayed = false
        }
        if (!currentInput.contains(".") && !currentInput.contains("%") && currentInput.contains(Regex("\\d+"))) {
            currentInput += "."
            expression += "."
            appendExpression("")
        }
        appendResult(expression)
    }

    private fun appendOperator(op: String) {
        if (isResultDisplayed) {
            isResultDisplayed = false
        }

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
            isResultDisplayed = false
        }
        if (currentInput.isNotEmpty() && currentInput != "0"  && !currentInput.contains("%")  && currentInput.contains(Regex("\\d+"))) {
            currentInput += "%"
            expression += "%"
            appendResult(expression)
        }
        appendExpression("")
    }

    private fun toggleSign() {
        if (currentInput != "0"){
            val parts = expression.trim().split(" ").toMutableList()

            if (parts.isNotEmpty()) {
                val lastIndex = parts.size - 1
                val lastPart = parts[lastIndex]

                if (lastPart.matches(Regex("-?\\d+(\\.\\d*)?%?"))) {
                    parts[lastIndex] = if (lastPart.startsWith("-")) lastPart.drop(1) else "-${lastPart}"
                    expression = parts.joinToString(" ")
                    appendResult(expression)
                }
            }
        }
    }


    private fun calculateResult() {
        if (expression.trim().isEmpty() || currentInput.trim().isEmpty() || !expression.contains(" ")) {
            if (!(expression.contains(Regex("-?\\d+(\\.\\d*)?%")) && !expression.contains(Regex("(?<=.)[+\\-*/]")))) return
        }

        try {
            val formattedExpression = convertPercent(expression)
            val result = evalExpression(formattedExpression)
            this.result = result

            appendExpression(expression)
            expression = result.stripTrailingZeros().toPlainString()
            currentInput = expression

            appendResult(expression)

            isResultDisplayed = true
        } catch (e: ArithmeticException) {
            appendExpression("Ошибка: деление на 0")
        } catch (e: Exception) {
            appendExpression("Ошибка")
        }
    }

    private fun convertPercent(expr: String): String {
        return Regex("(\\d+(?:\\.\\d*)?)%").replace(expr) { matchResult ->
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
        resultTextView.textSize = when (result.replace(" ", "").length) {
            in 0..5 -> 80f
            in 6..10 -> 56f
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
        isResultDisplayed = false
        appendExpression("")

        if(expression.length == 1){
            expression = "0"
            currentInput = "0"
            result = null
        }
        else{
            if (expression.last() != ' '){
                expression = expression.dropLast(1)
                currentInput = expression.split(" ").last()
            }
            else
            {
                expression = expression.dropLast(3)
                currentInput = expression.split(" ").last()
            }
        }
        appendResult(expression)
    }
}
