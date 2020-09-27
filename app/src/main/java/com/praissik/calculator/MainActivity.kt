package com.praissik.calculator

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.mariuszgromada.math.mxparser.Expression
import kotlin.math.pow


class MainActivity : Activity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        history.movementMethod = ScrollingMovementMethod()
        all.movementMethod = ScrollingMovementMethod()

        one.setOnClickListener { num('1') }
        two.setOnClickListener { num('2') }
        three.setOnClickListener { num('3') }
        four.setOnClickListener { num('4') }
        five.setOnClickListener { num('5') }
        six.setOnClickListener { num('6') }
        seven.setOnClickListener { num('7') }
        eight.setOnClickListener { num('8') }
        nine.setOnClickListener { num('9') }
        zero.setOnClickListener { num('0') }
        dot.setOnClickListener { dot() }
        power.setOnClickListener { power() }
        percent.setOnClickListener { percent() }
        backspace.setOnClickListener { backspace() }
        plus.setOnClickListener { operator('+') }
        minus.setOnClickListener { operator('-') }
        multiply.setOnClickListener { operator('*') }
        divide.setOnClickListener { operator('/') }
        clearDigit.setOnClickListener { clearDigit() }
        clearEverything.setOnClickListener { clearEverything() }
        calculate.setOnClickListener { calculateExpression() }
    }

    override fun onStart() {
        super.onStart()

        sharedPref = getPreferences(MODE_PRIVATE)
        history.text = sharedPref.getString("history", "")
        all.text = sharedPref.getString("all", "")
        digit.text = sharedPref.getString("digit", "")

        if (digit.text.isNotEmpty()) {
            clearEverything.visibility = TextView.GONE
            clearDigit.visibility = TextView.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()

        sharedPref.edit()
            .putString("history", history.text.toString())
            .putString("all", all.text.toString())
            .putString("digit", digit.text.toString())
            .apply()
    }

    private fun checkResult(result: String) : String {
        if (result.indexOf('.') == result.length - 2 && result.last() == '0') {
            return result.substring(0..result.length-3)
        }
        return result
    }

    private fun scroll() {
        history.post {
            val scrollAmount = history.layout.getLineTop(history.lineCount) - history.height
            println(history.height)
            if (scrollAmount >= -10) {
                when {
                    history.height > 800 -> {
                        history.scrollTo(0, scrollAmount+90)
                    }
                    history.height > 500 -> {
                        history.scrollTo(0, scrollAmount+75)
                    }
                    else -> {
                        history.scrollTo(0, scrollAmount+50)
                    }
                }
            }
        }

        all.post {
            val scrollAmount = all.layout.getLineTop(all.lineCount) - all.height
            if (scrollAmount >= -10) {
                all.scrollTo(0, scrollAmount)
            }
        }
    }

    private fun intIfMay(digit: String) : String {
        if (digit.endsWith(".0")){
            return digit.take(digit.length-3)
        }
        return digit
    }

    private fun calculateExpression() {
        if (all.text.isNotEmpty()) {
            if (digit.text.isEmpty()) {
                digit.text = "0"
            }
            if (all.text.toString().last() == '/' && digit.text.toString().toDouble() == 0.0) {
                Toast.makeText(this, "Can't divide by zero...", Toast.LENGTH_SHORT).show()
            } else if (digit.text.first() != '=' && digit.text.last().isDigit()) {

                val expString = all.text.toString() + digit.text.toString()
                val expression = Expression(expString)

                val result = expression.calculate().toString()

                digit.text = "= ".plus(checkResult(result))
                all.text = expString
                scroll()
            }
        }
    }

    private fun clearDigit() {
        if (digit.text.isEmpty()) {
            digit.text = "0"
        }
        when {
            digit.text.first() != '=' -> {
                digit.text = ""
                all.text = ""
                clearDigit.visibility = TextView.GONE
                clearEverything.visibility = TextView.VISIBLE
            } else -> {
                digit.text = all.text.takeLastWhile { it.isDigit() || it == '.' }
                all.text = all.text.dropLast(digit.text.toString().length)
            }
        }
    }

    private fun clearEverything() {
        all.text = ""
        history.text = ""
    }

    private fun operator(symbol: Char) {
        if (digit.text.isEmpty()) {
            digit.text = "0"
        }
        if (all.text.toString().isNotEmpty() && all.text.toString().last() == '/' && digit.text.toString().toDouble() == 0.0) {
            Toast.makeText(this, "Can't divide by zero...", Toast.LENGTH_SHORT).show()
        } else if (digit.text.isNotEmpty() && digit.text.last().isDigit()) {
            if (digit.text.first() == '=') {
                history.text = history.text.toString().plus("\n\n").plus(all.text.toString()).plus("\n")
                                        .plus(all.text.toString()).plus(" " + digit.text.toString())
                all.text = digit.text.toString().drop(2).plus(symbol)
            } else {
                all.text = all.text.toString().plus(digit.text.toString()).plus(symbol)
            }
            digit.text = ""
            clearEverything.visibility = TextView.GONE
            clearDigit.visibility = TextView.VISIBLE
            scroll()
        }
    }

    private fun backspace() {
        if (digit.text.isNotEmpty()) {
            when {
                digit.text.first() == '=' -> {
                    history.text = history.text.toString().plus("\n\n").plus(all.text.toString()).plus("\n")
                                            .plus(all.text.toString()).plus(" " + digit.text.toString())
                    digit.text = digit.text.drop(2).dropLast(1)
                    all.text = ""
                }
                digit.text.length == 1 && all.text.isNotEmpty() -> {
                    digit.text = all.text.dropLast(1).takeLastWhile { it.isDigit() || it == '.' }
                    all.text = all.text.dropLast(digit.length() + 1)
                }
                digit.text.length == 1 -> {
                    digit.text = ""
                    clearEverything.visibility = TextView.VISIBLE
                    clearDigit.visibility = TextView.GONE
                }
                else -> {
                    digit.text = digit.text.dropLast(1)
                }
            }
        }
    }

    private fun percent() {
        if (digit.text.isEmpty()) {
            digit.text = "0"
        }
        if (digit.text.first() == '=') {
            history.text = history.text.toString().plus("\n\n").plus(all.text.toString()).plus("\n")
                                    .plus(all.text.toString()).plus(" " + digit.text.toString())
            all.text = ""
            digit.text = digit.text.drop(2)
        }
        if (digit.text.toString().toDouble() != 0.0 && digit.text.last().isDigit()) {
            digit.text = intIfMay((digit.text.toString().toDouble()/100).toString())
        }
    }

    private fun power() {
        if (digit.text.isEmpty()) {
            digit.text = "0"
        }
        if (digit.text.first() == '=') {
            history.text = history.text.toString().plus("\n\n").plus(all.text.toString()).plus("\n")
                                    .plus(all.text.toString()).plus(" " + digit.text.toString())
            all.text = ""
            digit.text = digit.text.drop(2)
        }
        if (digit.text.last().isDigit()) {
            val digitString = digit.text.toString()
            var digitDouble = digitString.toDouble()
            if (digitString.contains('.')) {
                val exponent = digitString.length - digitString.indexOf('.') - 1
                digitDouble *= 10.0.pow(exponent)
                digitDouble *= digitDouble
                digitDouble /= 10.0.pow(exponent*2)
                digit.text = digitDouble.toString()
            } else {
                digit.text = digitDouble.pow(2).toInt().toString()
            }
        }
    }

    private fun dot() {
        if (digit.text.isEmpty()) {
            digit.text = "0."
            clearEverything.visibility = TextView.GONE
            clearDigit.visibility = TextView.VISIBLE
        } else if (digit.text.first() == '=') {
            history.text = history.text.toString().plus("\n\n").plus(all.text.toString()).plus("\n")
                                    .plus(all.text.toString()).plus(" " + digit.text.toString())
            digit.text = digit.text.drop(2)
            all.text = ""
        }
        if (digit.text.last().isDigit() && !digit.text.toString().contains('.')) {
            digit.text = digit.text.toString().plus('.')
            clearEverything.visibility = TextView.GONE
            clearDigit.visibility = TextView.VISIBLE
        }
        scroll()
    }

    private fun num(num: Char) {
        when {
            digit.text.toString() == "0" || digit.text.isEmpty() -> {
                digit.text = num.toString()
            } digit.text.first() == '=' -> {
                history.text = history.text.toString().plus("\n\n").plus(all.text.toString()).plus("\n")
                                        .plus(all.text.toString()).plus(" " + digit.text.toString())
                all.text = ""
                digit.text = num.toString()
            }
            digit.text.length < 12 -> {
                digit.text = digit.text.toString().plus(num)
            }
        }
        clearEverything.visibility = TextView.GONE
        clearDigit.visibility = TextView.VISIBLE
        scroll()
    }
}
