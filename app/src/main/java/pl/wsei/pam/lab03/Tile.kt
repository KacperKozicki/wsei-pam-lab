package pl.wsei.pam.lab03

import android.widget.ImageButton

// Prosta klasa do przechowywania stanu kafelka
data class Tile(
    val button: ImageButton,
    val tileResource: Int, // ID zasobu obrazka (np. R.drawable.ic_rocket)
    val deckResource: Int, // ID zasobu obrazka dla zakrytego kafelka
    var revealed: Boolean = false, // Czy kafelek jest tymczasowo odkryty
    var isMatched: Boolean = false // Czy kafelek został dopasowany i jest usunięty
) {
    fun updateImage() {
        if (isMatched) {
            // Zwykle ustawiamy GONE, ale na wszelki wypadek można też ustawić przezroczystość
            button.visibility = android.view.View.GONE
            button.alpha = 0.0f // Upewnij się, że jest niewidoczny
        } else if (revealed) {
            button.setImageResource(tileResource)
            button.alpha = 1.0f // Upewnij się, że jest widoczny
            button.visibility = android.view.View.VISIBLE
        } else {
            button.setImageResource(deckResource)
            button.alpha = 1.0f // Upewnij się, że jest widoczny
            button.visibility = android.view.View.VISIBLE
        }
    }

    // Usuwa listener, gdy kafelek jest dopasowany lub tymczasowo odkryty (w trakcie animacji)
    fun removeOnClickListener() {
        button.setOnClickListener(null)
    }

    // Przywraca listener, gdy kafelek jest ponownie zakrywany
    fun restoreOnClickListener(listener: android.view.View.OnClickListener) {
        if (!isMatched) { // Nie przywracaj listenera dla dopasowanych kafelków
            button.setOnClickListener(listener)
        }
    }
}