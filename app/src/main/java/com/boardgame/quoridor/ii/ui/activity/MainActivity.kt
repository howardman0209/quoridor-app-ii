package com.boardgame.quoridor.ii.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.boardgame.quoridor.ii.ai.AIHelper
import com.boardgame.quoridor.ii.ai.mcts.MCTSController
import com.boardgame.quoridor.ii.extension.toNotation
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.game.QuoridorGameState
import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Orientation
import com.boardgame.quoridor.ii.util.DebugUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val gameState = QuoridorGameState(size = 9)
//        gameState.executeGameAction(GameAction.PawnMovement(gameState.player().pawnLocation, Location(4, 1)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(0, 1)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(2, 1)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(4, 1)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.VERTICAL, Location(5, 1)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(5, 2)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(4, 0)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(7, 7)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(5, 7)))
//        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(7, 2)))
//        Log.d("MainActivity", "gameState: $gameState")

//        DebugUtil.measureExecutionTime {
//            Log.d("MainActivity", "isExistPathToGoal: ${gameState.isExistPathToGoal()}")
//        }
//
//        DebugUtil.measureExecutionTime {
//            Log.d("MainActivity", "getShortestPathToGoal: ${gameState.getShortestPathToGoal()}")
//        }

//        DebugUtil.measureExecutionTime {
//            val isLegalWallPlacement = gameState.isLegalWallPlacement(GameAction.WallPlacement(Orientation.VERTICAL, Location(0, 0)))
//            Log.d("MainActivity", "isLegalWallPlacement: $isLegalWallPlacement")
//        }

//        DebugUtil.measureExecutionTime {
//            val legalGameActions = gameState.getLegalGameActions()
//            legalGameActions.forEachIndexed { idx, action ->
//                Log.d("MainActivity", "#$idx: ${action.toNotation()} $action")
//            }
//        }

        CoroutineScope(Dispatchers.Default).launch {
            val gameState = QuoridorGameState(BoardSize.SIZE_9)
            DebugUtil.measureExecutionTime {
                var simCount = 0
                while (true) {
                    val simGame = AIHelper.simulatePlayGameState(gameState)
                    Log.d("simulation", "simGame: $simGame")
                }

//                val bestAction = MCTSController.search(gameState, 5)
//                Log.d("@@@", "bestAction: $bestAction")
            }
        }
    }
}