package com.ac10.tetrislol.logic

import androidx.compose.ui.geometry.Offset
import kotlin.math.absoluteValue
import kotlin.random.Random

data class Tetrimino(
    val shape: List<Offset> = emptyList(),
    val offset: Offset = Offset(0, 0)
) {
    val location: List<Offset> = shape.map { it + offset }

    fun moveBy(step: Pair<Int, Int>): Tetrimino =
        copy(offset = offset + Offset(step.first, step.second))

    fun rotate(): Tetrimino {
        val newShape = shape.toMutableList()
        for (i in shape.indices) {
            newShape[i] = Offset(shape[i].y, -shape[i].x)
        }
        return copy(shape = newShape)


    }

    fun adjustOffset(matrix: Pair<Int, Int>, adjustY: Boolean = true): Tetrimino {
        val yOffset =
            if (adjustY)
                (location.minByOrNull { it.y }?.y?.takeIf { it < 0 }?.absoluteValue ?: 0).toInt() +
                        (location.maxByOrNull { it.y }?.y?.takeIf { it > matrix.second - 1 }
                            ?.let { matrix.second - it - 1 } ?: 0).toInt()
            else 0
        val xOffset =
            (location.minByOrNull { it.x }?.x?.takeIf { it < 0 }?.absoluteValue ?: 0).toInt() +
                    (location.maxByOrNull { it.x }?.x?.takeIf { it > matrix.first - 1 }
                        ?.let { matrix.first - it - 1 } ?: 0).toInt()
        return moveBy(xOffset to yOffset)
    }

    companion object {
        val Empty = Tetrimino()
    }


}

val TetriminoType = listOf(
    listOf(Offset(1, -1), Offset(1, 0), Offset(0, 0), Offset(0, 1)),//Z
    listOf(Offset(0, -1), Offset(0, 0), Offset(1, 0), Offset(1, 1)),//S
    listOf(Offset(0, -1), Offset(0, 0), Offset(0, 1), Offset(0, 2)),//I
    listOf(Offset(0, 1), Offset(0, 0), Offset(0, -1), Offset(1, 0)),//T
    listOf(Offset(1, 0), Offset(0, 0), Offset(1, -1), Offset(0, -1)),//O
    listOf(Offset(0, -1), Offset(1, -1), Offset(1, 0), Offset(1, 1)),//L
    listOf(Offset(1, -1), Offset(0, -1), Offset(0, 0), Offset(0, 1))//J
)

fun Tetrimino.isValidInMatrix(blocks: List<Brick>, matrix: Pair<Int, Int>): Boolean {
    return location.none { location ->
        location.x < 0 || location.x > matrix.first - 1 || location.y > matrix.second - 1 ||
                blocks.any { it.location.x == location.x && it.location.y == location.y }
    }
}

fun generateTetriminoReverse(matrix: Pair<Int, Int>): List<Tetrimino> {
    return TetriminoType.map {
        Tetrimino(it, Offset(Random.nextInt(matrix.first - 1), -1)).adjustOffset(matrix, false)
    }.shuffled()
}

