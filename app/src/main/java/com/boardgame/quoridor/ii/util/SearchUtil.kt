package com.boardgame.quoridor.ii.util

import com.boardgame.quoridor.ii.model.Location
import java.util.LinkedList
import java.util.PriorityQueue
import java.util.Queue

object SearchUtil {

    fun bfsShortestDistance(
        pointOfSearch: Location,
        getNextMoves: (point: Location) -> List<Location>,
        isReachGoal: (point: Location) -> Boolean
    ): List<Location>? {
        val queue: Queue<Pair<Location, List<Location>>> = LinkedList()
        val visited: MutableList<Location> = mutableListOf()
        queue.add(pointOfSearch to emptyList())
        visited.add(pointOfSearch)

        while (queue.isNotEmpty()) {
            queue.poll()?.let { (current, route) ->
                if (isReachGoal(current)) {
                    return route + listOf(current)
                }

                val validMoves = getNextMoves(current)

                validMoves.forEach { move ->
                    val isVisited = visited.contains(move)
                    if (!isVisited) {
                        val newRoute = route + listOf(current)
                        queue.add(move to newRoute)
                        visited.add(move)
                    }
                }
            }
        }

        return null
    }

    private data class AStartNode(
        val location: Location,
        val route: List<Location>,
        val g: Int,
        val f: Int
    )

    fun aStarShortestDistance(
        pointOfSearch: Location,
        getNextMoves: (point: Location) -> List<Location>,
        isReachGoal: (point: Location) -> Boolean,
        heuristic: (point: Location) -> Int
    ): List<Location>? {
        val queue = PriorityQueue<AStartNode>(compareBy { it.f })
        val visited = mutableMapOf<Location, Int>()

        queue.add(AStartNode(pointOfSearch, emptyList(), 0, heuristic(pointOfSearch)))
        visited[pointOfSearch] = 0

        while (queue.isNotEmpty()) {
            queue.poll()?.let { (current, route, g, _) ->
                if (isReachGoal(current)) {
                    return route + current
                }

                val validMoves = getNextMoves(current)

                for (move in validMoves) {
                    val newG = g + 1
                    val newF = newG + heuristic(move)

                    if (!visited.containsKey(move) || newG < visited[move]!!) {
                        visited[move] = newG
                        val newRoute = route + current
                        queue.add(AStartNode(move, newRoute, newG, newF))
                    }
                }
            }
        }

        return null
    }
}