package com.boardgame.quoridor.ii.game

import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Player

abstract class BasicQuoridorGameState : GameStateProperty<BasicQuoridorGameState> {
    abstract fun player(): Player

    abstract fun opponent(): Player

    abstract fun isLegalPawnMovement(action: GameAction.PawnMovement, forPlayer: Boolean = true): Boolean

    abstract fun isLegalWallPlacement(action: GameAction.WallPlacement): Boolean

    abstract fun getLegalPawnMovements(forPlayer: Boolean = true): List<GameAction.PawnMovement>

    abstract fun getLegalWallPlacements(): List<GameAction.WallPlacement>

    abstract fun getShortestPathToGoal(forPlayer: Boolean = true): List<Location>?

    override fun getLegalGameActions(): List<GameAction> {
        val legalMoves = getLegalPawnMovements()
        return if (player().remainingWalls > 0) legalMoves + getLegalWallPlacements() else legalMoves
    }
}