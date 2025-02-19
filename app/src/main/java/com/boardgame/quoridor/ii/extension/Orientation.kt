package com.boardgame.quoridor.ii.extension

import com.boardgame.quoridor.ii.model.Orientation

fun Orientation.getFlipped(): Orientation {
    return when (this) {
        Orientation.HORIZONTAL -> Orientation.VERTICAL
        Orientation.VERTICAL -> Orientation.HORIZONTAL
    }
}