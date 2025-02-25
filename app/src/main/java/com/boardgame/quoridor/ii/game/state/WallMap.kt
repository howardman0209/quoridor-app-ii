package com.boardgame.quoridor.ii.game.state

import com.boardgame.quoridor.ii.extension.getDelta
import com.boardgame.quoridor.ii.extension.getFlipped
import com.boardgame.quoridor.ii.model.Direction
import com.boardgame.quoridor.ii.model.GameAction
import com.boardgame.quoridor.ii.model.Location
import com.boardgame.quoridor.ii.model.Orientation
import com.boardgame.quoridor.ii.model.Player
import java.util.BitSet
import kotlin.math.min

/**
 * occupancy info (0: empty, 1: placed)
 * orientation info (0: horizontal, 1: vertical)
 * placer info (0: player1, 1: player2)
 */
class WallMap(val size: Int) {
    private val occupancyData = BitSet(size * size)
    private val orientationData = BitSet(size * size)
    private val placerData = BitSet(size * size)

    private fun getWallIndex(location: Location): Int {
        return (location.x + location.y * size)
    }

    fun placeWall(action: GameAction.WallPlacement, placer: Player) {
        val wallIndex = getWallIndex(action.wallLocation)

        // set occupancy
        occupancyData.set(wallIndex)

        // set orientation
        if (action.orientation == Orientation.VERTICAL) {
            orientationData.set(wallIndex)
        }

        // set placer
        if (placer.getPlayerIndex() == 1) {
            placerData.set(wallIndex)
        }
    }

    fun removeWall(action: GameAction.WallPlacement) {
        val wallIndex = getWallIndex(action.wallLocation)
        occupancyData.clear(wallIndex)
        orientationData.clear(wallIndex)
        placerData.clear(wallIndex)
    }

    fun getAllWallPlacements(): List<Pair<GameAction.WallPlacement, Int>> {
        val wallIndices = (0 until size * size)
            .filter { occupancyData[it] }
            .toIntArray()

        return wallIndices.map {
            Pair(
                GameAction.WallPlacement(
                    orientation = if (orientationData.get(it)) Orientation.VERTICAL else Orientation.HORIZONTAL,
                    wallLocation = Location(it % size, it / size)
                ),
                if (placerData.get(it)) 1 else 0
            )
        }
    }

    fun isLegalWallLocation(wallLocation: Location): Boolean {
        return (wallLocation.x in 0 until size) && (wallLocation.y in 0 until size)
    }

    fun isOverlapWallPlacement(action: GameAction.WallPlacement): Boolean {
        val pointOfCheck = action.wallLocation

        // check crossing
        val wallIndex = getWallIndex(action.wallLocation)
        val isWallPlaced = occupancyData.get(wallIndex)
        if (isWallPlaced) {
//            Log.e("isLegalWallPlacement", "wall crossing")
            return true
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
                val isWallPlaced = occupancyData.get(nearByWallIndex)
                if (!isWallPlaced) {
                    continue
                }

                // overlapping
                val isHorizontalWall = !orientationData.get(nearByWallIndex)
                if (isHorizontalWall) {
//                    Log.e("isLegalWallPlacement", "wall overlapping")
                    return true
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
                val isWallPlaced = occupancyData.get(nearByWallIndex)
                if (!isWallPlaced) {
                    continue
                }

                // overlapping
                val isVerticalWall = orientationData.get(nearByWallIndex)
                if (isVerticalWall) {
//                    Log.e("isLegalWallPlacement", "wall overlapping")
                    return true
                }
            }
        }

        return false
    }

    /**
     * Basically, separated into 2 check case
     * 1. Perpendicular connection 2. Parallel connection
     * For Perpendicular connection => check is there any wall perpendicular joint
     * For Parallel connection => check is there any wall parallel joint
     */
    fun isJointWallPlacement(wallLocation: Location, orientation: Orientation): Boolean {
        // Perpendicular connection check
        for (direction in Direction.entries) {
            val oneStepDelta = direction.getDelta(1)
            val oneStepLocation = Location(wallLocation.x + oneStepDelta.first, wallLocation.y + oneStepDelta.second)
            if (!isLegalWallLocation(oneStepLocation)) {
                continue
            }

            val wallIndex = getWallIndex(oneStepLocation)
            val isWallPlaced = occupancyData.get(wallIndex)
            if (!isWallPlaced) {
                continue
            }

            val wallOrientation = if (orientationData.get(wallIndex)) Orientation.VERTICAL else Orientation.HORIZONTAL
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
            val isWallPlaced = occupancyData.get(wallIndex)
            if (!isWallPlaced) {
                continue
            }

            val wallOrientation = if (orientationData.get(wallIndex)) Orientation.VERTICAL else Orientation.HORIZONTAL
            if (wallOrientation == orientation) {
                return true
            }
        }

        return false
    }

    /**
     * Basically, separated into 2 check case
     * 1. Horizontal Movement 2. Vertical Movement
     * For Horizontal movement => check is there any vertical wall placed
     * For Vertical movement => check is there any horizontal wall placed
     */
    fun isBlocked(pawnLocation1: Location, pawnLocation2: Location): Boolean {
        val isHorizontalMovement = pawnLocation1.y == pawnLocation2.y
        if (isHorizontalMovement) {
            val wallLocations = List(2) { Location(x = min(pawnLocation1.x, pawnLocation2.x), pawnLocation1.y - it) }
            for (wallLocation in wallLocations) {
                if (!isLegalWallLocation(wallLocation)) {
                    continue
                }

                val wallIndex = getWallIndex(wallLocation)
                val isWallPlaced = occupancyData.get(wallIndex)
                if (!isWallPlaced) {
                    continue
                }

                val isVerticalWall = orientationData.get(wallIndex)
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
                val isWallPlaced = occupancyData.get(wallIndex)
                if (!isWallPlaced) {
                    continue
                }

                val isHorizontalWall = !orientationData.get(wallIndex)
                if (isHorizontalWall) {
                    return true
                }
            }
        }

        return false
    }

    fun getVacantWallLocations(): List<Location> {
        val wallIndices = (0 until (size * size))
            .filter { !occupancyData[it] }
            .toIntArray()

        return wallIndices.map { Location(it % size, it / size) }
    }

    fun clone(): WallMap {
        val newWallMap = WallMap(size)
        newWallMap.occupancyData.or(this@WallMap.occupancyData)
        newWallMap.orientationData.or(this@WallMap.orientationData)
        newWallMap.placerData.or(this@WallMap.placerData)
        return newWallMap
    }


    override fun hashCode(): Int {
        var result = size
        result = 31 * result + occupancyData.hashCode()
        result = 31 * result + orientationData.hashCode()
        result = 31 * result + placerData.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WallMap

        if (size != other.size) return false
        if (occupancyData != other.occupancyData) return false
        if (orientationData != other.orientationData) return false
        if (placerData != other.placerData) return false

        return true
    }
}