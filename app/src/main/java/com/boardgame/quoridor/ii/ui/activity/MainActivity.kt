package com.boardgame.quoridor.ii.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.boardgame.quoridor.ii.ai.SimulationHelper
import com.boardgame.quoridor.ii.ai.mcts.HeuristicMCTSController
import com.boardgame.quoridor.ii.extension.toNotation
import com.boardgame.quoridor.ii.game.qf.QFCodeTransformer
import com.boardgame.quoridor.ii.game.qf.QFState
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

//        val orgBase64Str = "yzoAAIBQEAAA" // hswBSF0FIBA // QGBCbVsg // yzoAAIBQEAAA
//        val bitSet = orgBase64Str.decodeBase64ToBitSet()
//        Log.d("@@@", "binaryStr: ${bitSet.toBinaryString(72)}")
//        val encodedBase64Str = bitSet.encodeToBase64String(12)
//        Log.d("@@@", "encodedBase64Str: $encodedBase64Str")

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
//                    val simGame = SimulationHelper.simulatePlayGameState(gameState)
//                    Log.d("simulation", "#${++simCount} simGame: $simGame")
//                }

                while (!gameState.isTerminated() && gameState.numberOfTurn < 20) {
                    val bestAction = mctsController.search(gameState, 1000)
                    Log.d("@@@", "bestAction: ${bestAction.toNotation()}")
                    executeGameAction(bestAction)
                    Log.d("@@@", "gameState: $gameState winner: ${gameState.winner()}")
                }
                val qfCode = QFCodeTransformer.encode(
                    QFState(
                        initialState = null, // Pair(gameState, gameActionStack.last()),
                        recordedActions = gameActionStack.toList() // emptyList()
                    ),
                    BoardSize.SIZE_9
                )
                Log.d("@@@", "qfCode: $qfCode")
            }
            Log.d("@@@", "gameActionStack: ${gameActionStack.map { it.toNotation() }}")
        }
    }
}