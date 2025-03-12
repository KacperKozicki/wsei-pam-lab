package pl.wsei.pam.lab03

import pl.wsei.pam.lab03.Tile
data class MemoryGameEvent(
    val tiles: List<Tile>,
    val state: GameStates
)
