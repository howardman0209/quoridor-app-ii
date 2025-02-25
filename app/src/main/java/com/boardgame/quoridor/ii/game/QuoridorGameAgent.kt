package com.boardgame.quoridor.ii.game

import com.boardgame.quoridor.ii.game.state.QuoridorGameState
import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
import java.util.Stack

class QuoridorGameAgent(boardSize: BoardSize) {
    private var currentGameState = QuoridorGameState(boardSize)
    private val executedActionStack = Stack<GameAction>()
    private val reversedActionStack = Stack<GameAction>()
    private var gameStateListener: GameStateListener? = null

    fun doGameAction(action: GameAction) {
        currentGameState.executeGameAction(action)
        executedActionStack.push(action)
        notifyListener()
    }

    fun undoLastGameAction() {
        if (executedActionStack.isNotEmpty()) {
            val lastExecutedAction = executedActionStack.pop()
            val previousPawnMovement = executedActionStack.lastOrNull {
                it is GameAction.PawnMovement && executedActionStack.indexOf(it) == currentGameState.opponent().getPlayerIndex()
            } ?: GameAction.PawnMovement(Location(currentGameState.size / 2, currentGameState.player().goalY)) // initial pawn location

            currentGameState.reverseGameAction(previousPawnMovement)
            reversedActionStack.push(lastExecutedAction)
            notifyListener()
        }
    }

    fun redoLastGameAction() {
        if (reversedActionStack.isNotEmpty()) {
            val lastReversedAction = reversedActionStack.pop()
            doGameAction(lastReversedAction)
        }
    }

    fun setGameStateListener(listener: GameStateListener) {
        this.gameStateListener = listener
    }

    private fun notifyListener() {
        gameStateListener?.onUpdated(currentGameState)
    }

    interface GameStateListener {
        fun onUpdated(newState: QuoridorGameState)
    }
}