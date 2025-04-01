package pl.wsei.pam.lab03

import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Użyj AlertDialog z androidx
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R

class Lab03Activity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private lateinit var memoryBoard: MemoryBoardView
    private var optionsMenuItemSound: MenuItem? = null // Zmień nazwę dla jasności

    private var columns: Int = 4
    private var rows: Int = 4
    private var mediaPlayer: MediaPlayer? = null
    private var isSoundEnabled: Boolean = true // Zmień nazwę dla jasności

    // Klucze do zapisywania stanu
    private val STATE_BOARD_LAYOUT = "memory_board_layout"
    private val STATE_MATCHED_STATE = "memory_matched_state" // Zmieniony klucz
    private val STATE_SOUND = "sound_state"
    private val STATE_MATCH_COUNT = "match_count"
    private val TAG = "Lab03Activity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)
        Log.d(TAG, "onCreate called. savedInstanceState is ${if (savedInstanceState == null) "null" else "not null"}")

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Pobierz wymiary planszy z intentu (jeśli są przesłane) - opcjonalne
        val size = intent.getIntArrayExtra("size")
        if (size != null && size.size == 2) {
            rows = size[0]
            columns = size[1]
            Log.d(TAG, "Board size from Intent: ${rows}x${columns}")
        } else {
            Log.d(TAG, "Using default board size: ${rows}x${columns}")
        }

        gridLayout = findViewById(R.id.memory_board)
        gridLayout.rowCount = rows
        gridLayout.columnCount = columns

        // Przywracanie stanu lub inicjalizacja nowej gry
        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring state from Bundle.")
            isSoundEnabled = savedInstanceState.getBoolean(STATE_SOUND, true)
            val savedLayout = savedInstanceState.getIntArray(STATE_BOARD_LAYOUT)
            val savedMatchedState = savedInstanceState.getBooleanArray(STATE_MATCHED_STATE)
            val savedMatchCount = savedInstanceState.getInt(STATE_MATCH_COUNT, 0)

            // Sprawdź, czy kluczowe dane stanu są dostępne
            if (savedLayout != null && savedMatchedState != null) {
                Log.d(TAG, "Restoring board with layout and matched state. Match count: $savedMatchCount")
                // Utwórz MemoryBoardView z zapisanymi danymi
                memoryBoard = MemoryBoardView(gridLayout, columns, rows, savedLayout, savedMatchedState)
                memoryBoard.setMatchCount(savedMatchCount) // Przywróć liczbę dopasowań w logice gry
            } else {
                // Jeśli brakuje kluczowych danych, rozpocznij nową grę
                Log.w(TAG, "Key state data missing in Bundle, initializing new board.")
                memoryBoard = MemoryBoardView(gridLayout, columns, rows)
            }
        } else {
            // Brak zapisanego stanu - inicjalizacja nowej gry
            Log.d(TAG, "No saved state found, initializing new board.")
            memoryBoard = MemoryBoardView(gridLayout, columns, rows)
        }

        // Ustaw nasłuchiwanie zdarzeń z gry (niezależnie od tego, czy stan był przywracany)
        setupGameListener()

        Log.d(TAG, "onCreate finished. Initial match count: ${memoryBoard.getMatchCount()}")
        // Sprawdź, czy gra była już ukończona przy przywracaniu stanu
        if (savedInstanceState != null && memoryBoard.getMatchCount() >= (rows * columns / 2)) {
            Log.d(TAG, "Game was already completed upon restoring state.")
            // Można od razu pokazać dialog, ale może to być mylące dla użytkownika
            // showGameCompletedDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        optionsMenuItemSound = menu.findItem(R.id.board_activity_sound)
        // Stan dźwięku jest już ustawiony w onCreate z savedInstanceState lub domyślnie
        updateSoundIcon() // Zaktualizuj ikonę na podstawie `isSoundEnabled`
        Log.d(TAG, "onCreateOptionsMenu finished. Sound enabled: $isSoundEnabled")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.board_activity_sound -> {
                isSoundEnabled = !isSoundEnabled // Odwróć stan dźwięku
                updateSoundIcon() // Zaktualizuj ikonę
                val message = if (isSoundEnabled) getString(R.string.sound_on_toast) else getString(R.string.sound_off_toast)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Sound option toggled. Sound enabled: $isSoundEnabled")
                true // Zdarzenie obsłużone
            }
            R.id.board_activity_reset -> {
                Log.d(TAG,"Reset option selected.")
                resetGame() // Wywołaj reset gry
                Toast.makeText(this, R.string.game_reset_toast, Toast.LENGTH_SHORT).show()
                true // Zdarzenie obsłużone
            }
            // Dodaj obsługę innych elementów menu, jeśli istnieją
            else -> super.onOptionsItemSelected(item) // Domyślna obsługa
        }
    }

    // Aktualizuje ikonę dźwięku w menu
    private fun updateSoundIcon() {
        optionsMenuItemSound?.setIcon(
            if (isSoundEnabled) R.drawable.baseline_volume_up_24 // Ikona dla włączonego dźwięku
            else R.drawable.baseline_volume_off_24 // Ikona dla wyłączonego dźwięku
        )
    }

    // Konfiguruje nasłuchiwanie zdarzeń z MemoryBoardView
    private fun setupGameListener() {
        memoryBoard.setOnGameChangeListener { event ->
            Log.d(TAG, "Game event received: ${event.state}, Tiles involved: ${event.tiles.size}")
            // Dźwięki są odtwarzane na podstawie stanu gry
            when (event.state) {
                GameStates.Match -> {
                    playSound(R.raw.completion) // Dźwięk dla dopasowania
                }
                GameStates.NoMatch -> {
                    playSound(R.raw.negative_guitar) // Dźwięk dla braku dopasowania
                }
                GameStates.Finished -> {
                    playSound(R.raw.completion) // Dźwięk ukończenia gry
                    Log.d(TAG, "Game finished!")
                    // Pokaż dialog o ukończeniu gry z niewielkim opóźnieniem,
                    // aby ostatnia animacja dopasowania mogła się zakończyć.
                    gridLayout.postDelayed({ showGameCompletedDialog() }, 600) // Opóźnienie np. 600ms
                }
                GameStates.Matching -> {
                    // Opcjonalnie: Dźwięk odkrycia pierwszej karty
                    // playSound(R.raw.card_flip)
                }
            }
            // Logika wizualna (animacje, ukrywanie) jest teraz w MemoryBoardView
        }
    }

    // Pokazuje dialog informujący o ukończeniu gry
    private fun showGameCompletedDialog() {
        // Upewnij się, że dialog nie jest już pokazywany
        if (isFinishing || supportFragmentManager.findFragmentByTag("completionDialog") != null) {
            Log.w(TAG, "Attempted to show completion dialog while finishing or already shown.")
            return
        }

        val builder = AlertDialog.Builder(this) // Użyj androidx.appcompat.app.AlertDialog
        builder.setTitle(getString(R.string.completion_dialog_title))
            .setMessage(getString(R.string.completion_dialog_message))
            .setPositiveButton(getString(R.string.completion_dialog_play_again)) { dialog, _ ->
                Log.d(TAG, "Completion dialog: Play Again clicked.")
                resetGame()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.completion_dialog_close)) { dialog, _ ->
                Log.d(TAG, "Completion dialog: Close clicked.")
                dialog.dismiss()
                finish() // Zamknij aktywność po kliknięciu "Zamknij"
            }
            .setCancelable(false) // Nie można zamknąć dialogu przez kliknięcie obok

        // Użyj runOnUiThread, jeśli jest szansa wywołania z innego wątku (choć tutaj raczej nie)
        runOnUiThread {
            val dialog = builder.create()
            dialog.show()
            // Dodaj tag, aby można było sprawdzić, czy dialog jest widoczny
            // Niestety, AlertDialog nie wspiera tagów jak FragmentDialog.
            // Można użyć flagi w aktywności, jeśli potrzebne jest śledzenie.
        }
        Log.d(TAG, "Game completed dialog shown.")
    }

    // Resetuje grę do stanu początkowego
    private fun resetGame() {
        Log.d(TAG, "Resetting game...")
        // Nie trzeba już ręcznie usuwać widoków, resetBoard w MemoryBoardView to załatwi
        // gridLayout.removeAllViews()
        memoryBoard.resetBoard() // Zresetuj stan planszy i logiki
        // Listener gry jest już ustawiony, nie trzeba go ustawiać ponownie
        // setupGameListener() // Niepotrzebne, jeśli listener jest ustawiany raz w onCreate
        Log.d(TAG, "Game reset complete.")
    }

    // Odtwarza dźwięk, jeśli dźwięk jest włączony
    private fun playSound(resourceId: Int) {
        if (!isSoundEnabled) {
            // Log.d(TAG, "Sound is disabled, not playing sound $resourceId")
            return
        }
        try {
            // Zatrzymaj i zwolnij poprzedni odtwarzacz, jeśli istnieje
            mediaPlayer?.stop() // Zatrzymaj przed zwolnieniem
            mediaPlayer?.release()
            mediaPlayer = null // Ustaw na null po zwolnieniu

            // Utwórz i uruchom nowy odtwarzacz
            mediaPlayer = MediaPlayer.create(this, resourceId)
            mediaPlayer?.setOnCompletionListener { mp ->
                // Zwolnij zasoby po zakończeniu odtwarzania
                // Log.d(TAG, "MediaPlayer finished playing, releasing.")
                mp.release()
                // Sprawdź czy to ten sam obiekt przed ustawieniem na null
                if (mediaPlayer == mp) {
                    mediaPlayer = null
                }
            }
            mediaPlayer?.start()
            // Log.d(TAG, "Playing sound: $resourceId")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound", e)
            mediaPlayer?.release() // Spróbuj zwolnić zasoby w razie błędu
            mediaPlayer = null
        }
    }

    // Zapisuje stan gry przed zniszczeniem aktywności (np. przy obrocie ekranu)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Sprawdź, czy memoryBoard została zainicjowana
        if (::memoryBoard.isInitialized) {
            val layout = memoryBoard.getBoardLayout()
            val matched = memoryBoard.getMatchedState()
            val matches = memoryBoard.getMatchCount()
            outState.putIntArray(STATE_BOARD_LAYOUT, layout)
            outState.putBooleanArray(STATE_MATCHED_STATE, matched)
            outState.putInt(STATE_MATCH_COUNT, matches)
            Log.d(TAG, "Saving instance state: Layout size=${layout.size}, Matched size=${matched.size}, Match count=$matches, Sound enabled=$isSoundEnabled")
        } else {
            Log.w(TAG, "onSaveInstanceState: memoryBoard not initialized!")
        }
        outState.putBoolean(STATE_SOUND, isSoundEnabled) // Zapisz stan dźwięku
    }

    // Pauzuje odtwarzanie dźwięku, gdy aktywność przechodzi w tło
    override fun onPause() {
        super.onPause()
        // Sprawdź, czy mediaPlayer istnieje i czy odtwarza
        if (mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.pause()
                Log.d(TAG, "MediaPlayer paused due to onPause.")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error pausing MediaPlayer", e)
                mediaPlayer?.release() // Zwolnij w razie błędu
                mediaPlayer = null
            }
        }
    }

    // Zwalnia zasoby MediaPlayer, gdy aktywność jest niszczona
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called, releasing MediaPlayer.")
        try {
            mediaPlayer?.stop() // Zatrzymaj przed zwolnieniem
            mediaPlayer?.release() // Zwolnij zasoby odtwarzacza
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer in onDestroy", e)
        }
        mediaPlayer = null // Ustaw na null, aby uniknąć użycia zwolnionego obiektu
    }

    // Dodajmy zasoby stringów dla lepszej praktyki (w pliku res/values/strings.xml)
    /*
    <resources>
        <string name="app_name">Lab03 Memory Game</string>
        <string name="sound_on_toast">Sound turned ON</string>
        <string name="sound_off_toast">Sound turned OFF</string>
        <string name="game_reset_toast">Game Reset</string>
        <string name="menu_sound_title">Toggle Sound</string>
        <string name="menu_reset_title">Reset Game</string>
        <string name="completion_dialog_title">Congratulations!</string>
        <string name="completion_dialog_message">You\'ve matched all the pairs!</string>
        <string name="completion_dialog_play_again">Play Again</string>
        <string name="completion_dialog_close">Close</string>
    </resources>
    */
}