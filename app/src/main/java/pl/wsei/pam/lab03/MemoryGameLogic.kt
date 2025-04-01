package pl.wsei.pam.lab03

import android.util.Log

class MemoryGameLogic(private val maxMatches: Int) {

    // Przechowuje funkcję zwracającą wartość (ID zasobu) pierwszego odkrytego kafelka
    private var firstValueProvider: (() -> Int)? = null
    private var matches: Int = 0

    /**
     * Przetwarza kliknięcie na kafelek.
     * @param currentValueProvider Funkcja lambda zwracająca wartość (ID zasobu) aktualnie klikniętego kafelka.
     * @return Stan gry po przetworzeniu kliknięcia (Matching, Match, NoMatch, Finished).
     */
    fun process(currentValueProvider: () -> Int): GameStates {
        if (firstValueProvider == null) {
            // To jest pierwszy kafelek z pary
            firstValueProvider = currentValueProvider
            Log.d("GameLogic", "Processing first tile. State: Matching")
            return GameStates.Matching
        } else {
            // To jest drugi kafelek z pary
            val firstValue = firstValueProvider!!() // Pobierz wartość pierwszego kafelka
            val secondValue = currentValueProvider() // Pobierz wartość drugiego kafelka
            val isMatch = firstValue == secondValue // Sprawdź dopasowanie

            Log.d("GameLogic", "Processing second tile. FirstValue=$firstValue, SecondValue=$secondValue, IsMatch=$isMatch")

            // Zresetuj dostawcę pierwszej wartości, niezależnie od wyniku
            firstValueProvider = null

            if (isMatch) {
                matches++
                Log.d("GameLogic", "Match found! Total matches: $matches / $maxMatches")
                return if (matches == maxMatches) {
                    Log.d("GameLogic", "Game Finished!")
                    GameStates.Finished
                } else {
                    GameStates.Match
                }
            } else {
                Log.d("GameLogic", "No match.")
                return GameStates.NoMatch
            }
        }
    }

    /**
     * Zwraca aktualną liczbę znalezionych par.
     */
    fun getMatches(): Int {
        return matches
    }

    /**
     * Ustawia liczbę znalezionych par (używane przy przywracaniu stanu).
     */
    fun setMatches(count: Int) {
        if (count in 0..maxMatches) {
            matches = count
            Log.d("GameLogic", "Matches set to: $count")
        } else {
            Log.w("GameLogic", "Attempted to set invalid match count: $count (max is $maxMatches)")
        }
    }

    /**
     * Resetuje logikę gry do stanu początkowego.
     */
    fun reset() {
        matches = 0
        firstValueProvider = null
        Log.d("GameLogic", "Logic reset. Matches: $matches")
    }
}