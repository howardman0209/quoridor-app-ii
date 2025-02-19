package com.boardgame.quoridor.ii.model

import java.io.Serializable

sealed class GameAction : Serializable {
    data class PawnMovement(
        val oldLocation: Location,
        val newLocation: Location
    ) : GameAction()

    data class WallPlacement(
        val orientation: Orientation,
        val location: Location,
    ) : GameAction()
}