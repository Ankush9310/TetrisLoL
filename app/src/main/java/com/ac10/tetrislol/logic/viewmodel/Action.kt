package com.ac10.tetrislol.logic.viewmodel

import com.ac10.tetrislol.logic.Direction

sealed interface Action {
    data class Move(val direction: Direction) : Action
    data object Reset : Action
    data object Pause : Action
    data object Resume : Action
    data object Rotate : Action
    data object Drop : Action
    data object GameTick : Action
    data object Mute : Action
}