package com.boardgame.quoridor.ii.extension

import com.boardgame.quoridor.ii.A_TO_Z
import com.boardgame.quoridor.ii.model.Location

fun Location.toNotation(): String {
    val column = A_TO_Z[this.x]
    val row = this.y + 1
    return "$column$row"
}