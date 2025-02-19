package com.boardgame.quoridor.ii.extension

import com.boardgame.quoridor.ii.model.Direction


fun Direction.getDelta(step: Int): Pair<Int, Int> {
    return Pair(this.delta.first * step, this.delta.second * step)
}

fun Direction.getInverted(): Direction {
    return when (this) {
        Direction.N -> Direction.S
        Direction.S -> Direction.N
        Direction.W -> Direction.E
        Direction.E -> Direction.W
        Direction.NW -> Direction.SE
        Direction.NE -> Direction.SW
        Direction.SW -> Direction.NE
        Direction.SE -> Direction.NW
    }
}

fun Direction.getNearBy(): List<Direction> {
    return when (this) {
        Direction.N -> listOf(Direction.NE, Direction.NW)
        Direction.S -> listOf(Direction.SE, Direction.SW)
        Direction.W -> listOf(Direction.NW, Direction.SW)
        Direction.E -> listOf(Direction.NE, Direction.SE)
        Direction.NW -> listOf(Direction.N, Direction.W)
        Direction.NE -> listOf(Direction.N, Direction.E)
        Direction.SW -> listOf(Direction.S, Direction.W)
        Direction.SE -> listOf(Direction.S, Direction.E)
    }
}