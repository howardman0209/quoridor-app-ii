package com.boardgame.quoridor.ii.ai

import android.util.Log
import com.boardgame.quoridor.ii.game.QuoridorGameState
import com.boardgame.quoridor.ii.model.Player

object AIHelper {
    /**
     * heuristic is added into the game play simulation
     * 1.
     */
    private inline fun <T> simulatePlay(
        gameState: QuoridorGameState,
        crossinline onTermination: (QuoridorGameState) -> T
    ): T {
        val simulationGame = gameState.deepCopy()

        generateSequence { simulationGame.getRandomLegalGameAction() }
            .forEach { action ->
                simulationGame.executeGameAction(action)
                if (simulationGame.winner() != null) {
                    return onTermination(simulationGame)
                }
            }

        // should not reach here
        throw IllegalStateException("Game simulation did not produce a winner")
    }

    fun simulatePlayGameState(gameState: QuoridorGameState): QuoridorGameState {
        return simulatePlay(gameState) { it }
    }

    fun simulatePlayWinner(gameState: QuoridorGameState): Player {
        return simulatePlay(gameState) { it.winner()!! }
    }
}