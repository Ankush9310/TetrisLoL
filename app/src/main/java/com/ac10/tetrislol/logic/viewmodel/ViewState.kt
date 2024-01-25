package com.ac10.tetrislol.logic.viewmodel

import com.ac10.tetrislol.logic.Brick
import com.ac10.tetrislol.logic.Tetrimino
import com.ac10.tetrislol.logic.Tetrimino.Companion.Empty
import kotlin.math.min

data class ViewState(
    val bricks: List<Brick> = emptyList(),
    val tetrimino: Tetrimino = Empty,
    val tetriminoReserve: List<Tetrimino> = emptyList(),
    val matrix: Pair<Int, Int> = MatrixWidth to MatrixHeight,
    val gameStatus: GameStatus = GameStatus.Onboard,
    val score: Int = 0,
    val line: Int = 0,
    val isMute: Boolean = false,
) {
    val level: Int
        get() = min(10, 1 + line / 20)

    val tetriminoNext: Tetrimino
        get() = tetriminoReserve.firstOrNull() ?: Empty

    val isPaused
        get() = gameStatus == GameStatus.Paused

    val isRunning
        get() = gameStatus == GameStatus.Running
}

private const val MatrixWidth = 12
private const val MatrixHeight = 24