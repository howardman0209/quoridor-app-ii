package com.boardgame.quoridor.ii.game

import android.util.Log
import com.boardgame.quoridor.ii.A_TO_Z
import com.boardgame.quoridor.ii.extension.getDelta
import com.boardgame.quoridor.ii.extension.getFlipped
import com.boardgame.quoridor.ii.extension.getNearBy
import com.boardgame.quoridor.ii.extension.toNotation
import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.Direction
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Orientation
import com.boardgame.quoridor.ii.model.Player
import com.boardgame.quoridor.ii.util.SearchUtil
import java.util.BitSet
import kotlin.math.abs
import kotlin.math.min

class QuoridorGameState(boardSize: BoardSize) : BasicQuoridorGameState() {
    val size = boardSize.value
    var numberOfTurn: Int = 1
        private set

    /**
     * occupancy info (0: empty, 1: placed)
     * orientation info (0: horizontal, 1: vertical)
     * placer info (0: player1, 1: player2)
     */
    private val placedWalls: BitSet = BitSet((size - 1) * (size - 1) * 3)

    private val players = listOf(
        Player( // P1
            goalY = size - 1,
            pawnLocation = Location(size / 2, 0),
            remainingWalls = 10
        ),
        Player( // P2
            goalY = 0,
            pawnLocation = Location(size / 2, size - 1),
            remainingWalls = 10
        )
    )

    override fun player(): Player = players[(numberOfTurn + 1) % 2]

    override fun opponent(): Player = players[numberOfTurn % 2]

    override fun winner(): Player? {
        return players.firstOrNull { it.hasReachedGoal() }
    }

    private fun movePawn(location: Location, forPlayer: Boolean = true) {
        val actionMaker = if (forPlayer) player() else opponent()
        actionMaker.pawnLocation = location
    }

    private fun getWallIndex(location: Location): Int {
        return (location.x + location.y * (size - 1)) * 3
    }

    private fun placeWall(location: Location, orientation: Orientation, forPlayer: Boolean = true) {
        val actionMaker = if (forPlayer) player() else opponent()
        val wallIndex = getWallIndex(location)
        // set occupancy
        placedWalls.set(wallIndex)

        // set orientation
        if (orientation != Orientation.HORIZONTAL) {
            placedWalls.set(wallIndex + 1)
        }

        // set placer
        if (actionMaker.getPlayerIndex() == 1) {
            placedWalls.set(wallIndex + 2)
        }
        actionMaker.remainingWalls--
    }

    private fun removeWall(location: Location, forPlayer: Boolean = true) {
        val actionMaker = if (forPlayer) player() else opponent()
        val wallIndex = getWallIndex(location)
        placedWalls.clear(wallIndex)
        placedWalls.clear(wallIndex + 1)
        placedWalls.clear(wallIndex + 2)

        actionMaker.remainingWalls++
    }

    override fun executeGameAction(action: GameAction) {
        when (action) {
            is GameAction.PawnMovement -> movePawn(action.newLocation)
            is GameAction.WallPlacement -> placeWall(action.location, action.orientation)
        }
        numberOfTurn++
    }

    override fun reverseGameAction(action: GameAction) {
        when (action) {
            is GameAction.PawnMovement -> {
                movePawn(action.oldLocation, false)
            }

            is GameAction.WallPlacement -> {
                removeWall(action.location, false)
            }
        }
        numberOfTurn--
    }

    override fun isLegalPawnMovement(action: GameAction.PawnMovement, forPlayer: Boolean): Boolean {
        val actionMaker = if (forPlayer) player() else opponent()
        if (action.oldLocation != actionMaker.pawnLocation) {
            return false
        }

        if (!getLegalPawnMovements(forPlayer).contains(action)) {
            return false
        }

        return true
    }

    override fun isLegalWallPlacement(action: GameAction.WallPlacement): Boolean {
        val pointOfCheck = action.location
        if (!isLegalWallLocation(pointOfCheck)) {
//            Log.e("isLegalWallPlacement", "illegal wall location")
            return false
        }

        // check crossing
        val wallIndex = getWallIndex(action.location)
        val isWallPlaced = placedWalls.get(wallIndex)
        if (isWallPlaced) {
//            Log.e("isLegalWallPlacement", "wall crossing")
            return false
        }

        // check overlapping
        if (action.orientation == Orientation.HORIZONTAL) {
            val directions = arrayOf(Direction.E, Direction.W)
            for (direction in directions) {
                val oneStepDelta = direction.getDelta(1)
                val oneStepLocation = Location(pointOfCheck.x + oneStepDelta.first, pointOfCheck.y + oneStepDelta.second)

                if (!isLegalWallLocation(oneStepLocation)) {
                    continue
                }

                val nearByWallIndex = getWallIndex(oneStepLocation)
                val isWallPlaced = placedWalls.get(nearByWallIndex)
                if (!isWallPlaced) {
                    continue
                }

                // overlapping
                val isHorizontalWall = !placedWalls.get(nearByWallIndex + 1)
                if (isHorizontalWall) {
//                    Log.e("isLegalWallPlacement", "wall overlapping")
                    return false
                }
            }
        } else {
            val directions = arrayOf(Direction.N, Direction.S)
            for (direction in directions) {
                val oneStepDelta = direction.getDelta(1)
                val oneStepLocation = Location(pointOfCheck.x + oneStepDelta.first, pointOfCheck.y + oneStepDelta.second)

                if (!isLegalWallLocation(oneStepLocation)) {
                    continue
                }

                val nearByWallIndex = getWallIndex(oneStepLocation)
                val isWallPlaced = placedWalls.get(nearByWallIndex)
                if (!isWallPlaced) {
                    continue
                }

                // overlapping
                val isVerticalWall = placedWalls.get(wallIndex + 1)
                if (isVerticalWall) {
//                    Log.e("isLegalWallPlacement", "wall overlapping")
                    return false
                }
            }
        }

        // check dead block
        if (isConnectingWall(action.location, action.orientation)) {
            val temporaryGameState = this.deepCopy()
            temporaryGameState.executeGameAction(action)
            if (!temporaryGameState.isPathToGoalExist(true)) {
//                Log.e("isLegalWallPlacement", "dead block P${temporaryGameState.player().getPlayerIndex() + 1}")
                return false
            }

            if (!temporaryGameState.isPathToGoalExist(false)) {
//                Log.e("isLegalWallPlacement", "dead block P${temporaryGameState.opponent().getPlayerIndex() + 1}")
                return false
            }
        }

        return true
    }

    /**
     * Basically, separated into 2 check case
     * 1. Perpendicular connection 2. Parallel connection
     * For Perpendicular connection => check is there any wall perpendicular joint
     * For Parallel connection => check is there any wall parallel joint
     */
    private fun isConnectingWall(wallLocation: Location, orientation: Orientation): Boolean {
        // Perpendicular connection check
        for (direction in Direction.entries) {
            val oneStepDelta = direction.getDelta(1)
            val oneStepLocation = Location(wallLocation.x + oneStepDelta.first, wallLocation.y + oneStepDelta.second)
            if (!isLegalWallLocation(oneStepLocation)) {
                continue
            }

            val wallIndex = getWallIndex(oneStepLocation)
            val isWallPlaced = placedWalls.get(wallIndex)
            if (!isWallPlaced) {
                continue
            }

            val wallOrientation = if (placedWalls.get(wallIndex + 1)) Orientation.VERTICAL else Orientation.HORIZONTAL
            if (wallOrientation == orientation.getFlipped()) {
                return true
            }
        }

        // Parallel connection check
        val directions = if (orientation == Orientation.HORIZONTAL) {
            arrayOf(Direction.E, Direction.W)
        } else {
            arrayOf(Direction.N, Direction.S)
        }

        for (direction in directions) {
            val twoStepDelta = direction.getDelta(2)
            val twoStepLocation = Location(wallLocation.x + twoStepDelta.first, wallLocation.y + twoStepDelta.second)
            if (!isLegalWallLocation(twoStepLocation)) {
                continue
            }

            val wallIndex = getWallIndex(twoStepLocation)
            val isWallPlaced = placedWalls.get(wallIndex)
            if (!isWallPlaced) {
                continue
            }

            val wallOrientation = if (placedWalls.get(wallIndex + 1)) Orientation.VERTICAL else Orientation.HORIZONTAL
            if (wallOrientation == orientation) {
                return true
            }
        }

        return false
    }

    private fun isLegalWallLocation(wallLocation: Location): Boolean {
        return (wallLocation.x in 0 until size - 1) && (wallLocation.y in 0 until size - 1)
    }

    private fun isLegalPawnLocation(pawnLocation: Location): Boolean {
        return (pawnLocation.x in 0 until size) && (pawnLocation.y in 0 until size)
    }

    /**
     * Basically, separated into 2 check case
     * 1. Horizontal Movement 2. Vertical Movement
     * For Horizontal movement => check is there any vertical wall placed
     * For Vertical movement => check is there any horizontal wall placed
     */
    private fun isBlocked(pawnLocation1: Location, pawnLocation2: Location): Boolean {
        val isHorizontalMovement = pawnLocation1.y == pawnLocation2.y
        if (isHorizontalMovement) {
            val wallLocations = List(2) { Location(x = min(pawnLocation1.x, pawnLocation2.x), pawnLocation1.y - it) }
            for (wallLocation in wallLocations) {
                if (!isLegalWallLocation(wallLocation)) {
                    continue
                }

                val wallIndex = getWallIndex(wallLocation)
                val isWallPlaced = placedWalls.get(wallIndex)
                if (!isWallPlaced) {
                    continue
                }

                val isVerticalWall = placedWalls.get(wallIndex + 1)
                if (isVerticalWall) {
                    return true
                }
            }
        } else {
            val wallLocations = List(2) { Location(x = pawnLocation1.x - it, min(pawnLocation1.y, pawnLocation2.y)) }
            for (wallLocation in wallLocations) {
                if (!isLegalWallLocation(wallLocation)) {
                    continue
                }

                val wallIndex = getWallIndex(wallLocation)
                val isWallPlaced = placedWalls.get(wallIndex)
                if (!isWallPlaced) {
                    continue
                }

                val isHorizontalWall = !placedWalls.get(wallIndex + 1)
                if (isHorizontalWall) {
                    return true
                }
            }
        }

        return false
    }

    private fun findLegalNextPawnLocations(pointOfSearch: Location, opponentLocation: Location? = null): List<Location> {
        val legalNewLocations = mutableListOf<Location>()

        val directions = arrayOf(Direction.N, Direction.S, Direction.W, Direction.E)
        for (direction in directions) {
            val oneStepDelta = direction.getDelta(1)
            val oneStepLocation = Location(pointOfSearch.x + oneStepDelta.first, pointOfSearch.y + oneStepDelta.second)
            if (!isLegalPawnLocation(oneStepLocation)) {
                continue
            }

            if (isBlocked(pointOfSearch, oneStepLocation)) {
                continue
            }

            val isMeetOpponent = oneStepLocation == opponentLocation
            if (!isMeetOpponent) {
                legalNewLocations += oneStepLocation
                continue
            }

            // if meet opponent case
            val twoStepDelta = direction.getDelta(2)
            val twoStepLocation = Location(pointOfSearch.x + twoStepDelta.first, pointOfSearch.y + twoStepDelta.second)
            val isJumpAvailable = !isBlocked(oneStepLocation, twoStepLocation) && isLegalPawnLocation(twoStepLocation)

            if (isJumpAvailable) {
                val threeStepDelta = direction.getDelta(3)
                val threeStepLocation = Location(pointOfSearch.x + threeStepDelta.first, pointOfSearch.y + threeStepDelta.second)
                legalNewLocations += threeStepLocation
                continue
            }

            // if jump is not available case
            val nearByDirection = direction.getNearBy()
            for (direction in nearByDirection) {
                val oneDiagonalStepDelta = direction.getDelta(1)
                val oneDiagonalStepLocation = Location(pointOfSearch.x + oneDiagonalStepDelta.first, pointOfSearch.y + oneDiagonalStepDelta.second)
                if (!isLegalPawnLocation(oneDiagonalStepLocation)) {
                    continue
                }

                if (isBlocked(oneStepLocation, oneDiagonalStepLocation)) {
                    continue
                }

                legalNewLocations += oneDiagonalStepLocation
            }
        }

        return legalNewLocations
    }

    override fun getLegalPawnMovements(forPlayer: Boolean): List<GameAction.PawnMovement> {
        val actionMaker = if (forPlayer) player() else opponent()
        val actionMakerOpponent = if (forPlayer) opponent() else player()
        val legalNewLocations = findLegalNextPawnLocations(pointOfSearch = actionMaker.pawnLocation, opponentLocation = actionMakerOpponent.pawnLocation)

        return legalNewLocations.map {
            GameAction.PawnMovement(
                oldLocation = actionMaker.pawnLocation,
                newLocation = it
            )
        }
    }

    override fun getLegalWallPlacements(): List<GameAction.WallPlacement> {
        val actionList = mutableListOf<GameAction.WallPlacement>()
        for (row in 0 until size - 1) {
            for (col in 0 until size - 1) {
                val horizontalWallPlacement = GameAction.WallPlacement(Orientation.HORIZONTAL, Location(col, row))
                if (isLegalWallPlacement(horizontalWallPlacement)) {
                    actionList.add(horizontalWallPlacement)
                }

                val verticalWallPlacement = GameAction.WallPlacement(Orientation.VERTICAL, Location(col, row))
                if (isLegalWallPlacement(verticalWallPlacement)) {
                    actionList.add(verticalWallPlacement)
                }
            }
        }

        return actionList
    }

    override fun getRandomLegalWallPlacement(): GameAction.WallPlacement {
        val wallIndices = (0 until (size - 1) * (size - 1))
            .filter { !placedWalls[it * 3] }
            .toIntArray()

        var wallPlacement: GameAction.WallPlacement
        do {
            val wallIndex = wallIndices.random()
            wallPlacement = GameAction.WallPlacement(
                orientation = Orientation.entries.random(),
                location = Location(wallIndex % (size - 1), wallIndex / (size - 1))
            )
        } while (!isLegalWallPlacement(wallPlacement))

        return wallPlacement
    }

    private fun getAllWallPlacement(): List<GameAction.WallPlacement> {
        val wallIndices = (0 until (size - 1) * (size - 1))
            .filter { placedWalls[it * 3] }
            .toIntArray()

        return wallIndices.map {
            GameAction.WallPlacement(
                orientation = if (placedWalls.get(it * 3 + 1)) Orientation.VERTICAL else Orientation.HORIZONTAL,
                location = Location(it % (size - 1), it / (size - 1))
            )
        }
    }

    override fun isPathToGoalExist(forPlayer: Boolean): Boolean {
        val targetPlayer = if (forPlayer) player() else opponent()
        val targetPlayerOpponent = if (forPlayer) opponent() else player()

        // DFS
        return SearchUtil.heuristicDFSPathToGoal(
            pointOfSearch = targetPlayer.pawnLocation,
            getNextMoves = { current -> findLegalNextPawnLocations(current, targetPlayerOpponent.pawnLocation) },
            isReachGoal = { current -> current.y == targetPlayer.goalY },
            heuristic = { current -> abs(current.y - targetPlayer.goalY) }
        )

        // BFS
//        return !getShortestPathToGoal(forPlayer).isNullOrEmpty()
    }

    override fun getShortestPathToGoal(forPlayer: Boolean): List<Location>? {
        val targetPlayer = if (forPlayer) player() else opponent()
        val targetPlayerOpponent = if (forPlayer) opponent() else player()

        // Apply A* for obstacle-rich maze-like environments where heuristic guidance improves efficiency,
        // otherwise use BFS for uncluttered paths where blind search performs better with linear complexity
        val shortestDistance = if (players.sumOf { it.remainingWalls } < 5) {
            // A*
            SearchUtil.aStarShortestDistance(
                pointOfSearch = targetPlayer.pawnLocation,
                getNextMoves = { current -> findLegalNextPawnLocations(current, targetPlayerOpponent.pawnLocation) },
                isReachGoal = { current -> current.y == targetPlayer.goalY },
                heuristic = { current -> abs(current.y - targetPlayer.goalY) }
            )
        } else {
            // BFS
            SearchUtil.bfsShortestDistance(
                pointOfSearch = targetPlayer.pawnLocation,
                getNextMoves = { current -> findLegalNextPawnLocations(current, targetPlayerOpponent.pawnLocation) },
                isReachGoal = { current -> current.y == targetPlayer.goalY }
            )
        }

        return shortestDistance
    }

    override fun deepCopy(): QuoridorGameState {
        val newGameState = QuoridorGameState(BoardSize.fromInt(size))
        newGameState.numberOfTurn = this@QuoridorGameState.numberOfTurn
        newGameState.placedWalls.or(this@QuoridorGameState.placedWalls)
        newGameState.players.forEachIndexed { idx, player ->
            player.pawnLocation = this@QuoridorGameState.players[idx].pawnLocation
            player.remainingWalls = this@QuoridorGameState.players[idx].remainingWalls
        }

        return newGameState
    }

    override val stringRepresentation: String
        get() {
            val printableGame = StringBuilder()
            printableGame.append("\n")
            printableGame.append("Turn: #$numberOfTurn \n")

            val size = size
            val padding = (size / 10) + 1
            val allWallPlacementNotions = getAllWallPlacement().map { "${it.location.toNotation()}${it.orientation.notation}" }
            val charList = A_TO_Z.substring(0, size).map { it }
            printableGame.append(" ".repeat(padding + 1))// padding
            val horizontalLabel = charList.joinToString(" ") { " $it " }
            printableGame.append(horizontalLabel)
            printableGame.append("\n")
            printableGame.append(" ".repeat(padding + 1))// padding
            printableGame.append("-".repeat(horizontalLabel.length))
            printableGame.append("\n")
            for (i in size downTo 1) {
                printableGame.append("$i".padStart(padding, ' '))
                printableGame.append("|")
                // grid line
                for (c in charList) {
                    val cell = if (players.first().pawnLocation.toNotation() == "${c}${i}") {
                        "@"
                    } else if (players.last().pawnLocation.toNotation() == "${c}${i}") {
                        "O"
                    } else {
                        " "
                    }
                    printableGame.append(" $cell ")
                    if (c < charList.last()) {
                        val vWall = if ("${c}${i - 1}v" in allWallPlacementNotions || "${c}${i}v" in allWallPlacementNotions) {
                            "|"
                        } else {
                            " "
                        }
                        printableGame.append(vWall)
                    }
                }
                printableGame.append("|")
                // border line
                printableGame.append("\n")
                if (i > 1) {
                    printableGame.append(" ".repeat(padding))
                    printableGame.append("|")
                    for (c in charList) {
                        val hWall = if ("${c}${i - 1}h" in allWallPlacementNotions || "${c - 1}${i - 1}h" in allWallPlacementNotions) {
                            "-"
                        } else {
                            " "
                        }
                        printableGame.append(hWall.repeat(3))

                        if (c < charList.last()) {
                            printableGame.append("+")
                        }
                    }
                    printableGame.append("|")
                    printableGame.append("\n")
                }
            }
            printableGame.append(" ".repeat(padding + 1))// padding
            printableGame.append("-".repeat(horizontalLabel.length))
            printableGame.append("\n")

            players.forEachIndexed { idx, player ->
                printableGame.append("P${idx + 1}-${if (idx == 0) "@" else "O"}: ${player.remainingWalls} wall(s) ")
            }

            return printableGame.toString()
        }

    override fun toString(): String {
        return stringRepresentation
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoridorGameState

        if (size != other.size) return false
        if (numberOfTurn != other.numberOfTurn) return false
        if (placedWalls != other.placedWalls) return false
        if (players != other.players) return false
        if (stringRepresentation != other.stringRepresentation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + numberOfTurn
        result = 31 * result + placedWalls.hashCode()
        result = 31 * result + players.hashCode()
        result = 31 * result + stringRepresentation.hashCode()
        return result
    }
}

