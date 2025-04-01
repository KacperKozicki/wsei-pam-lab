package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.camera.viewfinder.core.ScaleType
import pl.wsei.pam.lab01.R
import java.util.*
import kotlin.collections.LinkedHashMap // Użyj LinkedHashMap, aby zachować kolejność wstawiania

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int,
    // Przyjmuje zapisaną kolejność ikon i stan dopasowania
    savedBoardLayout: IntArray? = null,
    savedMatchedState: BooleanArray? = null
) {
    // Użyj LinkedHashMap, aby klucze (tagi) miały przewidywalną kolejność iteracji
    private val tiles: MutableMap<String, Tile> = LinkedHashMap()
    private val boardLayout: MutableList<Int> = mutableListOf() // Przechowuje układ ikon na planszy
    private val icons: List<Int> = listOf(
        R.drawable.ic_rocket,
        android.R.drawable.ic_dialog_alert,
        android.R.drawable.ic_dialog_email,
        android.R.drawable.ic_dialog_info,
        android.R.drawable.ic_dialog_map,
        android.R.drawable.ic_input_add,
        android.R.drawable.ic_input_delete,
        android.R.drawable.ic_lock_idle_alarm,
        android.R.drawable.ic_media_pause,
        android.R.drawable.ic_media_play,
        android.R.drawable.ic_menu_camera,
        android.R.drawable.ic_menu_call
        // Dodaj więcej ikon, jeśli plansza jest większa niż 4x6
    )

    private val deckResource: Int = R.drawable.card_background // Użyj własnego tła karty
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { _ -> }
    private val matchedPair: Stack<Tile> = Stack() // Stos dla aktualnie odkrytych kafelków
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)
    private val tileClickListener = View.OnClickListener { v -> onClickTile(v) }


    init {
        gridLayout.removeAllViews() // Wyczyść gridLayout przed dodaniem nowych widoków
        initializeBoard(savedBoardLayout, savedMatchedState)
    }

    private fun initializeBoard(savedBoardLayout: IntArray?, savedMatchedState: BooleanArray?) {
        // 1. Ustal układ ikon (nowy lub z zapisanego stanu)
        if (savedBoardLayout != null) {
            Log.d("MemoryBoardView", "Restoring board layout from saved state.")
            boardLayout.addAll(savedBoardLayout.toList())
        } else {
            Log.d("MemoryBoardView", "Creating new shuffled board layout.")
            // Utwórz nowy, potasowany układ
            val iconsNeeded = cols * rows / 2
            if (icons.size < iconsNeeded) {
                throw IllegalArgumentException("Not enough icons (${icons.size}) for the board size (${cols}x${rows} requires $iconsNeeded pairs)")
            }
            val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
                it.addAll(icons.subList(0, iconsNeeded))
                it.addAll(icons.subList(0, iconsNeeded))
                it.shuffle()
            }
            boardLayout.addAll(shuffledIcons)
        }

        // 2. Utwórz siatkę kafelków
        tiles.clear() // Wyczyść mapę kafelków przed ponownym wypełnieniem
        var iconIndex = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tag = "${row}x${col}"
                val btn = ImageButton(gridLayout.context).apply {
                    this.tag = tag // Ustaw tag PRZED utworzeniem Tile
                    val layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = 0
                        setGravity(Gravity.CENTER)
                        columnSpec = GridLayout.spec(col, 1, 1f)
                        rowSpec = GridLayout.spec(row, 1, 1f)
                        // Ustaw marginesy dla odstępów między kafelkami
                        val margin = 8 // w dp, przelicz na piksele jeśli potrzeba
                        setMargins(margin, margin, margin, margin)
                    }
                    this.layoutParams = layoutParams
                   // this.scaleType = ImageButton.ScaleType.CENTER_CROP // Dopasuj obrazek
                    // Ustaw tło, jeśli chcesz mieć ramkę lub inny efekt
                    // this.setBackgroundResource(R.drawable.tile_border)
                }
                gridLayout.addView(btn) // Dodaj przycisk do gridLayout

                val iconRes = boardLayout[iconIndex]
                val tile = Tile(btn, iconRes, deckResource)
                tiles[tag] = tile // Dodaj do mapy używając tagu jako klucza

                iconIndex++
            }
        }
        Log.d("MemoryBoardView", "Created ${tiles.size} tiles.")

        // 3. Przywróć stan dopasowania (jeśli istnieje) i ustaw początkowy wygląd
        tiles.values.forEachIndexed { index, tile ->
            val isMatched = savedMatchedState?.get(index) ?: false
            if (isMatched) {
                tile.isMatched = true
                tile.revealed = true // Dopasowane są też "odkryte" w sensie logiki
                tile.removeOnClickListener()
            } else {
                tile.isMatched = false
                tile.revealed = false // Upewnij się, że są zakryte
                tile.restoreOnClickListener(tileClickListener) // Ustaw listener dla niedopasowanych
            }
            tile.updateImage() // Ustaw obrazek (zakryty, odkryty lub GONE)
        }
        Log.d("MemoryBoardView", "Initial tile states applied. Matched count from state: ${savedMatchedState?.count { it } ?: 0}")
    }


    // Zwraca aktualny układ ikon na planszy
    fun getBoardLayout(): IntArray {
        return boardLayout.toIntArray()
    }

    // Zwraca stan dopasowania każdego kafelka (true jeśli dopasowany i usunięty)
    fun getMatchedState(): BooleanArray {
        // Upewnij się, że iterujesz w tej samej kolejności, w jakiej tworzono kafelki
        // LinkedHashMap zachowuje kolejność wstawiania
        return tiles.values.map { it.isMatched }.toBooleanArray()
    }

    // Ustawia stan dopasowania dla kafelków (używane przy przywracaniu stanu)
    // Ta metoda jest teraz częścią logiki initializeBoard, ale może być użyteczna oddzielnie
    fun setMatchedState(state: BooleanArray) {
        if (state.size != tiles.size) {
            Log.e("MemoryBoardView", "setMatchedState: state size (${state.size}) != tiles size (${tiles.size})")
            return
        }
        var idx = 0
        // Użyj `entries` dla LinkedHashMap, aby zachować kolejność
        for (tile in tiles.values) {
            if (state[idx]) {
                tile.isMatched = true
                tile.revealed = true // Dopasowane są też odkryte
                tile.removeOnClickListener() // Usuń listener dla dopasowanych
            } else {
                tile.isMatched = false
                // Nie resetuj 'revealed' tutaj, bo może być tymczasowo odkryty
                tile.restoreOnClickListener(tileClickListener) // Upewnij się, że listener jest ustawiony
            }
            tile.updateImage() // Zaktualizuj wygląd kafelka (GONE lub obrazek)
            idx++
        }
        Log.d("MemoryBoardView", "setMatchedState applied. Matched count: ${state.count { it }}")
    }

    // Logika kliknięcia na kafelek
    private fun onClickTile(v: View) {
        val tag = v.tag?.toString() ?: return // Ignoruj kliknięcia na widoki bez tagu
        val tile = tiles[tag]

        // Ignoruj kliknięcia jeśli:
        // - Kafelek nie istnieje
        // - Kafelek jest już odkryty (tymczasowo lub na stałe)
        // - Stos `matchedPair` zawiera już 2 kafelki (czekamy na animację)
        if (tile == null || tile.revealed || matchedPair.size >= 2) {
            Log.d("MemoryBoardView", "Ignoring click on tile $tag: tile=$tile, revealed=${tile?.revealed}, matchedPairSize=${matchedPair.size}")
            return
        }
        Log.d("MemoryBoardView", "Tile $tag clicked.")

        tile.revealed = true
        tile.updateImage()
        tile.removeOnClickListener() // Usuń listener, aby uniknąć podwójnego kliknięcia

        matchedPair.push(tile)

        if (matchedPair.size == 2) {
            // Mamy parę, zablokuj planszę na czas sprawdzania i animacji
            setBoardInteraction(false)
            Log.d("MemoryBoardView", "Two tiles revealed, processing match...")

            // Sprawdź dopasowanie używając zasobów ikon
            val firstTile = matchedPair[0]
            val secondTile = matchedPair[1]
            val matchResult = logic.process { secondTile.tileResource } // Przekaż zasób drugiego kafelka

            // Przekaż informację o zdarzeniu do Activity
            onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
        } else {
            // Dopiero pierwszy kafelek odkryty, stan Matching
            Log.d("MemoryBoardView", "First tile revealed, waiting for second.")
            val matchResult = GameStates.Matching
            logic.process { tile.tileResource } // Zaktualizuj logikę pierwszym kafelkiem
            onGameChangeStateListener(MemoryGameEvent(listOf(tile), matchResult))
            // Nie blokuj planszy po odkryciu pierwszego kafelka
            // setBoardInteraction(true) // Usuń to, jeśli blokujesz tylko przy dwóch
        }
    }

    // Ustawia nasłuchiwanie na zdarzenia z gry (wywoływane z Activity)
    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = { event ->
            Log.d("MemoryBoardView", "Handling game event: ${event.state}")
            when (event.state) {
                GameStates.Match, GameStates.Finished -> {
                    // Dopasowanie lub koniec gry - animacja znikania
                    event.tiles.forEach { tile ->
                        // Oznacz jako dopasowany *przed* animacją
                        tile.isMatched = true
                        tile.revealed = true // Pozostaje odkryty (logicznie)
                        tile.removeOnClickListener() // Upewnij się, że listener jest usunięty

                        animatePairedButton(tile.button) {
                            // Po animacji:
                            tile.updateImage() // Ustawia visibility = GONE
                            Log.d("MemoryBoardView", "Match animation finished for tile ${tile.button.tag}")
                            // Sprawdź, czy wszystkie animacje dla pary się zakończyły
                            if (matchedPair.all { it.isMatched }) { // Sprawdź, czy oba są oznaczone
                                Log.d("MemoryBoardView", "Both match animations finished.")
                                matchedPair.clear()
                                setBoardInteraction(true) // Odblokuj planszę po zakończeniu animacji pary
                            }
                        }
                    }
                }
                GameStates.NoMatch -> {
                    // Brak dopasowania - animacja potrząsania i zakrycie
                    val firstTile = event.tiles[0]
                    val secondTile = event.tiles[1]

                    animateNonMatchedButton(firstTile.button) {
                        // Po animacji pierwszego:
                        firstTile.revealed = false
                        firstTile.updateImage()
                        firstTile.restoreOnClickListener(tileClickListener)
                        Log.d("MemoryBoardView", "NoMatch animation 1 finished for tile ${firstTile.button.tag}")
                        // Sprawdź, czy druga animacja też się zakończyła
                        if (!secondTile.revealed) { // Jeśli drugi też już jest zakryty
                            Log.d("MemoryBoardView", "Both NoMatch animations finished.")
                            matchedPair.clear()
                            setBoardInteraction(true)
                        }
                    }
                    animateNonMatchedButton(secondTile.button) {
                        // Po animacji drugiego:
                        secondTile.revealed = false
                        secondTile.updateImage()
                        secondTile.restoreOnClickListener(tileClickListener)
                        Log.d("MemoryBoardView", "NoMatch animation 2 finished for tile ${secondTile.button.tag}")
                        // Sprawdź, czy pierwsza animacja też się zakończyła
                        if (!firstTile.revealed) { // Jeśli pierwszy też już jest zakryty
                            Log.d("MemoryBoardView", "Both NoMatch animations finished.")
                            matchedPair.clear()
                            setBoardInteraction(true)
                        }
                    }
                }
                GameStates.Matching -> {
                    // Pierwszy kafelek odkryty - nic specjalnego do animowania, plansza pozostaje aktywna
                    // Listener został już usunięty w onClickTile
                    // Wizualna zmiana obrazka też już nastąpiła w onClickTile
                    // setBoardInteraction(true) // Nie ma potrzeby zmiany interakcji
                }
            }
            // Wywołaj oryginalny listener przekazany z Activity (np. do odtwarzania dźwięków)
            listener(event)
        }
    }

    // Animacja dla dopasowanej pary (znikanie)
    private fun animatePairedButton(button: ImageButton, onEndAction: Runnable) {
        val set = AnimatorSet()
        // Prostsza animacja zanikania i lekkiego zmniejszenia
        val fadeOut = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.5f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.5f)

        set.playTogether(fadeOut, scaleX, scaleY)
        set.duration = 500 // Krótszy czas animacji
        set.interpolator = DecelerateInterpolator()

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Przywróć skalę na wypadek ponownego użycia widoku (choć tutaj znika)
                button.scaleX = 1f
                button.scaleY = 1f
                button.visibility = View.GONE // Kluczowe: Ustaw GONE po animacji
                Log.d("MemoryBoardView","Paired animation ended for ${button.tag}, setting GONE.")
                onEndAction.run() // Wykonaj akcję po zakończeniu (np. odblokowanie planszy)
            }
        })
        set.start()
    }

    // Animacja dla niedopasowanej pary (potrząsanie)
    private fun animateNonMatchedButton(button: ImageButton, onEndAction: Runnable) {
        val set = AnimatorSet()
        val shake = ObjectAnimator.ofFloat(button, "translationX", 0f, 15f, -15f, 15f, -15f, 0f)
        shake.duration = 400 // Czas trwania potrząsania
        shake.interpolator = AccelerateDecelerateInterpolator()

        set.play(shake)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                button.translationX = 0f // Zresetuj pozycję X
                Log.d("MemoryBoardView","NonMatched animation ended for ${button.tag}, running action.")
                onEndAction.run() // Wykonaj akcję (np. ukrycie kafelka, odblokowanie planszy)
            }
        })
        set.start()
    }

    // Włącza lub wyłącza interakcję z całą planszą
    private fun setBoardInteraction(enabled: Boolean) {
        Log.d("MemoryBoardView", "Setting board interaction to: $enabled")
        // Można też iterować po kafelkach, ale wyłączenie gridLayout jest prostsze
        gridLayout.isEnabled = enabled
        // Dodatkowo, aby zapobiec kliknięciom podczas animacji, można iterować:
        /*
        tiles.values.forEach { tile ->
            if (enabled && !tile.isMatched && !tile.revealed) { // Włącz tylko dla aktywnych, zakrytych kafelków
                tile.restoreOnClickListener(tileClickListener)
            } else {
                tile.removeOnClickListener() // Wyłącz dla wszystkich innych przypadków
            }
        }
        */
    }

    // Metody do pobierania/ustawiania liczby dopasowań w logice gry
    fun getMatchCount(): Int {
        return logic.getMatches()
    }

    fun setMatchCount(count: Int) {
        logic.setMatches(count)
        Log.d("MemoryBoardView", "Match count set to $count")
    }

    // Metoda resetująca planszę do stanu początkowego (nowe tasowanie)
    fun resetBoard() {
        Log.d("MemoryBoardView", "Resetting board.")
        logic.reset() // Zresetuj logikę gry (liczbę dopasowań)
        matchedPair.clear() // Wyczyść stos aktualnie odkrytych
        initializeBoard(null, null) // Utwórz nową planszę bez zapisanych stanów
        setBoardInteraction(true) // Upewnij się, że plansza jest interaktywna
    }
}
