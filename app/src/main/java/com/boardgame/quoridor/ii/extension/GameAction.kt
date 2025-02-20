package com.boardgame.quoridor.ii.extension

import com.boardgame.quoridor.ii.model.GameAction

fun GameAction.toNotation(): String {
    return when (this) {
        is GameAction.PawnMovement -> newLocation.toNotation()
        is GameAction.WallPlacement -> "${location.toNotation()}${orientation.notation}"
    }
}