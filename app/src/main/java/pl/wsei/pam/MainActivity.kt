package pl.wsei.pam

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pl.wsei.pam.lab01.Lab01Activity
import pl.wsei.pam.lab01.R
import pl.wsei.pam.lab02.Lab02Activity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val btnLab01 = findViewById<Button>(R.id.mainBtnRunLab01)
        val btnLab02 = findViewById<Button>(R.id.mainBtnRunLab02)

        btnLab01.setOnClickListener {
            val intent = Intent(this, Lab01Activity::class.java)
            startActivity(intent)
        }

        btnLab02.setOnClickListener {
            val intent = Intent(this, Lab02Activity::class.java)
            startActivity(intent)
        }
    }
}
