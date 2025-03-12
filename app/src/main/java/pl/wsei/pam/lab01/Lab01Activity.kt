package pl.wsei.pam.lab01

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Lab01Activity : AppCompatActivity() {
    private lateinit var mLayout: LinearLayout
    private lateinit var mTitle: TextView
    private lateinit var mProgressBar: ProgressBar
    private val mBoxes: MutableList<CheckBox> = mutableListOf()
    private val mButtons: MutableList<Button> = mutableListOf()
    private var testsPassed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mLayout = findViewById(R.id.main)

        // Tytuł
        mTitle = TextView(this).apply {
            text = "Laboratorium 1"
            textSize = 24f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 20, 20, 20)
            }
        }
        mLayout.addView(mTitle)

        // Utwórz wiersz dla każdego testu: checkbox + przycisk
        for (i in 1..6) {
            val horizontalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            val checkBox = CheckBox(this).apply {
                text = "Zadanie $i"
                isEnabled = false
            }
            horizontalLayout.addView(checkBox)
            mBoxes.add(checkBox)

            val button = Button(this).apply {
                text = "Uruchom test $i"
            }
            horizontalLayout.addView(button)
            mButtons.add(button)

            mLayout.addView(horizontalLayout)
        }

        // Pasek postępu umieszczony na dole
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 6
            progress = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 20, 20, 20)
            }
        }
        mLayout.addView(mProgressBar)

        // Obsługa kliknięć dla przycisków testów
        mButtons[0].setOnClickListener {
            val result = (task11(4, 6) in 0.666665..0.666667) &&
                    (task11(7, -6) in -1.1666667..-1.1666665)
            if (result && !mBoxes[0].isChecked) {
                mBoxes[0].isChecked = true
                testsPassed++
                mProgressBar.progress = testsPassed
                Toast.makeText(this, "Test 1 wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else if (!result) {
                Toast.makeText(this, "Test 1 nie został wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Test 1 już wykonany", Toast.LENGTH_SHORT).show()
            }
        }

        mButtons[1].setOnClickListener {
            val result = (task12(7U, 6U) == "7 + 6 = 13") &&
                    (task12(12U, 15U) == "12 + 15 = 27")
            if (result && !mBoxes[1].isChecked) {
                mBoxes[1].isChecked = true
                testsPassed++
                mProgressBar.progress = testsPassed
                Toast.makeText(this, "Test 2 wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else if (!result) {
                Toast.makeText(this, "Test 2 nie został wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Test 2 już wykonany", Toast.LENGTH_SHORT).show()
            }
        }

        mButtons[2].setOnClickListener {
            val result = (task13(0.0, 5.4f) && !task13(7.0, 5.4f) &&
                    !task13(-6.0, -1.0f) && task13(6.0, 9.1f) &&
                    !task13(6.0, -1.0f) && task13(1.0, 1.1f))
            if (result && !mBoxes[2].isChecked) {
                mBoxes[2].isChecked = true
                testsPassed++
                mProgressBar.progress = testsPassed
                Toast.makeText(this, "Test 3 wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else if (!result) {
                Toast.makeText(this, "Test 3 nie został wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Test 3 już wykonany", Toast.LENGTH_SHORT).show()
            }
        }

        mButtons[3].setOnClickListener {
            val result = (task14(-2, 5) == "-2 + 5 = 3") &&
                    (task14(-2, -5) == "-2 - 5 = -7")
            if (result && !mBoxes[3].isChecked) {
                mBoxes[3].isChecked = true
                testsPassed++
                mProgressBar.progress = testsPassed
                Toast.makeText(this, "Test 4 wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else if (!result) {
                Toast.makeText(this, "Test 4 nie został wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Test 4 już wykonany", Toast.LENGTH_SHORT).show()
            }
        }

        mButtons[4].setOnClickListener {
            val result = (task15("DOBRY") == 4) &&
                    (task15("barDzo dobry") == 5) &&
                    (task15("doStateczny") == 3) &&
                    (task15("Dopuszczający") == 2) &&
                    (task15("NIEDOSTATECZNY") == 1) &&
                    (task15("XYZ") == -1)
            if (result && !mBoxes[4].isChecked) {
                mBoxes[4].isChecked = true
                testsPassed++
                mProgressBar.progress = testsPassed
                Toast.makeText(this, "Test 5 wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else if (!result) {
                Toast.makeText(this, "Test 5 nie został wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Test 5 już wykonany", Toast.LENGTH_SHORT).show()
            }
        }

        mButtons[5].setOnClickListener {
            val result = (task16(
                mapOf("A" to 2U, "B" to 4U, "C" to 3U),
                mapOf("A" to 1U, "B" to 2U)
            ) == 2U) &&
                    (task16(
                        mapOf("A" to 2U, "B" to 4U, "C" to 3U),
                        mapOf("F" to 1U, "G" to 2U)
                    ) == 0u) &&
                    (task16(
                        mapOf("A" to 23U, "B" to 47U, "C" to 30U),
                        mapOf("A" to 1U, "B" to 2U, "C" to 4U)
                    ) == 7U)
            if (result && !mBoxes[5].isChecked) {
                mBoxes[5].isChecked = true
                testsPassed++
                mProgressBar.progress = testsPassed
                Toast.makeText(this, "Test 6 wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else if (!result) {
                Toast.makeText(this, "Test 6 nie został wykonany poprawnie", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Test 6 już wykonany", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Funkcje testowe

    private fun task11(a: Int, b: Int): Double =
        a.toDouble() / b.toDouble()

    private fun task12(a: UInt, b: UInt): String =
        "$a + $b = ${a + b}"

    fun task13(a: Double, b: Float): Boolean =
        a >= 0 && a < b.toDouble()

    fun task14(a: Int, b: Int): String =
        if (b < 0)
            "$a - ${Math.abs(b)} = ${a + b}"
        else
            "$a + $b = ${a + b}"

    fun task15(degree: String): Int =
        when (degree.lowercase()) {
            "bardzo dobry" -> 5
            "dobry" -> 4
            "dostateczny" -> 3
            "dopuszczający" -> 2
            "niedostateczny" -> 1
            else -> -1
        }

    fun task16(store: Map<String, UInt>, asset: Map<String, UInt>): UInt =
        asset.map { (element, requiredQuantity) ->
            store.getOrElse(element) { 0u } / requiredQuantity
        }.minOrNull() ?: 0u
}
