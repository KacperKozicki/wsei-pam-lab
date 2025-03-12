package pl.wsei.pam.lab03

import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R
import pl.wsei.pam.lab03.GameStates
import pl.wsei.pam.lab03.MemoryBoardView
import pl.wsei.pam.lab03.Tile
import java.util.*

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.memory_board)

        // Get board dimensions from intent
        val size = intent.getIntArrayExtra("size") ?: intArrayOf(3, 3)
        val rows = size[0]
        val cols = size[1]

        if (savedInstanceState != null) {
            val state = savedInstanceState.getIntArray("state")
            mBoardModel = MemoryBoardView(mBoard, cols, rows)
            if (state != null) {
                mBoardModel.setState(state)
            }
        } else {
            mBoardModel = MemoryBoardView(mBoard, cols, rows)
        }

        setupGameEvents()
    }

    private fun setupGameEvents() {
        mBoardModel.setOnGameChangeListener { e ->
            run {
                when (e.state) {
                    GameStates.Matching -> {
                        e.tiles.forEach { Tile -> Tile.revealed = true }
                    }
                    GameStates.Match -> {
                        e.tiles.forEach { tile ->
                            tile.revealed = true
                            tile.removeOnClickListener()
                        }
                    }
                    GameStates.NoMatch -> {
                        e.tiles.forEach { tile -> tile.revealed = true }
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    e.tiles.forEach { tile -> tile.revealed = false }
                                }
                            }
                        }, 2000)
                    }
                    GameStates.Finished -> {
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("state", mBoardModel.getState())
    }
}