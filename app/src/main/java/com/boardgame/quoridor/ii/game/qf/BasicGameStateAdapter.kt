package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.model.BoardSize

interface BasicGameStateAdapter<T> {
    fun encode(state: T): String

    fun decode(code: String, boardSize: BoardSize): T
}