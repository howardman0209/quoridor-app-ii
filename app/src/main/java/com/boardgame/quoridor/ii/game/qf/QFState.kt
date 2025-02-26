package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.game.state.QuoridorGameState
import com.boardgame.quoridor.ii.model.GameAction

data class QFState(
    val initialState: Pair<QuoridorGameState, GameAction>?,
    val recordedActions: List<GameAction>
)