package com.boardgame.quoridor.ii.model

enum class Direction(val delta: Pair<Int, Int>) {
    N(Pair(0, -1)),
    S(Pair(0, 1)),
    W(Pair(-1, 0)),
    E(Pair(1, 0)),
    NW(Pair(-1, -1)),
    NE(Pair(1, -1)),
    SW(Pair(-1, 1)),
    SE(Pair(1, 1)),
}