package com.boardgame.quoridor.ii.model

/** ^N
 *     >E
 */
enum class Direction(val delta: Pair<Int, Int>) {
    N(Pair(0, 1)),
    S(Pair(0, -1)),
    W(Pair(-1, 0)),
    E(Pair(1, 0)),
    NW(Pair(-1, 1)),
    NE(Pair(1, 1)),
    SW(Pair(-1, -1)),
    SE(Pair(1, -1));

    companion object {
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