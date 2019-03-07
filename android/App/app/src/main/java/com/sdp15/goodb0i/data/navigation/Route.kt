package com.sdp15.goodb0i.data.navigation

import timber.log.Timber

/*
 * A route is a list of RoutePoints
 */
class Route private constructor(
    points: List<RoutePoint>
) : List<Route.RoutePoint> by points {


    companion object {

        private const val delim = "%"
        private const val separator = ","

        fun fromString(data: String): Route? {
            val points = mutableListOf<RoutePoint>()
            data.split(separator).forEach { point ->
                val type = point.substringBefore(delim)
                val body = point.substringAfter(delim)
                Timber.i("Type $type id $body")
                when (type) {
                    "start" -> points.add(RoutePoint.Start)
                    "end" -> points.add(RoutePoint.End)
                    "left" -> points.add(RoutePoint.TurnLeft)
                    "right" -> points.add(RoutePoint.TurnRight)
                    "center" -> points.add(RoutePoint.TurnCenter)
                    "stop" -> {
                        val id = body.substringBefore(delim) // First int value
                        val indices = body.substringAfter(delim).split(delim).map { it.toInt() }
                        points.add(RoutePoint.Stop(id, indices))
                    }
                    "pass" -> points.add(RoutePoint.Pass(body))
                }
            }
            Timber.i("Created route with points $points")
            return Route(points)
        }

        fun emptyRoute() = Route(emptyList())

    }

    sealed class RoutePoint {

        // Constant start point
        object Start : RoutePoint()

        // A point to be passed through (only used to ensure that we are still on track)
        data class Pass(val id: String) : RoutePoint()

        object TurnLeft : RoutePoint()

        object TurnRight : RoutePoint()

        object TurnCenter : RoutePoint()

        // A point at which to stop
        data class Stop(val id: String, val productIndices: List<Int>) : RoutePoint()

        // Constant end point
        object End : RoutePoint()

    }

}