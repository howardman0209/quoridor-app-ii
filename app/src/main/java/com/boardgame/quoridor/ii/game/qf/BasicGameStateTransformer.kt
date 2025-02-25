package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.model.GameAction

interface BasicGameStateTransformer<T> {
    fun encode(initialGameState: T, recordedActions: List<GameAction>): String

    fun decode(code: String): Pair<T, List<GameAction>>
}