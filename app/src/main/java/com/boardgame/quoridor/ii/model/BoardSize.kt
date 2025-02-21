package com.boardgame.quoridor.ii.model

enum class BoardSize(val value: Int) {
    SIZE_7(7), SIZE_9(9), SIZE_11(11), SIZE_13(13);

    companion object {
        fun fromInt(value: Int): BoardSize {
            return entries.find { it.value == value }
                ?: throw IllegalArgumentException("Invalid board size: $value")
        }
    }
}