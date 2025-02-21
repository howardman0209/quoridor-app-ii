package com.boardgame.quoridor.ii.game

import com.boardgame.quoridor.ii.model.GameAction

interface GameStateProperty<T> {

    fun getLegalGameActions(): List<GameAction>

    fun executeGameAction(action: GameAction)

    fun isTerminated(): Boolean

    fun deepCopy(): T

    val stringRepresentation: String
}