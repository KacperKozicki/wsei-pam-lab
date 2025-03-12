package pl.wsei.pam.lab02

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R
import pl.wsei.pam.lab03.Lab03Activity

class Lab02Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab02)

        // Find all buttons and set the click listener
        val buttons = listOf(
            findViewById<Button>(R.id.main_6_6_board),
            findViewById<Button>(R.id.main_4_4_board),
            findViewById<Button>(R.id.main_4_6_board),
            findViewById<Button>(R.id.main_6_4_board)
        )

        buttons.forEach { button ->
            button.setOnClickListener { v -> onBoardSizeSelected(v) }
        }
    }

    private fun onBoardSizeSelected(v: View) {
        val tag: String? = v.tag as String?
        val tokens: List<String>? = tag?.split(" ")
        val rows = tokens?.get(0)?.toInt() ?: 3
        val columns = tokens?.get(1)?.toInt() ?: 3

        // Launch Lab03Activity with selected board dimensions
        val intent = Intent(this, Lab03Activity::class.java)
        val size: IntArray = intArrayOf(rows, columns)
        intent.putExtra("size", size)
        startActivity(intent)
    }
}