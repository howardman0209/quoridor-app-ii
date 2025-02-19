package com.boardgame.quoridor.ii.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.GameState
import com.boardgame.quoridor.ii.model.Location

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameState = GameState(size = 9)
        val newGameState = gameState.clone()
        gameState.takeGameAction(GameAction.PawnMovement(gameState.player().pawnLocation, Location(4, 1)))
        Log.d("MainApplication", "gameState: $gameState")
        Log.d("MainApplication", "gameState: ${gameState.hashCode()}, ${newGameState.hashCode()}")
        Log.d("MainApplication", "gameState = newGameState: ${gameState == newGameState}")
    }
}