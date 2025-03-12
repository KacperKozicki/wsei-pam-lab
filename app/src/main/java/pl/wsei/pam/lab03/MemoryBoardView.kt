package pl.wsei.pam.lab03

import pl.wsei.pam.lab03.Tile
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.wsei.pam.lab01.R
import java.util.*

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
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
        // Add more icons if needed for larger boards
    )

    init {
        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            it.addAll(icons.subList(0, cols * rows / 2))
            it.addAll(icons.subList(0, cols * rows / 2))
            it.shuffle()
        }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"
                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.CENTER)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }
                val icon = shuffledIcons.removeAt(0)
                addTile(btn, icon)
            }
        }
    }

    private val deckResource: Int = R.drawable.ic_rocket
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { _ -> }
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    private fun onClickTile(v: View) {
        val tile = tiles[v.tag]
        if (tile != null && !tile.revealed) {
            matchedPair.push(tile)
            val matchResult = logic.process {
                tile.tileResource
            }
            onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
            if (matchResult != GameStates.Matching) {
                matchedPair.clear()
            }
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    fun getState(): IntArray {
        val state = IntArray(rows * cols) { -1 }
        var idx = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles["${row}x${col}"]
                if (tile?.revealed == true) {
                    state[idx] = tile.tileResource
                }
                idx++
            }
        }
        return state
    }

    fun setState(state: IntArray) {
        var idx = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles["${row}x${col}"]
                if (state[idx] != -1 && tile != null) {
                    tile.revealed = true
                    tile.removeOnClickListener()
                }
                idx++
            }
        }
    }
}