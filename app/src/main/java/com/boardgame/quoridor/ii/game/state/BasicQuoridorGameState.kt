package com.boardgame.quoridor.ii.game.state

import com.boardgame.quoridor.ii.MAX_NUM_OF_WALL
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Player
import kotlin.random.Random

abstract class BasicQuoridorGameState : GameStateProperty<BasicQuoridorGameState> {
    val maxNumOfWallPerPlayer = MAX_NUM_OF_WALL
    var numberOfTurn: Int = 1
        protected set

    abstract fun player(): Player

    abstract fun opponent(): Player

    abstract fun winner(): Player?

    abstract fun isLegalPawnMovement(action: GameAction.PawnMovement, forPlayer: Boolean = true): Boolean

    abstract fun isLegalWallPlacement(action: GameAction.WallPlacement): Boolean

    abstract fun getLegalPawnMovements(forPlayer: Boolean = true): List<GameAction.PawnMovement>

    abstract fun getLegalWallPlacements(): List<GameAction.WallPlacement>

    abstract fun getRandomLegalWallPlacement(): GameAction.WallPlacement

    abstract fun isPathToGoalExist(forPlayer: Boolean = true): Boolean

    abstract fun getShortestPathToGoal(forPlayer: Boolean = true, considerOpponent: Boolean = true): List<Location>?

    override fun executeGameAction(action: GameAction) {
        numberOfTurn++
    }

    override fun reverseGameAction(action: GameAction) {
        numberOfTurn--
    }

    override fun isTerminated(): Boolean {
        return winner() != null
    }

    override fun getLegalGameActions(): List<GameAction> {
        val legalMoves = getLegalPawnMovements()
        return if (player().remainingWalls > 0) legalMoves + getLegalWallPlacements() else legalMoves
    }

    fun getRandomLegalGameAction(): GameAction {
        return if (Random.Default.nextDouble() < 0.5 || player().remainingWalls <= 0) {
            getLegalPawnMovements().random()
        } else {
            getRandomLegalWallPlacement()
        }
    }

    fun getLegalPawnMovementToNearestGoal(): GameAction.PawnMovement {
        val shortestPathToGoal = getShortestPathToGoal(considerOpponent = true)
            ?: getShortestPathToGoal(considerOpponent = false)
            ?: throw Exception("Invalid game state. Shortest path to goal for player do not exist. $this")
        require(shortestPathToGoal.size >= 2) { "Invalid shortest path returned. Please check 'fun getShortestPathToGoal()' $this" }

        if (shortestPathToGoal[1] == opponent().pawnLocation) {
            return getLegalPawnMovements().random()
        }

        return GameAction.PawnMovement(oldPawnLocation = shortestPathToGoal[0], newPawnLocation = shortestPathToGoal[1])
    }
}