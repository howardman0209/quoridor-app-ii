package com.boardgame.quoridor.ii.ai

import android.util.Log
import com.boardgame.quoridor.ii.game.QuoridorGameState
import com.boardgame.quoridor.ii.model.Player

object AIHelper {
    private const val TAG = "AIHelper"

    /**
     * heuristic is added into the game play simulation
     * 1. if opponent has no wall remains, only choose the next move in the shortest path to goal
     */
    private inline fun <T> simulatePlayWithHeuristic(
        gameState: QuoridorGameState,
        crossinline onTermination: (QuoridorGameState) -> T
    ): T {
        val simulationGame = gameState.deepCopy()

        generateSequence {
            if (simulationGame.opponent().remainingWalls <= 0) {
                return@generateSequence simulationGame.getLegalPawnMovementToNearestGoal()
            }

            simulationGame.getRandomLegalGameAction()
        }.forEach { action ->
//            Log.d(TAG, "simulationGame: $simulationGame") // this log significantly time consuming
            simulationGame.executeGameAction(action)
            if (simulationGame.winner() != null) {
                return onTermination(simulationGame)
            }
        }

        // should not reach here
        throw IllegalStateException("Game simulation did not produce a winner")
    }

    /**
     * Pure random game play simulation
     */
    private inline fun <T> simulatePlayPureRandom(
        gameState: QuoridorGameState,
        crossinline onTermination: (QuoridorGameState) -> T
    ): T {
        val simulationGame = gameState.deepCopy()

        generateSequence {
            simulationGame.getRandomLegalGameAction()
        }.forEach { action ->
//            Log.d(TAG, "simulationGame: $simulationGame") // this log significantly time consuming
            simulationGame.executeGameAction(action)
            if (simulationGame.winner() != null) {
                return onTermination(simulationGame)
            }
        }

        // should not reach here
        throw IllegalStateException("Game simulation did not produce a winner")
    }

    fun simulatePlayGameState(gameState: QuoridorGameState): QuoridorGameState {
        return simulatePlayWithHeuristic(gameState) { it }
    }

    fun simulatePlayWinner(gameState: QuoridorGameState): Player {
        return simulatePlayWithHeuristic(gameState) { it.winner()!! }
    }
}