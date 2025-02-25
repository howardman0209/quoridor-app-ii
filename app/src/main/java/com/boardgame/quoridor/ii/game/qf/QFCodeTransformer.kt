package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.game.state.QuoridorGameState
import com.boardgame.quoridor.ii.model.GameAction
import kotlin.math.ceil
import kotlin.math.ln

/**
 * A helper class to transform game-state to qr-code.
 * QF code format: https://www.quoridorfansite.com/tools/qfb.html
 */
object QFCodeTransformer : BasicGameStateTransformer<QuoridorGameState> {
    private enum class QFCodeField {
        STATE,
        RECORD,
        WHITE_PAWN_LOCATION,
        BLACK_PAWN_LOCATION,
        WHITE_HORIZONTAL_WALL_COUNT,
        WHITE_VERTICAL_WALL_COUNT,
        BLACK_HORIZONTAL_WALL_COUNT,
        BLACK_VERTICAL_WALL_COUNT,
        WALL_LOCATION,
        PAWN_OR_WALL,
        TURN_NUMBER
    }

    private fun getSmallestExponentOfTwoGreaterThan(target: Int): Int {
        val log2OfTarget = ln(target.toDouble()) / ln(2.0)
        return ceil(log2OfTarget).toInt()
    }

    private fun initFieldToSizeMap(initialGameState: QuoridorGameState, recordedActions: List<GameAction>): Map<QFCodeField, Int> {
        val size = initialGameState.size
        val pawnLocationDataSize = getSmallestExponentOfTwoGreaterThan(size * size)
        val wallLocationDataSize = getSmallestExponentOfTwoGreaterThan((size - 1) * (size - 1))
        val wallCountDataSize = getSmallestExponentOfTwoGreaterThan(initialGameState.maxNumOfWallPerPlayer)

        return mapOf(
            QFCodeField.STATE to 1,
            QFCodeField.RECORD to 1,
            QFCodeField.WHITE_PAWN_LOCATION to pawnLocationDataSize,
            QFCodeField.BLACK_PAWN_LOCATION to pawnLocationDataSize,
            QFCodeField.WHITE_HORIZONTAL_WALL_COUNT to wallCountDataSize,
            QFCodeField.WHITE_VERTICAL_WALL_COUNT to wallCountDataSize,
            QFCodeField.BLACK_HORIZONTAL_WALL_COUNT to wallCountDataSize,
            QFCodeField.BLACK_VERTICAL_WALL_COUNT to wallCountDataSize,
            QFCodeField.WALL_LOCATION to wallLocationDataSize,
//            QFCodeField.LAST_MOVE
        )
    }

    override fun encode(initialGameState: QuoridorGameState, recordedActions: List<GameAction>): String {
        TODO("Not yet implemented")
    }

    override fun decode(code: String): Pair<QuoridorGameState, List<GameAction>> {
        TODO("Not yet implemented")
    }
}