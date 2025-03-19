package pl.wsei.pam.lab03

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R

class Lab03Activity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private lateinit var memoryBoard: MemoryBoardView

    private var columns: Int = 4
    private var rows: Int = 4
    private var mediaPlayer: MediaPlayer? = null

    private val STATE_BOARD = "memory_board_state"
    private val TAG = "Lab03Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        setupBoard()

        // If there's saved state, restore it
        savedInstanceState?.getIntArray(STATE_BOARD)?.let { state ->
            memoryBoard.setState(state)
        }
    }

    private fun showGameCompletedDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Congratulations!")
            .setMessage("You've completed the memory game!")
            .setPositiveButton("Play Again") { dialog, _ ->
                recreate()
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)

        runOnUiThread {
            builder.create().show()
        }
    }

    private fun setupBoard() {
        gridLayout = findViewById(R.id.memory_board)
        gridLayout.rowCount = rows
        gridLayout.columnCount = columns

        // Create memory board
        memoryBoard = MemoryBoardView(gridLayout, columns, rows)

        // Setup game state listener
        memoryBoard.setOnGameChangeListener { event ->
            Log.d(TAG, "Game event: ${event.state}, Tiles: ${event.tiles.size}")

            when (event.state) {
                GameStates.Matching -> {
                    event.tiles.forEach { tile ->
                        tile.button.post {
                            tile.button.setImageResource(tile.tileResource)
                        }
                    }
                }
                GameStates.Match -> {
                    playSound(R.raw.completion)
                    event.tiles.forEach { tile ->
                        tile.button.post {
                            tile.button.setImageResource(tile.tileResource)
                        }
                    }
                }
                GameStates.NoMatch -> {
                    playSound(R.raw.negative_guitar)
                    event.tiles.forEach { tile ->
                        tile.button.post {
                            tile.button.setImageResource(tile.tileResource)
                        }
                    }
                }
                GameStates.Finished -> {
                    playSound(R.raw.completion)
                    showGameCompletedDialog()
                }
            }
        }
    }

    private fun playSound(resourceId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, resourceId)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(STATE_BOARD, memoryBoard.getState())
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}