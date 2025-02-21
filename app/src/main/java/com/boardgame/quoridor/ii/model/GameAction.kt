package com.boardgame.quoridor.ii.model

import java.io.Serializable

sealed class GameAction : Serializable {
    companion object {
        fun fromNotation(notation: String): GameAction? {
            if (notation.length < 2) return null
            val location = Location.fromNotation(notation.take(2)) ?: return null
            return if (notation.length == 3) {
                val orientation = Orientation.fromNotation(notation.last()) ?: return null
                WallPlacement(orientation, location)
            } else {
                PawnMovement(newPawnLocation = location)
            }
        }
    }

    data class PawnMovement(
        val newPawnLocation: Location
    ) : GameAction()

    data class WallPlacement(
        val orientation: Orientation,
        val wallLocation: Location,
    ) : GameAction()
}