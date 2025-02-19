package com.boardgame.quoridor.ii.model

interface GameStateProperty<T> {
    fun player(): Player

    fun opponent(): Player

    fun takeGameAction(action: GameAction)

    fun reverseGameAction(action: GameAction)

    fun isLegalPawnMovement(action: GameAction.PawnMovement, forPlayer: Boolean = true): Boolean

    fun isLegalWallPlacement(action: GameAction.WallPlacement): Boolean

    fun getLegalPawnMovements(forPlayer: Boolean = true): List<GameAction.PawnMovement>

    fun getLegalWallPlacements(): List<GameAction.WallPlacement>

    fun getShortestPathToGoal(forPlayer: Boolean = true): List<Location>?

    fun isTerminated(): Boolean

    fun clone(): T

    val printable: String
}