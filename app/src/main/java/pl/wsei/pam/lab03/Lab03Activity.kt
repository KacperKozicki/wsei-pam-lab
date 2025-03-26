package pl.wsei.pam.lab03

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R

class Lab03Activity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private lateinit var memoryBoard: MemoryBoardView
    private var menuItem: MenuItem? = null

    private var columns: Int = 4
    private var rows: Int = 4
    private var mediaPlayer: MediaPlayer? = null
    private var isSound: Boolean = true

    private val STATE_BOARD = "memory_board_state"
    private val STATE_SOUND = "sound_state"
    private val STATE_MATCH_COUNT = "match_count"
    private val TAG = "Lab03Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Pobierz wymiary planszy z intentu (jeśli są przesłane)
        val size = intent.getIntArrayExtra("size")
        if (size != null) {
            rows = size[0]
            columns = size[1]
        }

        // Przywróć stan dźwięku (domyślnie true)
        if (savedInstanceState != null) {
            isSound = savedInstanceState.getBoolean(STATE_SOUND, true)
        }

        gridLayout = findViewById(R.id.memory_board)
        gridLayout.rowCount = rows
        gridLayout.columnCount = columns

        // Inicjalizacja planszy
        memoryBoard = MemoryBoardView(gridLayout, columns, rows)
        setupGameListener()

        // Przywróć stan gry, jeśli zapisany stan istnieje
        savedInstanceState?.let {
            val boardState = it.getIntArray(STATE_BOARD)
            val matchCount = it.getInt(STATE_MATCH_COUNT, 0)
            if (boardState != null) {
                memoryBoard.setState(boardState)
                memoryBoard.setMatchCount(matchCount)
                // Jeśli liczba dopasowań wskazuje, że gra została ukończona
                if (matchCount >= (rows * columns / 2)) {
                    showGameCompletedDialog()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        menuItem = menu.findItem(R.id.board_activity_sound)
        updateSoundIcon()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.board_activity_sound -> {
                isSound = !isSound
                updateSoundIcon()
                val message = if (isSound) "Sound turned on" else "Sound turned off"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateSoundIcon() {
        menuItem?.setIcon(
            if (isSound) R.drawable.baseline_volume_up_24
            else R.drawable.baseline_volume_off_24
        )
    }

    private fun setupGameListener() {
        memoryBoard.setOnGameChangeListener { event ->
            Log.d(TAG, "Game event: ${event.state}, Tiles: ${event.tiles.size}")
            when (event.state) {
                GameStates.Matching -> {
                    event.tiles.forEach { tile ->
                        tile.button.post { tile.button.setImageResource(tile.tileResource) }
                    }
                }
                GameStates.Match -> {
                    playSound(R.raw.completion)
                    event.tiles.forEach { tile ->
                        tile.button.post { tile.button.setImageResource(tile.tileResource) }
                    }
                }
                GameStates.NoMatch -> {
                    playSound(R.raw.negative_guitar)
                    event.tiles.forEach { tile ->
                        tile.button.post { tile.button.setImageResource(tile.tileResource) }
                    }
                }
                GameStates.Finished -> {
                    playSound(R.raw.completion)
                    showGameCompletedDialog()
                }
            }
        }
    }

    private fun showGameCompletedDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Congratulations!")
            .setMessage("You've completed the memory game!")
            .setPositiveButton("Play Again") { dialog, _ ->
                resetGame()
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)

        runOnUiThread {
            builder.create().show()
        }
    }

    private fun resetGame() {
        gridLayout.removeAllViews()
        setupBoard()
    }

    private fun setupBoard() {
        gridLayout = findViewById(R.id.memory_board)
        gridLayout.rowCount = rows
        gridLayout.columnCount = columns
        memoryBoard = MemoryBoardView(gridLayout, columns, rows)
        setupGameListener()
    }

    private fun playSound(resourceId: Int) {
        if (!isSound) return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, resourceId)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Zapisywanie stanu gry przy zmianie konfiguracji (np. obrót ekranu)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(STATE_BOARD, memoryBoard.getState())
        outState.putBoolean(STATE_SOUND, isSound)
        outState.putInt(STATE_MATCH_COUNT, memoryBoard.getMatchCount())
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
