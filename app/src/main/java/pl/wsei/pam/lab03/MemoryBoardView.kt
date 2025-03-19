package pl.wsei.pam.lab03

import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.wsei.pam.lab01.R
import java.util.*
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator

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

    //animacje przycisków trafionych kart
    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)
        set.startDelay = 500
        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)
        set.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}

            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        set.start()
    }

    private fun animateNonMatchedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()

        // Create a shaking animation
        val shakeRight = ObjectAnimator.ofFloat(button, "rotation", 0f, 10f)
        val shakeLeft = ObjectAnimator.ofFloat(button, "rotation", 10f, -10f)
        val shakeCenter = ObjectAnimator.ofFloat(button, "rotation", -10f, 0f)

        // Configure the shake sequence
        val shakeSequence = AnimatorSet().apply {
            playSequentially(shakeRight, shakeLeft, shakeCenter)
            duration = 300
        }

        // Repeat the shake a few times
        val fullShake = AnimatorSet().apply {
            playSequentially(shakeSequence, shakeSequence.clone(), shakeSequence.clone())
            interpolator = AccelerateDecelerateInterpolator()
        }

        fullShake.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                button.rotation = 0f  // Reset rotation
                action.run()
            }
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })

        fullShake.start()
    }

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
        onGameChangeStateListener = { event ->
            // Block UI during animations
            gridLayout.isEnabled = false

            when (event.state) {
                GameStates.Match, GameStates.Finished -> {
                    // For matched pairs
                    event.tiles.forEach { tile ->
                        animatePairedButton(tile.button) {
                            tile.revealed = true
                            tile.removeOnClickListener()
                            gridLayout.isEnabled = true
                        }
                    }
                }
                GameStates.NoMatch -> {
                    // For non-matching pairs
                    val nonMatchingTiles = event.tiles.toList()

                    // Animate the tiles
                    var animationsCompleted = 0
                    nonMatchingTiles.forEach { tile ->
                        animateNonMatchedButton(tile.button) {
                            animationsCompleted++
                            if (animationsCompleted == nonMatchingTiles.size) {
                                // Reset cards after animation completes
                                nonMatchingTiles.forEach { it.revealed = false }
                                gridLayout.isEnabled = true
                            }
                        }
                    }
                }
                GameStates.Matching -> {
                    // Just reveal the first card, no animation needed
                    event.tiles.firstOrNull()?.revealed = true
                    gridLayout.isEnabled = true
                }
            }

            // Call the original listener
            listener(event)
        }
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