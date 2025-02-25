package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.GameAction

interface BasicGameStateTransformer<T> {
    fun encode(initialState: Pair<T, GameAction>?, recordedActions: List<GameAction>): String

    fun decode(code: String, boardSize: BoardSize): Pair<T, List<GameAction>>
}