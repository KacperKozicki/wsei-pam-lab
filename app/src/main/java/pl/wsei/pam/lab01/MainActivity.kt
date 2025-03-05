package pl.wsei.pam.lab01

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var mLayout: LinearLayout
    lateinit var mTitle: TextView
    lateinit var mProgressBar: ProgressBar
    var mBoxes: MutableList<CheckBox> = mutableListOf()
    var mButtons: MutableList<Button> = mutableListOf()
    var testsPassed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mLayout = findViewById(R.id.main)

        // Tytuł
        mTitle = TextView(this)
        mTitle.text = "Laboratorium 1"
        mTitle.textSize = 24f
        val titleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        titleParams.setMargins(20, 20, 20, 20)
        mTitle.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        mTitle.layoutParams = titleParams
        mLayout.addView(mTitle)

        // Utwórz wiersz dla każdego testu: checkbox + przycisk
        for (i in 1..6) {
            val horizontalLayout = LinearLayout(this)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL

            val checkBox = CheckBox(this)
            checkBox.text = "Zadanie $i"
            checkBox.isEnabled = false
            horizontalLayout.addView(checkBox)
            mBoxes.add(checkBox)

            val button = Button(this)
            button.text = "Uruchom test $i"
            horizontalLayout.addView(button)
            mButtons.add(button)

            mLayout.addView(horizontalLayout)
        }

        // Pasek postępu umieszczony na dole
        mProgressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        val progressParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        progressParams.setMargins(20, 20, 20, 20)
        mProgressBar.layoutParams = progressParams
        mProgressBar.max = 6
        mProgressBar.progress = 0
        mLayout.addView(mProgressBar)

        // Ustawienie obsługi kliknięć dla przycisków testów
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

    // Wykonaj dzielenie niecałkowite parametru a przez b
    private fun task11(a: Int, b: Int): Double {
        return a.toDouble() / b.toDouble()
    }

    // Zwraca łańcuch wg schematu: <a> + <b> = <a+b>
    private fun task12(a: UInt, b: UInt): String {
        return "$a + $b = ${a + b}"
    }

    // Zwraca true, jeśli a jest nieujemne i mniejsze od b
    fun task13(a: Double, b: Float): Boolean {
        return a >= 0 && a < b.toDouble()
    }

    // Zwraca łańcuch dla liczb całkowitych, przy czym jeśli b jest ujemne, zamiast '+' używa '-'
    fun task14(a: Int, b: Int): String {
        return if (b < 0)
            "$a - ${Math.abs(b)} = ${a + b}"
        else
            "$a + $b = ${a + b}"
    }

    // Zwraca ocenę jako liczbę całkowitą na podstawie opisu słownego
    fun task15(degree: String): Int {
        return when(degree.lowercase()) {
            "bardzo dobry" -> 5
            "dobry" -> 4
            "dostateczny" -> 3
            "dopuszczający" -> 2
            "niedostateczny" -> 1
            else -> -1
        }
    }

    // Zwraca liczbę egzemplarzy, które można zbudować na podstawie magazynu (store) i zestawu elementów (asset)
    fun task16(store: Map<String, UInt>, asset: Map<String, UInt>): UInt {
        return asset.map { (element, requiredQuantity) ->
            store.getOrElse(element) { 0u } / requiredQuantity
        }.minOrNull() ?: 0u
    }
}
