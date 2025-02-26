package com.boardgame.quoridor.ii.game.state

import com.boardgame.quoridor.ii.A_TO_Z
import com.boardgame.quoridor.ii.extension.getDelta
import com.boardgame.quoridor.ii.extension.getNearBy
import com.boardgame.quoridor.ii.extension.toNotation
import com.boardgame.quoridor.ii.model.BoardSize
import com.boardgame.quoridor.ii.model.Direction
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Orientation
import com.boardgame.quoridor.ii.model.Player
import com.boardgame.quoridor.ii.util.SearchUtil
import kotlin.math.abs

class QuoridorGameState(boardSize: BoardSize) : BasicQuoridorGameState() {
    companion object {
        fun createFrom(
            boardSize: BoardSize,
            p1PawnLocation: Location,
            p2PawnLocation: Location,
            p1WallPlacements: List<GameAction.WallPlacement>,
            p2WallPlacements: List<GameAction.WallPlacement>,
            numberOfTurn: Int
        ): QuoridorGameState {
            return QuoridorGameState(boardSize).apply {
                getFirstPlayer().pawnLocation = p1PawnLocation

                getSecondPlayer().pawnLocation = p2PawnLocation
                p1WallPlacements.forEach {
                    wallMap.placeWall(it, getFirstPlayer())
                    getFirstPlayer().remainingWalls--
                }

                p2WallPlacements.forEach {
                    wallMap.placeWall(it, getSecondPlayer())
                    getSecondPlayer().remainingWalls--
                }

                this.numberOfTurn = numberOfTurn
            }
        }
    }

    val size = boardSize.value

    private var wallMap = WallMap(size - 1)

    private val players = listOf(
        Player( // P1
            goalY = size - 1,
            pawnLocation = Location(size / 2, 0),
            remainingWalls = maxNumOfWallPerPlayer
        ),
        Player( // P2
            goalY = 0,
            pawnLocation = Location(size / 2, size - 1),
            remainingWalls = maxNumOfWallPerPlayer
        )
    )

    fun getFirstPlayer(): Player = players.first()
    fun getSecondPlayer(): Player = players.last()

    override fun player(): Player = players[(numberOfTurn + 1) % 2]

    override fun opponent(): Player = players[numberOfTurn % 2]

    override fun winner(): Player? {
        return players.firstOrNull { it.hasReachedGoal() }
    }

    private fun movePawn(location: Location, forPlayer: Boolean = true) {
        val actionMaker = if (forPlayer) player() else opponent()
        actionMaker.pawnLocation = location
    }

    private fun placeWall(action: GameAction.WallPlacement, forPlayer: Boolean = true) {
        val actionMaker = if (forPlayer) player() else opponent()
        wallMap.placeWall(action, actionMaker)
        actionMaker.remainingWalls--
    }

    private fun removeWall(action: GameAction.WallPlacement, forPlayer: Boolean = true) {
        val actionMaker = if (forPlayer) player() else opponent()
        wallMap.removeWall(action)
        actionMaker.remainingWalls++
    }

    override fun executeGameAction(action: GameAction) {
        when (action) {
            is GameAction.PawnMovement -> movePawn(action.newPawnLocation)
            is GameAction.WallPlacement -> placeWall(action)
        }
        super.executeGameAction(action)
    }

    override fun reverseGameAction(action: GameAction) {
        when (action) {
            is GameAction.PawnMovement -> {
                movePawn(action.oldPawnLocation, false)
            }

            is GameAction.WallPlacement -> {
                removeWall(action, false)
            }
        }
        super.reverseGameAction(action)
    }

    override fun isLegalPawnMovement(action: GameAction.PawnMovement, forPlayer: Boolean): Boolean {
        return getLegalPawnMovements(forPlayer).contains(action)
    }

    override fun isLegalWallPlacement(action: GameAction.WallPlacement): Boolean {
        if (!wallMap.isLegalWallLocation(action.wallLocation)) {
//            Log.e("isLegalWallPlacement", "illegal wall location")
            return false
        }

        if (wallMap.isOverlapWallPlacement(action)) {
//            Log.e("isLegalWallPlacement", "wall overlapping")
            return false
        }

        // check dead block
        if (wallMap.isJointWallPlacement(action.wallLocation, action.orientation)) {
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

    fun isEffectiveWallPlacement(wallPlacement: GameAction.WallPlacement): Boolean {
        return wallPlacement.let {
            wallMap.isJointWallPlacement(it.wallLocation, it.orientation) // wall joining an other wall
                    || (abs(it.wallLocation.x - player().pawnLocation.x) <= 1 && abs(it.wallLocation.y - player().pawnLocation.y) <= 1) // around player's pawn
                    || (abs(it.wallLocation.x - opponent().pawnLocation.x) <= 1 && abs(it.wallLocation.y - opponent().pawnLocation.y) <= 1) // around opponent's pawn
                    || ((it.wallLocation.x == 0 || it.wallLocation.x == size - 2) && it.orientation == Orientation.HORIZONTAL) // leftmost and rightmost horizontal
        }
    }

    fun getEffectiveWallPlacements(): List<GameAction.WallPlacement> {
        return getLegalWallPlacements().filter { isEffectiveWallPlacement(it) }
    }

    fun getRandomEffectiveWallPlacement(): GameAction.WallPlacement {
        val vacantWallLocation = wallMap.getVacantWallLocations()

        var wallPlacement: GameAction.WallPlacement
        do {
            val wallLocation = vacantWallLocation.random()
            wallPlacement = GameAction.WallPlacement(
                orientation = Orientation.entries.random(),
                wallLocation = wallLocation
            )
        } while (!isLegalWallPlacement(wallPlacement) || !isEffectiveWallPlacement(wallPlacement))

        return wallPlacement
    }

    private fun isLegalPawnLocation(pawnLocation: Location): Boolean {
        return (pawnLocation.x in 0 until size) && (pawnLocation.y in 0 until size)
    }

    private fun findNextLegalPawnLocations(pointOfSearch: Location, opponentLocation: Location? = null): List<Location> {
        val legalNewLocations = mutableListOf<Location>()

        val directions = arrayOf(Direction.N, Direction.S, Direction.W, Direction.E)
        for (direction in directions) {
            val oneStepDelta = direction.getDelta(1)
            val oneStepLocation = Location(pointOfSearch.x + oneStepDelta.first, pointOfSearch.y + oneStepDelta.second)
            if (!isLegalPawnLocation(oneStepLocation)) {
                continue
            }

            if (wallMap.isBlocked(pointOfSearch, oneStepLocation)) {
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
            val isJumpAvailable = !wallMap.isBlocked(oneStepLocation, twoStepLocation) && isLegalPawnLocation(twoStepLocation)

            if (isJumpAvailable) {
                legalNewLocations += twoStepLocation
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

                if (wallMap.isBlocked(oneStepLocation, oneDiagonalStepLocation)) {
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
        val legalNewLocations = findNextLegalPawnLocations(pointOfSearch = actionMaker.pawnLocation, opponentLocation = actionMakerOpponent.pawnLocation)

        return legalNewLocations.map { GameAction.PawnMovement(newPawnLocation = it, oldPawnLocation = actionMaker.pawnLocation) }
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
        val vacantWallLocation = wallMap.getVacantWallLocations()

        var wallPlacement: GameAction.WallPlacement
        do {
            val wallLocation = vacantWallLocation.random()
            wallPlacement = GameAction.WallPlacement(
                orientation = Orientation.entries.random(),
                wallLocation = wallLocation
            )
        } while (!isLegalWallPlacement(wallPlacement))

        return wallPlacement
    }

    override fun isPathToGoalExist(forPlayer: Boolean): Boolean {
        val targetPlayer = if (forPlayer) player() else opponent()

        // DFS
        return SearchUtil.heuristicDFSPathToGoal(
            pointOfSearch = targetPlayer.pawnLocation,
            getNextMoves = { current -> findNextLegalPawnLocations(current) },
            isReachGoal = { current -> current.y == targetPlayer.goalY },
            heuristic = { current -> abs(current.y - targetPlayer.goalY) }
        )

        // BFS
//        return !getShortestPathToGoal(forPlayer).isNullOrEmpty()
    }

    override fun getShortestPathToGoal(forPlayer: Boolean, considerOpponent: Boolean): List<Location>? {
        val targetPlayer = if (forPlayer) player() else opponent()
        val targetPlayerOpponent = if (!considerOpponent) null else if (forPlayer) opponent() else player()

        // Apply A* for obstacle-rich maze-like environments where heuristic guidance improves efficiency,
        // otherwise use BFS for uncluttered paths where blind search performs better with linear complexity
        val shortestDistance = if (players.sumOf { it.remainingWalls } < 5) {
            // A*
            SearchUtil.aStarShortestDistance(
                pointOfSearch = targetPlayer.pawnLocation,
                getNextMoves = { current -> findNextLegalPawnLocations(current, targetPlayerOpponent?.pawnLocation) },
                isReachGoal = { current -> current.y == targetPlayer.goalY },
                heuristic = { current -> abs(current.y - targetPlayer.goalY) }
            )
        } else {
            // BFS
            SearchUtil.bfsShortestDistance(
                pointOfSearch = targetPlayer.pawnLocation,
                getNextMoves = { current -> findNextLegalPawnLocations(current, targetPlayerOpponent?.pawnLocation) },
                isReachGoal = { current -> current.y == targetPlayer.goalY }
            )
        }

        return shortestDistance
    }

    fun getAllWallPlacements(): List<Pair<GameAction.WallPlacement, Int>> {
        return wallMap.getAllWallPlacements()
    }

    override fun deepCopy(): QuoridorGameState {
        val newGameState = QuoridorGameState(BoardSize.fromInt(size))
        newGameState.numberOfTurn = this@QuoridorGameState.numberOfTurn
        newGameState.wallMap = this@QuoridorGameState.wallMap.clone()
        newGameState.players.forEachIndexed { idx, player ->
            player.pawnLocation = this@QuoridorGameState.players[idx].pawnLocation
            player.remainingWalls = this@QuoridorGameState.players[idx].remainingWalls
        }

        return newGameState
    }

    override fun getStringRepresentation(): String {
        val printableGame = StringBuilder()
        printableGame.append("\n")
        printableGame.append("Turn: #$numberOfTurn - P${player().getPlayerIndex() + 1}\n")

        val size = size
        val padding = (size / 10) + 1
        val allWallPlacementNotions = wallMap.getAllWallPlacements().map { "${it.first.wallLocation.toNotation()}${it.first.orientation.notation}" }
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
                    " " // ▢
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
        return getStringRepresentation()
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + numberOfTurn
        result = 31 * result + wallMap.hashCode()
        result = 31 * result + players.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoridorGameState

        if (size != other.size) return false
        if (numberOfTurn != other.numberOfTurn) return false
        if (wallMap != other.wallMap) return false
        if (players != other.players) return false

        return true
    }
}

