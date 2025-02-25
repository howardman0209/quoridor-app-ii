package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.extension.decodeBase64ToBitSet
import com.boardgame.quoridor.ii.game.state.QuoridorGameState
import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
import kotlin.math.ceil
import kotlin.math.ln

/**
 * A helper class to transform game-state to qr-code.
 * QF code format: https://www.quoridorfansite.com/tools/qfb.html
 */
object QFCodeTransformer : BasicGameStateTransformer<QuoridorGameState> {
    private data class QFData(
        val state: State? = null
    ) {
        data class State(
            val whitePawnLocation: Location,
            val blackPawnLocation: Location,
            val whiteWallPlacements: List<GameAction.WallPlacement>,
            val blackWallPlacements: List<GameAction.WallPlacement>
        )
    }

    private fun getSmallestExponentOfTwoGreaterThan(target: Int): Int {
        val log2OfTarget = ln(target.toDouble()) / ln(2.0)
        return ceil(log2OfTarget).toInt()
    }

    private fun initFieldToSizeMap(initialGameState: QuoridorGameState, recordedActions: List<GameAction>) {
        val size = initialGameState.size
        val pawnLocationDataSize = getSmallestExponentOfTwoGreaterThan(size * size)
        val wallLocationDataSize = getSmallestExponentOfTwoGreaterThan((size - 1) * (size - 1))
        val wallCountDataSize = getSmallestExponentOfTwoGreaterThan(initialGameState.maxNumOfWallPerPlayer)
        val allWallPlacements = initialGameState.getAllWallPlacements()
        val whiteHorizontalWallCount = allWallPlacements
    }

    override fun encode(initialState: Pair<QuoridorGameState, GameAction>?, recordedActions: List<GameAction>): String {
        TODO("Not yet implemented")
    }

    override fun decode(code: String, boardSize: BoardSize): Pair<QuoridorGameState, List<GameAction>> {
        val bitSet = code.decodeBase64ToBitSet()
        TODO("Not yet implemented")
    }
}