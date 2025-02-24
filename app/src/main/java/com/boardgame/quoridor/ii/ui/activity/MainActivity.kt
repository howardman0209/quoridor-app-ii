package com.boardgame.quoridor.ii.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.boardgame.quoridor.ii.ai.mcts.HeuristicMCTSController
import com.boardgame.quoridor.ii.extension.toNotation
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.game.state.QuoridorGameState
import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.util.DebugUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Stack

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val gameState = QuoridorGameState(BoardSize.SIZE_9)
//        gameState.executeGameAction(GameAction.PawnMovement(gameState.player().pawnLocation, Location(5, 4)))
//        gameState.executeGameAction(GameAction.PawnMovement(gameState.player().pawnLocation, Location(4, 4)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.VERTICAL, Location(3, 3)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(4, 4)))
//
//        val legalPawnMovements = gameState.getLegalPawnMovements()
//        legalPawnMovements.forEach {
//            Log.d("MainActivity", "legalPawnMovements: ${it.toNotation()}")
//        }

//        val notations = listOf("d4h","f9","f1","a4v","d8h","h5h","g7h","e7h","f2","f8","c1h","h2h","g2","g8","f2","f8","c5v","g8","f1","h1h","b5v","h8","f2","d2v","f3","a7h","b8v","e5h","h8h","g8","f4","c7h","f5","g9","f4","f9","f3")
//        notations.forEach {
//            GameAction.fromNotation(it)?.let { action ->
//                gameState.executeGameAction(action)
//            }
//        }
//        Log.d("MainActivity", "gameState: $gameState")
//        val isLegalWallPlacement = gameState.isLegalWallPlacement(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(7, 6)))
//        Log.d("MainActivity", "isLegalWallPlacement: $isLegalWallPlacement")

        CoroutineScope(Dispatchers.Default).launch {
            val gameState = QuoridorGameState(BoardSize.SIZE_9)
            val mctsController = HeuristicMCTSController()
            val gameActionStack = Stack<GameAction>()
            fun executeGameAction(action: GameAction) {
                gameState.executeGameAction(action)
                gameActionStack.push(action)
            }

            DebugUtil.measureExecutionTime {
//                var simCount = 0
//                while (simCount < 1) {
//                    val simGame = AIHelper.simulatePlayGameState(gameState)
//                    Log.d("simulation", "#${++simCount} simGame: $simGame")
//                }

//                executeGameAction(GameAction.fromNotation("e2")!!)
//                executeGameAction(GameAction.fromNotation("e8")!!)
//                executeGameAction(GameAction.fromNotation("e3")!!)
//                executeGameAction(GameAction.fromNotation("e7")!!)
//                executeGameAction(GameAction.fromNotation("e4")!!)
//                executeGameAction(GameAction.fromNotation("e6")!!)

                while (!gameState.isTerminated()) {
                    val bestAction = mctsController.search(gameState, 5000)
                    Log.d("@@@", "bestAction: ${bestAction.toNotation()}")
                    executeGameAction(bestAction)
                    Log.d("@@@", "gameState: $gameState winner: ${gameState.winner()}")
                }
            }
            Log.d("@@@", "gameActionStack: ${gameActionStack.map { it.toNotation() }}")
        }
    }
}