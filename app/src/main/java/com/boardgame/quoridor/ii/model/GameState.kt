package com.boardgame.quoridor.ii.model

import com.boardgame.quoridor.ii.extension.getDelta
import com.boardgame.quoridor.ii.extension.getFlipped
import com.boardgame.quoridor.ii.extension.getNearBy
import com.google.gson.Gson
import java.util.BitSet
import kotlin.math.min

data class GameState(
    val size: Int
) : GameStateProperty<GameState> {
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
            goalY = 0,
            pawnLocation = Location(size / 2, size - 1),
            remainingWalls = 10
        ),
        Player( // P2
            goalY = size - 1,
            pawnLocation = Location(size / 2, 0),
            remainingWalls = 10
        )
    )

    override fun player(): Player = players[(numberOfTurn + 1) % 2]

    override fun opponent(): Player = players[numberOfTurn % 2]

    private fun movePawn(location: Location, forPlayer: Boolean = true) {
        val actionMaker = if (forPlayer) player() else opponent()
        actionMaker.pawnLocation = location
    }

    private fun getWallIndex(location: Location): Int {
        return (location.x * (size - 1) + location.y) * 3
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
        if (actionMaker.goalY != 0) {
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

    override fun takeGameAction(action: GameAction) {
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
            return false
        }

        val wallIndex = getWallIndex(action.location)
        val isWallPlaced = placedWalls.get(wallIndex)
        if (isWallPlaced) {
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
                val isHorizontalWall = !placedWalls.get(wallIndex + 1)
                if (isHorizontalWall) {
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
                    return false
                }
            }
        }

        // check dead block
        if (isConnectingWall(action.location, action.orientation)) {
            val temporaryGameState = this.clone()
            temporaryGameState.takeGameAction(action)
            if (temporaryGameState.getShortestPathToGoal(true).isNullOrEmpty()) {
                return false
            }

            if (temporaryGameState.getShortestPathToGoal(false).isNullOrEmpty()) {
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

    override fun getLegalPawnMovements(forPlayer: Boolean): List<GameAction.PawnMovement> {
        var legalNewLocations = arrayOf<Location>()

        val actionMaker = if (forPlayer) player() else opponent()
        val actionMakerOpponent = if (forPlayer) opponent() else player()

        val pointOfCheck = actionMaker.pawnLocation
        val directions = arrayOf(Direction.N, Direction.S, Direction.W, Direction.E)
        for (direction in directions) {
            val oneStepDelta = direction.getDelta(1)
            val oneStepLocation = Location(pointOfCheck.x + oneStepDelta.first, pointOfCheck.y + oneStepDelta.second)
            if (!isLegalPawnLocation(oneStepLocation)) {
                continue
            }

            if (isBlocked(pointOfCheck, oneStepLocation)) {
                continue
            }

            val isMeetOpponent = oneStepLocation == actionMakerOpponent.pawnLocation
            if (!isMeetOpponent) {
                legalNewLocations += oneStepLocation
                continue
            }

            // if meet opponent case
            val twoStepDelta = direction.getDelta(2)
            val twoStepLocation = Location(pointOfCheck.x + twoStepDelta.first, pointOfCheck.y + twoStepDelta.second)
            val isJumpAvailable = !isBlocked(oneStepLocation, twoStepLocation)

            if (isJumpAvailable) {
                val threeStepDelta = direction.getDelta(3)
                val threeStepLocation = Location(pointOfCheck.x + threeStepDelta.first, pointOfCheck.y + threeStepDelta.second)
                legalNewLocations += threeStepLocation
                continue
            }

            // if jump is not available case
            val nearByDirection = direction.getNearBy()
            for (direction in nearByDirection) {
                val oneDiagonalStepDelta = direction.getDelta(1)
                val oneDiagonalStepLocation = Location(pointOfCheck.x + oneDiagonalStepDelta.first, pointOfCheck.y + oneDiagonalStepDelta.second)
                if (!isLegalPawnLocation(oneDiagonalStepLocation)) {
                    continue
                }

                if (isBlocked(oneStepLocation, oneDiagonalStepLocation)) {
                    continue
                }

                legalNewLocations += oneDiagonalStepLocation
            }
        }

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

    override fun getShortestPathToGoal(forPlayer: Boolean): List<Location>? {
        TODO("Not yet implemented")
    }

    override fun isTerminated(): Boolean {
        return players.any { it.pawnLocation.y == it.goalY }
    }

    override fun clone(): GameState {
        val jsonStr = Gson().toJson(this)
        return Gson().fromJson(jsonStr, GameState::class.java)
    }

    override val printable: String
        get() {
            return ""
        }

}

