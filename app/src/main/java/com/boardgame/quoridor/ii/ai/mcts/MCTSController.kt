package com.boardgame.quoridor.ii.ai.mcts

import android.util.Log
import com.boardgame.quoridor.ii.ai.SimulationHelper
import com.boardgame.quoridor.ii.extension.orZero
import com.boardgame.quoridor.ii.extension.toNotation
import com.boardgame.quoridor.ii.game.state.QuoridorGameState
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Player
import kotlin.math.ln
import kotlin.math.sqrt

open class MCTSController {
    companion object {
        private const val TAG = "MCTS"
        var enableProgressDebugLog = true

        private fun debugLog(message: String) {
            if (enableProgressDebugLog) {
                Log.i(TAG, message)
            }
        }
    }

    protected class MCTSNode(
        val gameState: QuoridorGameState,
        val executedAction: GameAction? = null,
        val parent: MCTSNode? = null
    ) {
        private val children: MutableList<MCTSNode> = mutableListOf()
        var visits = 0
        var score = 0.0
        val winRate: Double
            get() = score / visits


        fun isLeafNode(): Boolean = children.isEmpty()

        fun isNewNode(): Boolean = visits == 0

        fun isRootNode(): Boolean = parent == null && executedAction == null

        fun addChildren(children: List<MCTSNode>) {
            this.children.addAll(children)
        }

        fun selectChild(explorationWeight: Double = 1.41, heuristicScore: Double? = null): MCTSNode {
            return children.maxBy {
                val selectionRating = if (it.visits != 0) {
                    val exploitation = it.score / it.visits
                    val exploration = sqrt(ln(visits.toDouble()) / it.visits)
                    exploitation + exploration * explorationWeight + heuristicScore.orZero()
                } else {
                    Double.MAX_VALUE
                }

//                debugLog("${it.executedAction?.toNotation().orEmpty()}: $selectionRating")
                selectionRating
            }
        }

        fun selectBestChild(): MCTSNode {
            return children.maxBy { it.visits }
        }

        fun backpropagation(score: Int) {
            var currentNode: MCTSNode? = this

            while (currentNode != null) {
                currentNode.visits++
                currentNode.score += score
                currentNode = currentNode.parent
            }
        }

        fun dump(layer: Int = 0) {
            debugLog("x${"-".repeat(layer)}> ${executedAction?.toNotation().orEmpty()} children (${children.count()}), visit: $visits, score: $score, winRate: $winRate")
            this.children.sortedByDescending { it.visits }.forEach {
                if (layer > 0) return
                it.dump(layer + 1)
            }
        }
    }

    protected open fun getExplorationWeightForSelection(): Double = 1.41

    protected open fun getHeuristicScoreForSelection(): Double = 0.0

    protected open fun getGameActionListForExpansion(currentNode: MCTSNode): List<GameAction> {
        return currentNode.gameState.getLegalGameActions().shuffled()
    }

    protected open fun getGameWinnerForSimulation(currentNode: MCTSNode): Player {
        return SimulationHelper.simulatePlayWinner(currentNode.gameState)
    }

    fun search(initialState: QuoridorGameState, iterations: Int = 1000): GameAction {
        val currentState = initialState.deepCopy()
        val root = MCTSNode(currentState)

        var node: MCTSNode
        repeat(iterations) {
            // Selection
            node = root
            while (!node.isLeafNode()) {
                node = node.selectChild(explorationWeight = getExplorationWeightForSelection(), heuristicScore = getHeuristicScoreForSelection())
            }
//            debugLog("#$it Selection ${node.executedAction?.toNotation().orEmpty()}")

            if ((node.isRootNode() || !node.isNewNode()) // always expand a root node and a new node
                && !node.gameState.isTerminated() // always not to expand a node with terminate game state
            ) {
                // Expansion
//                debugLog("#$it Expansion")
                val legalGameActionList = getGameActionListForExpansion(node).shuffled()
                val children = legalGameActionList.map {
                    val newState = node.gameState.deepCopy()
                    newState.executeGameAction(it)
                    MCTSNode(gameState = newState, executedAction = it, parent = node)
                }
                node.addChildren(children)
            } else {
                // Simulation (Rollout)
                val winner = getGameWinnerForSimulation(node)
                val score = if (winner.getPlayerIndex() == currentState.player().getPlayerIndex()) 1 else 0
//                debugLog("#$it Rollout -> $score")

                // Backpropagation
//                debugLog("#$it Backpropagation")
                node.backpropagation(score)
            }
        }

        root.dump()
        return root.selectBestChild().executedAction ?: throw Exception("Abnormal! No executed action found in child node")
    }
}