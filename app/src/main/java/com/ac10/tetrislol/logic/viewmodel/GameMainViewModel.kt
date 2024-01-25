package com.ac10.tetrislol.logic.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ac10.tetrislol.logic.Brick
import com.ac10.tetrislol.logic.Direction
import com.ac10.tetrislol.logic.ScoreEverySpirit
import com.ac10.tetrislol.logic.SoundType
import com.ac10.tetrislol.logic.SoundUtil
import com.ac10.tetrislol.logic.Tetrimino
import com.ac10.tetrislol.logic.Tetrimino.Companion.Empty
import com.ac10.tetrislol.logic.calculateScore
import com.ac10.tetrislol.logic.generateTetriminoReverse
import com.ac10.tetrislol.logic.isValidInMatrix
import com.ac10.tetrislol.logic.toOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GameMainViewModel : ViewModel() {

    private val _viewState: MutableState<ViewState> = mutableStateOf(ViewState())
    val viewState: State<ViewState> = _viewState


    fun dispatch(action: Action) =
        reduce(viewState.value, action)

    private fun reduce(state: ViewState, action: Action) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {

                emit(when (action) {
                    Action.Reset -> run {
                        if (state.gameStatus == GameStatus.Onboard || state.gameStatus == GameStatus.GameOver)
                            return@run ViewState(
                                gameStatus = GameStatus.Running,
                                isMute = state.isMute
                            )
                        state.copy(
                            gameStatus = GameStatus.ScreenClearing
                        ).also {
                            launch {
                                clearScreen(state = state)
                                emit(
                                    ViewState(
                                        gameStatus = GameStatus.Onboard,
                                        isMute = state.isMute
                                    )
                                )
                            }
                        }
                    }

                    Action.Pause -> if (state.isRunning) {
                        state.copy(gameStatus = GameStatus.Paused)
                    } else state

                    Action.Resume ->
                        if (state.isPaused) {
                            state.copy(gameStatus = GameStatus.Running)
                        } else state

                    is Action.Move -> run {
                        if (!state.isRunning) return@run state
                        SoundUtil.play(state.isMute, SoundType.Move)
                        val offset = action.direction.toOffset()
                        val tetrimino = state.tetrimino.moveBy(offset)
                        if (tetrimino.isValidInMatrix(state.bricks, state.matrix)) {
                            state.copy(tetrimino = tetrimino)
                        } else {
                            state
                        }
                    }

                    Action.Rotate -> run {
                        if (!state.isRunning) return@run state
                        SoundUtil.play(state.isMute, SoundType.Rotate)
                        val tetrimino = state.tetrimino.rotate().adjustOffset(state.matrix)
                        if (tetrimino.isValidInMatrix(state.bricks, state.matrix)) {
                            state.copy(tetrimino = tetrimino)
                        } else {
                            state
                        }
                    }

                    Action.Drop -> run {
                        if (!state.isRunning) return@run state
                        SoundUtil.play(state.isMute, SoundType.Drop)
                        var i = 0
                        while (state.tetrimino.moveBy(0 to ++i)
                                .isValidInMatrix(state.bricks, state.matrix)
                        ) { //nothing to do
                        }

                        val tetrimino = state.tetrimino.moveBy(0 to i - 1)

                        state.copy(tetrimino = tetrimino)
                    }

                    Action.GameTick -> run {
                        if (!state.isRunning) return@run state

                        //Tetrimino continue falling
                        if (state.tetrimino != Empty) {
                            val tetrimino = state.tetrimino.moveBy(Direction.Down.toOffset())
                            if (tetrimino.isValidInMatrix(state.bricks, state.matrix)) {
                                return@run state.copy(tetrimino = tetrimino)
                            }
                        }

                        //GameOver
                        if (!state.tetrimino.isValidInMatrix(state.bricks, state.matrix)) {
                            return@run state.copy(
                                gameStatus = GameStatus.ScreenClearing
                            ).also {
                                launch {
                                    emit(
                                        clearScreen(state = state).copy(gameStatus = GameStatus.GameOver)
                                    )
                                }
                            }
                        }

                        //Next Tetrimino
                        val (updatedBricks, clearedLines) = updateBricks(
                            state.bricks,
                            state.tetrimino,
                            matrix = state.matrix
                        )
                        val (noClear, clearing, cleared) = updatedBricks
                        val newState = state.copy(
                            tetrimino = state.tetriminoNext,
                            tetriminoReserve = (state.tetriminoReserve - state.tetriminoNext).takeIf { it.isNotEmpty() }
                                ?: generateTetriminoReverse(state.matrix),
                            score = state.score + calculateScore(clearedLines) +
                                    if (state.tetrimino != Empty) ScoreEverySpirit else 0,
                            line = state.line + clearedLines
                        )
                        if (clearedLines != 0) {
                            // has cleared lines
                            SoundUtil.play(state.isMute, SoundType.Clean)
                            state.copy(
                                gameStatus = GameStatus.LineClearing
                            ).also {
                                launch {
                                    //animate the clearing lines
                                    repeat(5) {
                                        emit(
                                            state.copy(
                                                gameStatus = GameStatus.LineClearing,
                                                tetrimino = Empty,
                                                bricks = if (it % 2 == 0) noClear else clearing
                                            )
                                        )
                                        delay(100)
                                    }
                                    //delay emit new state
                                    emit(
                                        newState.copy(
                                            bricks = cleared,
                                            gameStatus = GameStatus.Running
                                        )
                                    )
                                }
                            }
                        } else {
                            newState.copy(bricks = noClear)
                        }
                    }

                    Action.Mute -> state.copy(isMute = !state.isMute)

                })
            }
        }

    }

    private suspend fun clearScreen(state: ViewState): ViewState {
        SoundUtil.play(state.isMute, SoundType.Start)
        val xRange = 0 until state.matrix.first
        var newState = state

        (state.matrix.second downTo 0).forEach { y ->
            emit(
                state.copy(
                    gameStatus = GameStatus.ScreenClearing,
                    bricks = state.bricks + Brick.of(
                        xRange, y until state.matrix.second
                    )
                )
            )
            delay(50)
        }
        (0..state.matrix.second).forEach { y ->
            emit(
                state.copy(
                    gameStatus = GameStatus.ScreenClearing,
                    bricks = Brick.of(xRange, y until state.matrix.second),
                    tetrimino = Empty
                ).also { newState = it }
            )
            delay(50)
        }
        return newState
    }

    private fun updateBricks(
        curBricks: List<Brick>,
        tetrimino: Tetrimino,
        matrix: Pair<Int, Int>
    ): Pair<Triple<List<Brick>, List<Brick>, List<Brick>>, Int> {
        val bricks = (curBricks + Brick.of(tetrimino))
        val map = mutableMapOf<Float, MutableSet<Float>>()
        bricks.forEach {
            map.getOrPut(it.location.y) {
                mutableSetOf()
            }.add(it.location.x)
        }
        var clearing = bricks
        var cleared = bricks
        val clearLines = map.entries.sortedBy { it.key }
            .filter { it.value.size == matrix.first }.map { it.key }
            .onEach { line ->
                //clear line
                clearing = clearing.filter { it.location.y != line }
                //clear line and then offset brick
                cleared = cleared.filter { it.location.y != line }
                    .map { if (it.location.y < line) it.offsetBy(0 to 1) else it }

            }

        return Triple(bricks, clearing, cleared) to clearLines.size
    }


    private fun emit(state: ViewState) {
        _viewState.value = state
    }


}