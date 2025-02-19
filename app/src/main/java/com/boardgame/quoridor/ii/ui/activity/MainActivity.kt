package com.boardgame.quoridor.ii.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.GameState
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Orientation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameState = GameState(size = 9)
        val newGameState = gameState.clone()
        gameState.takeGameAction(GameAction.PawnMovement(gameState.player().pawnLocation, Location(4, 1)))
        gameState.takeGameAction(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(4, 1)))
        gameState.takeGameAction(GameAction.WallPlacement(Orientation.VERTICAL, Location(1, 1)))
        Log.d("MainActivity", "gameState: $gameState")
        Log.d("MainActivity", "gameState: ${gameState.hashCode()}, ${newGameState.hashCode()}")
        Log.d("MainActivity", "gameState = newGameState: ${gameState == newGameState}")
        val isLegalWallPlacement = gameState.isLegalWallPlacement(GameAction.WallPlacement(Orientation.HORIZONTAL, Location(2, 1)))
        Log.d("MainActivity", "isLegalWallPlacement: $isLegalWallPlacement")
    }
}