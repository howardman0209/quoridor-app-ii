package com.boardgame.quoridor.ii.model

data class Player(
    val goalY: Int,
    var pawnLocation: Location,
    var remainingWalls: Int,
)
