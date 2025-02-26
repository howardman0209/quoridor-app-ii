package com.boardgame.quoridor.ii.model

/** ^N
 *     >E
 */
enum class Direction(val id: Int, val delta: Pair<Int, Int>) {
    N(0, Pair(0, 1)),
    NE(1, Pair(1, 1)),
    E(2, Pair(1, 0)),
    SE(3, Pair(1, -1)),
    S(4, Pair(0, -1)),
    SW(5, Pair(-1, -1)),
    W(6, Pair(-1, 0)),
    NW(7, Pair(-1, 1));

    companion object {
        fun fromId(id: Int): Direction? {
            return Direction.entries.find { it.id == id }
        }

        fun fromAtoB(locationA: Location, locationB: Location): Direction? {
            val deltaX = locationB.x - locationA.x
            val deltaY = locationB.y - locationA.y

            return when {
                deltaX == 0 && deltaY > 0 -> N  // North
                deltaX == 0 && deltaY < 0 -> S  // South
                deltaX < 0 && deltaY == 0 -> W  // West
                deltaX > 0 && deltaY == 0 -> E  // East
                deltaX < 0 && deltaY > 0 -> NW // Northwest
                deltaX > 0 && deltaY > 0 -> NE // Northeast
                deltaX < 0 && deltaY < 0 -> SW // Southwest
                deltaX > 0 && deltaY < 0 -> SE // Southeast
                else -> null // Invalid direction (e.g., same location)
            }
        }
    }
}