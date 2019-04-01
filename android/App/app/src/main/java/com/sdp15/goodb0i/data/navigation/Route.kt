package com.sdp15.goodb0i.data.navigation

import timber.log.Timber

/*
 * A route is a list of RoutePoints
 */
class Route private constructor(
    private val points: MutableList<RoutePoint>
) : List<Route.RoutePoint> by points {


    companion object {

        private const val delim = "%"
        private const val separator = ","

        fun fromString(data: String): Route? {
            val points = mutableListOf<RoutePoint>()
            var index = 0
            data.split(separator).forEach { point ->
                val type = point.substringBefore(delim)
                val body = point.substringAfter(delim)
                Timber.i("Type $type id $body")
                when (type) {
                    "start" -> points.add(RoutePoint.IndexPoint.IdentifiedPoint.Start(++index, body))
                    "end" -> points.add(RoutePoint.IndexPoint.IdentifiedPoint.End(++index, body))
                    "left" -> points.add(RoutePoint.TurnLeft)
                    "right" -> points.add(RoutePoint.TurnRight)
                    "forward" -> points.add(RoutePoint.TurnForward)
                    "stop" -> {
                        val id = body.substringBefore(delim) // First int value
                        val indices = body.substringAfter(delim).split(delim).map { it.toInt() }.chunked(2)
                        points.add(RoutePoint.IndexPoint.IdentifiedPoint.Stop(++index, id, indices.map { it.first() }, indices.map { it[1] }))
                    }
                    "pass" -> points.add(RoutePoint.IndexPoint.IdentifiedPoint.Pass(++index, body))
                }
            }
            return Route(points)
        }

        fun emptyRoute() = Route(mutableListOf())

    }

    fun replaceSubRoute(route: Route) {
        val first = route.first() as RoutePoint.IndexPoint.IdentifiedPoint
        val startIndex = indexOfFirst { point -> point is RoutePoint.IndexPoint.IdentifiedPoint && first.id  == point.id }
        if (startIndex == -1) throw IllegalArgumentException("Subroute start point ${route.first()} not in route")
        val end = route.last() as RoutePoint.IndexPoint.IdentifiedPoint
        val endIndex = indexOfFirst { point -> point is RoutePoint.IndexPoint.IdentifiedPoint && end.id == point.id }
        if (endIndex == -1) throw IllegalArgumentException("Subroute end point ${route.last()} not in route")
        if (startIndex >= endIndex) throw IllegalArgumentException("Subroute start index $startIndex must be less than subroute end index $endIndex")
        val newRoute = points.subList(0, startIndex) + route.subList(0, route.size) + points.subList(endIndex, points.size)
        Timber.i("Inserted sub route $route ")
        points.clear()
        points.addAll(newRoute)
    }

    sealed class RoutePoint {

        sealed class IndexPoint(val index: Int) : RoutePoint() {
            // Constant start

            sealed class IdentifiedPoint(index: Int, val id: String) : IndexPoint(index) {

                class Start(index: Int, id: String) : IdentifiedPoint(index, id)

                // A point to be passed through (only used to ensure that we are still on track)
                class Pass(index: Int, id: String) : IdentifiedPoint(index, id)

                // A point at which to stop
                class Stop(index: Int, id: String, val productIndices: List<Int>, val shelfPositions: List<Int>) : IdentifiedPoint(index, id)

                class End(index: Int, id: String) : IdentifiedPoint(index, id)
            }


        }

        object TurnLeft : RoutePoint()

        object TurnRight : RoutePoint()

        object TurnForward : RoutePoint()

    }

}