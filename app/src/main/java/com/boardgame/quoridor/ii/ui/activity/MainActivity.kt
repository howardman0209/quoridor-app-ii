package com.boardgame.quoridor.ii.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.boardgame.quoridor.ii.extension.toNotation
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.game.QuoridorGameState
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Orientation
import com.boardgame.quoridor.ii.util.DebugUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameState = QuoridorGameState(size = 9)
        val newGameState = gameState.deepCopy()
        gameState.executeGameAction(GameAction.PawnMovement(gameState.player().pawnLocation, Location(4, 1)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(0, 1)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(2, 1)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(4, 1)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.VERTICAL, Location(5, 1)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(5, 2)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(4, 0)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(7, 7)))
        gameState.executeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(5, 7)))
        Log.d("MainActivity", "gameState: $gameState")
        Log.d("MainActivity", "gameState: ${gameState.hashCode()}, ${newGameState.hashCode()}")
        Log.d("MainActivity", "gameState = newGameState: ${gameState == newGameState}")

        DebugUtil.measureExecutionTime {
            Log.d("MainActivity", "sd for player: ${gameState.getShortestPathToGoal()}")
        }

        DebugUtil.measureExecutionTime {
            Log.d("MainActivity", "sd for opponent: ${gameState.getShortestPathToGoal(false)}")
        }
//        val isLegalWallPlacement = gameState.isLegalWallPlacement(GameAction.WallPlacement(Orientation.VERTICAL, Location(5, 0)))
//        Log.d("MainActivity", "isLegalWallPlacement: $isLegalWallPlacement")
//        val legalGameActions = gameState.getLegalGameActions()
//        legalGameActions.forEachIndexed { idx, action ->
//            Log.d("MainActivity", "#$idx: ${action.toNotation()} $action")
//        }
//        Log.d("MainActivity", "newGameState: $newGameState")

//        CoroutineScope(Dispatchers.IO).launch {
//            val startTime = System.currentTimeMillis()
//            while (!gameState.isTerminated()) {
//                val legalGameActions = gameState.getLegalGameActions()
//                legalGameActions.randomOrNull()?.let {
//                    gameState.executeGameAction(it)
//                    Log.d("MainActivity", "gameState: $gameState")
//                }
//            }
//            Log.d("MainActivity", "Duration: ${System.currentTimeMillis() - startTime}")
//        }
    }
}