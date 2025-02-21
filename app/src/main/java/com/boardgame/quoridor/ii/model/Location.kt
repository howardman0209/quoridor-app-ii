package com.boardgame.quoridor.ii.model

import com.boardgame.quoridor.ii.A_TO_Z

data class Location(val x: Int, val y: Int) {
    companion object {
        fun fromNotation(notation: String): Location? {
            return try {
                Location(x = A_TO_Z.indexOf(notation.first()), "${notation.last() - 1}".toInt())
            } catch (_: Exception) {
                null
            }
        }
    }
}
