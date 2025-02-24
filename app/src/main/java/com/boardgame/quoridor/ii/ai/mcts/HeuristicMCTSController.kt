package com.boardgame.quoridor.ii.ai.mcts

import com.boardgame.quoridor.ii.game.QuoridorGameState
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location

class HeuristicMCTSController : MCTSController() {
    override fun getGameActionListForExpansion(currentNode: MCTSNode): List<GameAction> {
        val currentGameState = currentNode.gameState as QuoridorGameState

        // opening heuristics
        if (currentGameState.numberOfTurn <= 6) {
            return listOf(currentGameState.getLegalPawnMovementToNearestGoal())
        }

//        if (currentGameState.player().pawnLocation == Location(4, 4)
//            && currentGameState.opponent().pawnLocation == Location(4, 6)
//            && currentGameState.player().remainingWalls == 10 && currentGameState.opponent().remainingWalls == 10
//        ) {
//            listOf()
//        }


        if (currentGameState.player().remainingWalls <= 0) {
            val wallActionList = if (currentGameState.player().remainingWalls > 0) currentGameState.getEffectiveWallPlacements() else emptyList()
            return listOf(currentGameState.getLegalPawnMovementToNearestGoal()) + wallActionList
        }

        val wallActionList = if (currentGameState.player().remainingWalls > 0) currentGameState.getEffectiveWallPlacements() else emptyList()
        return currentGameState.getLegalPawnMovements() + wallActionList
    }
}