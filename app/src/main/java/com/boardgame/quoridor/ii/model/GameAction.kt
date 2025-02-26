package com.boardgame.quoridor.ii.model

import java.io.Serializable

sealed class GameAction : Serializable {
    data class PawnMovement(
        val oldPawnLocation: Location,
        val newPawnLocation: Location
    ) : GameAction()

    data class WallPlacement(
        val orientation: Orientation,
        val wallLocation: Location,
    ) : GameAction()
}