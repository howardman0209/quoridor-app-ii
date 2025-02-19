package com.boardgame.quoridor.ii.model

class Player(
    val goalY: Int,
    var pawnLocation: Location,
    var remainingWalls: Int,
) {
    fun hasReachedGoal() = pawnLocation.y == goalY

    override fun hashCode(): Int {
        var result = goalY
        result = 31 * result + remainingWalls
        result = 31 * result + pawnLocation.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (goalY != other.goalY) return false
        if (remainingWalls != other.remainingWalls) return false
        if (pawnLocation != other.pawnLocation) return false

        return true
    }
}
