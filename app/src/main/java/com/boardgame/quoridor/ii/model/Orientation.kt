package com.boardgame.quoridor.ii.model

enum class Orientation(val notation: Char) {
    HORIZONTAL('h'),
    VERTICAL('v');

    companion object {
        fun fromNotation(notation: Char): Orientation? {
            return Orientation.entries.find { it.notation == notation }
        }
    }
}