package com.example.personalcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.personalcalculator.databinding.ActivityMainBinding
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val pi: String = "3.14159265359"
    private val e: String = "2.718281828459045"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    enum class MathOperation {
        NONE, SIN, COS, TAN, aSIN, aCOS, aTAN, FCT,     // no second number needed
        PLUS, MINUS, MULTIPLY, DIVIDE, POWER, ROOT, LOG;     // second number needed
    }

    private var numString1: StringBuilder = StringBuilder(70)
    private var num1Delimited: Boolean = false

    private var numString2: StringBuilder = StringBuilder(70)
    private var num2Delimited: Boolean = false

    private var mathOperation = MathOperation.NONE
    private var inverseModifier: Boolean = false

    private var result: Double = 0.0

    fun numberButtonClicked(view: View?) {
        if (view is Button) {
            val pressedButton: Button = view
            // if typing first number
            if (mathOperation.ordinal < 8) {
                numString1.append(pressedButton.text.toString())
            } else {    // if typing second number
                numString2.append(pressedButton.text.toString())
            }
        }
        updateDisplay()
    }

    fun constantButtonClicked(view: View?) {
        if (view is Button) {
            val pressedButton: Button = view
            // if typing first number
            val constant: String = if (pressedButton == binding.buttonPi) { pi } else { e }
            if (mathOperation.ordinal < 8) {
                numString1.replace(0, numString1.length, constant)
            } else {    // if typing second number
                numString1.replace(0, numString1.length, constant)
            }
        }
        updateDisplay()
    }

    fun operatorButtonClicked(view: View?) {
        val selectedOperation = when(view as Button) {
            binding.buttonPlus -> MathOperation.PLUS
            binding.buttonMinus -> MathOperation.MINUS
            binding.buttonMultiply -> MathOperation.MULTIPLY
            binding.buttonDivide -> MathOperation.DIVIDE
            binding.buttonPower -> MathOperation.POWER
            binding.buttonRoot -> MathOperation.ROOT
            binding.buttonFactorial -> MathOperation.FCT
            binding.buttonSin -> { if (inverseModifier) { MathOperation.aSIN } else { MathOperation.SIN } }
            binding.buttonCos -> { if (inverseModifier) { MathOperation.aCOS } else { MathOperation.COS } }
            binding.buttonTan -> { if (inverseModifier) { MathOperation.aTAN } else { MathOperation.TAN } }
            // TODO: logarithms will be implemented later
            //binding.buttonLog -> MathOperation.LOG
            else -> MathOperation.NONE
        }

        // single number operations should be type-able immediately
        if (numString1.isNotEmpty() or (selectedOperation.ordinal < 8)) {
            // calculate written input if a second number was already typed
            if (numString2.isNotEmpty()) {
                calculateButtonClicked(view)
            }
            // set operation
            mathOperation = selectedOperation
        }
        updateDisplay()
    }

    fun delimiterButtonClicked(view: View?) {
        if (mathOperation == MathOperation.NONE) {
            if (!num1Delimited) {
                numString1.append(".")
                num1Delimited = true
            }
        } else if (mathOperation != MathOperation.ROOT) {
            if (!num2Delimited) {
                numString2.append(".")
                num2Delimited = true
            }
        }
        updateDisplay()
    }

    fun correctionButtonClicked(view: View?) {
        if ((mathOperation == MathOperation.NONE) and (numString1.isNotEmpty())) {
            if (numString1.last() == '.') num1Delimited = false
            numString1 = numString1.delete(numString1.length - 1, numString1.length)
        } else if (numString2.isEmpty()) {
            mathOperation = MathOperation.NONE
        } else {
            if (numString2.last() == '.') num2Delimited = false
            numString2 = numString2.delete(numString2.length - 1, numString2.length)
        }
        updateDisplay()
    }

    fun clearButtonClicked(view: View) {
        numString1.delete(0, numString1.length)
        num1Delimited = false

        numString2.delete(0, numString2.length)
        num2Delimited = false

        mathOperation = MathOperation.NONE

        inverseModifier = false
        setTrigInverseText(inverseModifier)

        updateDisplay()
    }

    fun inverseButtonClicked(view: View?) {
        inverseModifier = !inverseModifier
        setTrigInverseText(inverseModifier)
    }

    fun calculateButtonClicked(view: View?) {
        if (mathOperation == MathOperation.NONE) { return }

        result = when(mathOperation) {
            MathOperation.PLUS -> { numString1.toString().toDouble() + numString2.toString().toDouble() }

            MathOperation.MINUS -> { numString1.toString().toDouble() - numString2.toString().toDouble() }

            MathOperation.MULTIPLY -> { numString1.toString().toDouble() * numString2.toString().toDouble() }

            MathOperation.DIVIDE -> { numString1.toString().toDouble() / numString2.toString().toDouble() }

            MathOperation.POWER -> { numString1.toString().toDouble().pow(numString2.toString().toDouble()) }

            MathOperation.ROOT -> { xRoot(numString1.toString().toDouble(), numString2.toString().toInt()) }

            // TODO: add option to use degrees instead of radians; probably a button at the top + trig function wrappers + toggle variable(s)

            MathOperation.SIN -> { sin(numString1.toString().toDouble()) }

            MathOperation.COS -> { cos(numString1.toString().toDouble()) }

            MathOperation.TAN -> { tan(numString1.toString().toDouble()) }

            MathOperation.aSIN -> { asin(numString1.toString().toDouble()) }

            MathOperation.aCOS -> { acos(numString1.toString().toDouble()) }

            MathOperation.aTAN -> { atan(numString1.toString().toDouble()) }

            MathOperation.FCT -> { factorial(numString1.toString().toDouble()) }

            // do nothing
            else -> result
        }
        numString1.replace(0, numString1.length, result.toString())
        numString2.delete(0, numString2.length)// end parameter is exclusive
        mathOperation = MathOperation.NONE
        num1Delimited = numString1.contains('.')
        num2Delimited = false
        updateDisplay()
    }

    private fun setTrigInverseText(inverse: Boolean) {
        if (inverse) {
            binding.buttonSin.text = "a-sin"
            binding.buttonCos.text = "a-cos"
            binding.buttonTan.text = "a-tan"
        }
        else {
            binding.buttonSin.text = "sin"
            binding.buttonCos.text = "cos"
            binding.buttonTan.text = "tan"
        }
    }

    // TODO: switch to view binding
    private fun updateDisplay() {
        when(mathOperation) {
            MathOperation.PLUS ->
                binding.calculatorDisplay.text = getString(R.string._pls, numString1, numString2)

            MathOperation.MINUS ->
                binding.calculatorDisplay.text = getString(R.string._mns, numString1, numString2)

            MathOperation.MULTIPLY ->
                binding.calculatorDisplay.text = getString(R.string._mlt, numString1, numString2)

            MathOperation.DIVIDE ->
                binding.calculatorDisplay.text = getString(R.string._div, numString1, numString2)

            MathOperation.POWER ->
                binding.calculatorDisplay.text = getString(R.string._pow, numString1, numString2)

            MathOperation.ROOT ->
                binding.calculatorDisplay.text = getString(R.string._rot, numString1, numString2)

            MathOperation.SIN ->
                binding.calculatorDisplay.text = getString(R.string._sin, numString1)

            MathOperation.COS ->
                binding.calculatorDisplay.text = getString(R.string._cos, numString1)

            MathOperation.TAN ->
                binding.calculatorDisplay.text = getString(R.string._tan, numString1)

            MathOperation.aSIN ->
                binding.calculatorDisplay.text = getString(R.string._asn, numString1)

            MathOperation.aCOS ->
                binding.calculatorDisplay.text = getString(R.string._acs, numString1)

            MathOperation.aTAN ->
                binding.calculatorDisplay.text = getString(R.string._atn, numString1)

            MathOperation.FCT ->
                binding.calculatorDisplay.text = getString(R.string._fct, numString1)

            else ->
                binding.calculatorDisplay.text = numString1
        }
    }

    // calculates factorial of a positive integer
    private fun factorial(number: Double): Double {
        if ((number < 0.0) or (floor(number) != number)) { return Double.NaN }
        else if (number == 0.0) { return 1.0 }
        // else:
        var multiplier: Double = number - 1.0
        var result: Double = number.toDouble()
        while (multiplier > 1) {
            result *= multiplier
            multiplier--
        }
        return result.toDouble()
    }

    // iterate to find the root
    private fun xRoot(number: Double, rootNumber: Int): Double {
        if ((number == 1.0) or (number == 0.0)) { return number }
        else if (number < 0.0) { return Double.NaN }

        // 1 to num       // 0 to 1
        var minNumber = if (number > 1.0) { 1.0 }         else { Double.MIN_VALUE }
        var maxNumber = if (number > 1.0) { number }      else { 1.0 }

        var result: Double

        var iterationNum = (minNumber + maxNumber) / 2
        var iterationCount = 0
        do {
            // try an iterationNumber:
            result = iterationNum
            for (i in 2..rootNumber) {
                result *= iterationNum
            }
            // compare the iterationNumber to power, to desired number
            if (result > number) {
                maxNumber = iterationNum
            }
            else if (result < number) {
                minNumber = iterationNum
            }
            iterationNum = (maxNumber + minNumber) / 2
            iterationCount++
        }
        while (((result - number).absoluteValue > Double.MIN_VALUE) and (iterationCount < 100))
        println(iterationCount)
        return iterationNum
    }
}