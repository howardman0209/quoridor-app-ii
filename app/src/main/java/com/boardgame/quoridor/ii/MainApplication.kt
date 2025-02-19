package com.boardgame.quoridor.ii

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.boardgame.quoridor.ii.model.GameState

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MainApplication", "app onCreate")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val gameState = GameState(
            size = 9
        )


    }
}