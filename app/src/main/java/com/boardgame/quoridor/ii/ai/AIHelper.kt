package com.boardgame.quoridor.ii.ai

import android.util.Log
import com.boardgame.quoridor.ii.game.BasicQuoridorGameState
import com.boardgame.quoridor.ii.game.QuoridorGameState
import com.boardgame.quoridor.ii.model.Player
import kotlin.random.Random

object AIHelper {
    private const val TAG = "AIHelper"

    /**
     * heuristic is added into the game play simulation
     * 1. In a certain probability (A) to choose the next move in the shortest path to goal
     * 2. if opponent has no wall remains, only choose the next move in the shortest path to goal
     * 3. Otherwise (1-A), place an effective wall randomly if wall remains
     * 4. if no wall remains, move pawn randomly
     */
    private fun simulatePlayWithHeuristic(gameState: BasicQuoridorGameState): BasicQuoridorGameState {
        val simulationGame = gameState.deepCopy() as QuoridorGameState

        var winner: Player? = simulationGame.winner()
        while (winner == null) {
//            Log.d(TAG, "simulationGame: $simulationGame") // this log significantly time consuming
            val gameAction = if (simulationGame.opponent().remainingWalls <= 0 || Random.nextDouble() < 0.7) {
                simulationGame.getLegalPawnMovementToNearestGoal()
            } else {
                if (simulationGame.player().remainingWalls > 0) {
                    simulationGame.getRandomEffectiveWallPlacement()
                } else {
                    simulationGame.getLegalPawnMovements().random()
                }
            }
            simulationGame.executeGameAction(gameAction)
            winner = simulationGame.winner()
        }

        return simulationGame
    }

    /**
     * Pure random game play simulation
     */
    private fun simulatePlayPureRandom(gameState: BasicQuoridorGameState): BasicQuoridorGameState {
        val simulationGame = gameState.deepCopy()

        var winner: Player? = simulationGame.winner()
        while (winner == null) {
//            Log.d(TAG, "simulationGame: $simulationGame") // this log significantly time consuming
            val gameAction = simulationGame.getRandomLegalGameAction()
            simulationGame.executeGameAction(gameAction)
            winner = simulationGame.winner()
        }

        return simulationGame
    }

    fun simulatePlayGameState(gameState: BasicQuoridorGameState): BasicQuoridorGameState {
        return simulatePlayWithHeuristic(gameState)
    }

    fun simulatePlayWinner(gameState: BasicQuoridorGameState): Player {
        return simulatePlayWithHeuristic(gameState).winner()!!
    }
}