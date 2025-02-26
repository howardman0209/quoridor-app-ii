package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Orientation
import kotlin.math.ceil
import kotlin.math.ln

/**
 * A helper class to transform game-state to qr-code.
 * QF code format: https://www.quoridorfansite.com/tools/qfb.html
 */
object QFCodeTransformer : BasicGameStateAdapter<QFState> {
    private fun getSmallestExponentOfTwoGreaterThan(target: Int): Int {
        val log2OfTarget = ln(target.toDouble()) / ln(2.0)
        return ceil(log2OfTarget).toInt()
    }

    override fun encode(state: QFState): String {
        var bitBuffer = booleanArrayOf()

        bitBuffer += (state.initialState != null)
        bitBuffer += state.recordedActions.isNotEmpty()

        if (state.initialState != null) {
            val gameState = state.initialState.first
            val lastAction = state.initialState.second

            // pawn info
            val pawnLocationDataSize = getSmallestExponentOfTwoGreaterThan(gameState.size * gameState.size)
            val whitePawnIndex = gameState.getFirstPlayer().pawnLocation.let { it.y * gameState.size + it.x }
            whitePawnIndex.toString(2).padStart(pawnLocationDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            val blackPawnIndex = gameState.getSecondPlayer().pawnLocation.let { it.y * gameState.size + it.x }
            blackPawnIndex.toString(2).padStart(pawnLocationDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            // placed wall info
            val allWallPlacements = gameState.getAllWallPlacements()
            val wHWalls = allWallPlacements.filter { it.second == 0 && it.first.orientation == Orientation.HORIZONTAL }.map { it.first }
            val wVWalls = allWallPlacements.filter { it.second == 0 && it.first.orientation == Orientation.VERTICAL }.map { it.first }
            val bHWalls = allWallPlacements.filter { it.second == 1 && it.first.orientation == Orientation.HORIZONTAL }.map { it.first }
            val bVWalls = allWallPlacements.filter { it.second == 1 && it.first.orientation == Orientation.VERTICAL }.map { it.first }

            val wallCountDataSize = getSmallestExponentOfTwoGreaterThan(gameState.maxNumOfWallPerPlayer)
            wHWalls.count().toString(2).padStart(wallCountDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            val wallLocationDataSize = getSmallestExponentOfTwoGreaterThan((gameState.size - 1) * (gameState.size - 1))
            wHWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (gameState.size - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            wVWalls.count().toString(2).padStart(wallCountDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            wVWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (gameState.size - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            bHWalls.count().toString(2).padStart(wallCountDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            bHWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (gameState.size - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            bVWalls.count().toString(2).padStart(wallCountDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            bVWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (gameState.size - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            // last action
            bitBuffer += gameState.opponent().getPlayerIndex() == gameState.getSecondPlayer().getPlayerIndex()
            bitBuffer += lastAction is GameAction.WallPlacement
            if (lastAction is GameAction.WallPlacement) {
                val wallIndex = lastAction.wallLocation.let { it.y * (gameState.size - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            gameState.numberOfTurn.toString(2).padStart(10, '0').forEach {
                bitBuffer += it == '1'
            }
        }

        if (state.recordedActions.isNotEmpty()) {

        }

        return CustomBase64.encode(bitBuffer)
    }

    override fun decode(code: String, boardSize: BoardSize): QFState {
        TODO("Not yet implemented")
    }

}