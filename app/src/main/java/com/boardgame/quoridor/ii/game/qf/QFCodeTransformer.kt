package com.boardgame.quoridor.ii.game.qf

import com.boardgame.quoridor.ii.MAX_NUM_OF_WALL
import com.boardgame.quoridor.ii.extension.toBinaryString
import com.boardgame.quoridor.ii.game.state.QuoridorGameState
import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.Direction
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
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

    override fun encode(state: QFState, boardSize: BoardSize): String {
        var bitBuffer = booleanArrayOf()

        bitBuffer += (state.initialState != null)
        bitBuffer += state.recordedActions.isNotEmpty()

        if (state.initialState != null) {
            val gameState = state.initialState.first
            val lastAction = state.initialState.second

            // pawn info
            val pawnLocationDataSize = getSmallestExponentOfTwoGreaterThan(boardSize.value * boardSize.value)
            val whitePawnIndex = gameState.getFirstPlayer().pawnLocation.let { it.y * boardSize.value + it.x }
            whitePawnIndex.toString(2).padStart(pawnLocationDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            val blackPawnIndex = gameState.getSecondPlayer().pawnLocation.let { it.y * boardSize.value + it.x }
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

            val wallLocationDataSize = getSmallestExponentOfTwoGreaterThan((boardSize.value - 1) * (boardSize.value - 1))
            wHWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (boardSize.value - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            wVWalls.count().toString(2).padStart(wallCountDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            wVWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (boardSize.value - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            bHWalls.count().toString(2).padStart(wallCountDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            bHWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (boardSize.value - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            bVWalls.count().toString(2).padStart(wallCountDataSize, '0').forEach {
                bitBuffer += it == '1'
            }

            bVWalls.forEach {
                val wallIndex = it.wallLocation.let { it.y * (boardSize.value - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            // last action
            bitBuffer += gameState.opponent().getPlayerIndex() == gameState.getSecondPlayer().getPlayerIndex()
            bitBuffer += lastAction is GameAction.WallPlacement
            if (lastAction is GameAction.WallPlacement) {
                val wallIndex = lastAction.wallLocation.let { it.y * (boardSize.value - 1) + it.x }
                wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                    bitBuffer += it == '1'
                }
            }

            gameState.numberOfTurn.toString(2).padStart(10, '0').forEach {
                bitBuffer += it == '1'
            }
        }

        if (state.recordedActions.isNotEmpty()) {
            state.recordedActions.size.toString(2).padStart(10, '0').forEach {
                bitBuffer += it == '1'
            }
            val wallLocationDataSize = getSmallestExponentOfTwoGreaterThan((boardSize.value - 1) * (boardSize.value - 1))
            state.recordedActions.forEach { action ->
                bitBuffer += action is GameAction.WallPlacement
                when (action) {
                    is GameAction.PawnMovement -> {
                        val direction = Direction.fromAtoB(action.oldPawnLocation, action.newPawnLocation)
                        if (direction == null) throw Exception("Invalid pawn movement exist in the recorded actions")
                        val value = direction.id
                        value.toString(2).padStart(3, '0').forEach {
                            bitBuffer += it == '1'
                        }
                    }

                    is GameAction.WallPlacement -> {
                        bitBuffer += action.orientation == Orientation.VERTICAL
                        val wallIndex = action.wallLocation.let { it.y * (boardSize.value - 1) + it.x }
                        wallIndex.toString(2).padStart(wallLocationDataSize, '0').forEach {
                            bitBuffer += it == '1'
                        }
                    }
                }
            }
        }

        return CustomBase64.encode(bitBuffer)
    }

    override fun decode(code: String, boardSize: BoardSize): QFState {
        val wallCountDataSize = getSmallestExponentOfTwoGreaterThan(MAX_NUM_OF_WALL)
        val wallLocationDataSize = getSmallestExponentOfTwoGreaterThan((boardSize.value - 1) * (boardSize.value - 1))
        val pawnLocationDataSize = getSmallestExponentOfTwoGreaterThan(boardSize.value * boardSize.value)

        var p1PawnLocation = Location(boardSize.value / 2, 0)
        var p2PawnLocation = Location(boardSize.value / 2, boardSize.value - 1)

        var cursor = 0
        val bitSet = CustomBase64.decode(code)
        val hasState = bitSet.get(cursor++)
        val hasRecord = bitSet.get(cursor++)

        val initialState = if (hasState) {
            val p1WallPlacements = mutableListOf<GameAction.WallPlacement>()
            val p2WallPlacements = mutableListOf<GameAction.WallPlacement>()
            val lastAction: GameAction

            // pawn location info
            val whitePawnLocationIndex = bitSet.get(cursor, cursor + pawnLocationDataSize).toBinaryString(pawnLocationDataSize).toInt(2)
            cursor += pawnLocationDataSize
            p1PawnLocation = Location(whitePawnLocationIndex % boardSize.value, whitePawnLocationIndex / boardSize.value)
            val blackPawnLocationIndex = bitSet.get(cursor, cursor + pawnLocationDataSize).toBinaryString(pawnLocationDataSize).toInt(2)
            cursor += pawnLocationDataSize
            p2PawnLocation = Location(blackPawnLocationIndex % boardSize.value, blackPawnLocationIndex / boardSize.value)

            // wall info
            val wHWallsCount = bitSet.get(cursor, cursor + wallCountDataSize).toBinaryString(wallCountDataSize).toInt(2)
            cursor += wallCountDataSize
            repeat(wHWallsCount) {
                val wallIndex = bitSet.get(cursor, cursor + wallLocationDataSize).toBinaryString(wallLocationDataSize).toInt(2)
                cursor += wallLocationDataSize
                p1WallPlacements.add(
                    GameAction.WallPlacement(
                        orientation = Orientation.HORIZONTAL,
                        wallLocation = Location(wallIndex % (boardSize.value - 1), wallIndex / (boardSize.value - 1))
                    )
                )
            }

            val wVWallsCount = bitSet.get(cursor, cursor + wallCountDataSize).toBinaryString(wallCountDataSize).toInt(2)
            cursor += wallCountDataSize
            repeat(wVWallsCount) {
                val wallIndex = bitSet.get(cursor, cursor + wallLocationDataSize).toBinaryString(wallLocationDataSize).toInt(2)
                cursor += wallLocationDataSize
                p1WallPlacements.add(
                    GameAction.WallPlacement(
                        orientation = Orientation.VERTICAL,
                        wallLocation = Location(wallIndex % (boardSize.value - 1), wallIndex / (boardSize.value - 1))
                    )
                )
            }

            val bHWallsCount = bitSet.get(cursor, cursor + wallCountDataSize).toBinaryString(wallCountDataSize).toInt(2)
            cursor += wallCountDataSize
            repeat(bHWallsCount) {
                val wallIndex = bitSet.get(cursor, cursor + wallLocationDataSize).toBinaryString(wallLocationDataSize).toInt(2)
                cursor += wallLocationDataSize
                p2WallPlacements.add(
                    GameAction.WallPlacement(
                        orientation = Orientation.HORIZONTAL,
                        wallLocation = Location(wallIndex % (boardSize.value - 1), wallIndex / (boardSize.value - 1))
                    )
                )
            }

            val bVWallsCount = bitSet.get(cursor, cursor + wallCountDataSize).toBinaryString(wallCountDataSize).toInt(2)
            cursor += wallCountDataSize
            repeat(bVWallsCount) {
                val wallIndex = bitSet.get(cursor, cursor + wallLocationDataSize).toBinaryString(wallLocationDataSize).toInt(2)
                cursor += wallLocationDataSize
                p2WallPlacements.add(
                    GameAction.WallPlacement(
                        orientation = Orientation.VERTICAL,
                        wallLocation = Location(wallIndex % (boardSize.value - 1), wallIndex / (boardSize.value - 1))
                    )
                )
            }

            // last action
            val lastActionMakerIndex = if (bitSet.get(cursor++)) 1 else 0
            lastAction = if (bitSet.get(cursor++)) { // WallPlacement
                val wallIndex = bitSet.get(cursor, cursor + wallLocationDataSize).toBinaryString(wallLocationDataSize).toInt(2)
                cursor += wallLocationDataSize
                val targetList = if (lastActionMakerIndex == 0) p1WallPlacements else p2WallPlacements
                targetList.first { it.wallLocation == Location(wallIndex % (boardSize.value - 1), wallIndex / (boardSize.value - 1)) }
            } else { // PawnMovement
                val pawnLocation = if (lastActionMakerIndex == 0) p1PawnLocation else p2PawnLocation
                GameAction.PawnMovement(
                    oldPawnLocation = pawnLocation,
                    newPawnLocation = pawnLocation
                )
            }

            // number of turn
            val numberOfTurn = bitSet.get(cursor, cursor + 10).toBinaryString(10).toInt(2)
            cursor += 10

            Pair(
                QuoridorGameState.createFrom(
                    boardSize = boardSize,
                    p1PawnLocation = p1PawnLocation,
                    p2PawnLocation = p2PawnLocation,
                    p1WallPlacements = p1WallPlacements,
                    p2WallPlacements = p2WallPlacements,
                    numberOfTurn = numberOfTurn
                ),
                lastAction
            )
        } else null

        val initialGameState = initialState?.first?.deepCopy() ?: QuoridorGameState(boardSize)

        val recordedGameActions = mutableListOf<GameAction>()
        if (hasRecord) {
            val numberOfRecord = bitSet.get(cursor, cursor + 10).toBinaryString(10).toInt(2)
            cursor += 10

            repeat(numberOfRecord) {
                if (bitSet.get(cursor++)) { // WallPlacement
                    val wallOrientation = if (bitSet.get(cursor++)) Orientation.VERTICAL else Orientation.HORIZONTAL
                    val wallIndex = bitSet.get(cursor, cursor + wallLocationDataSize).toBinaryString(wallLocationDataSize).toInt(2)
                    cursor += wallLocationDataSize

                    val wallPlacement = GameAction.WallPlacement(
                        orientation = wallOrientation,
                        wallLocation = Location(wallIndex % (boardSize.value - 1), wallIndex / (boardSize.value - 1))
                    )
                    initialGameState.executeGameAction(wallPlacement)
                    recordedGameActions.add(wallPlacement)
                } else { // PawnMovement
                    val direction = Direction.fromId(bitSet.get(cursor, cursor + 3).toBinaryString(3).toInt(2))
                    cursor += 3

                    val pawnMovement = initialGameState.getLegalPawnMovements().first {
                        direction == Direction.fromAtoB(it.oldPawnLocation, it.newPawnLocation)
                    }
                    initialGameState.executeGameAction(pawnMovement)
                    recordedGameActions.add(pawnMovement)
                }
            }
        }
        return QFState(initialState, recordedGameActions)
    }

}