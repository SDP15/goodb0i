package com.sdp15.goodb0i.data.navigation

/*
 * A route is a list of RoutePoints
 */
class Route private constructor(
    points: List<RoutePoint>,
    private val routePoints: List<RoutePoint> = mutableListOf(RoutePoint.Start) + points + mutableListOf(RoutePoint.End)
) : List<Route.RoutePoint> by routePoints {


    companion object {

        private const val delim = "%"
        private const val separator = ","

        fun fromString(data: String): Route? {
            val points = mutableListOf<RoutePoint>()
            data.split(separator).forEach { point ->
                val type = point.substringBefore(delim)
                val id = point.substringAfter(delim)
                if (id.isEmpty()) return null
                when (type) {
                    "start" -> points.add(RoutePoint.Start)
                    "end" -> points.add(RoutePoint.End)
                    "left" -> points.add(RoutePoint.TurnLeft)
                    "right" -> points.add(RoutePoint.TurnRight)
                    "center" -> points.add(RoutePoint.TurnCenter)
                    "stop" -> points.add(RoutePoint.Stop(id))
                    "pass" -> points.add(RoutePoint.Pass(id))
                }
            }
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
        data class Stop(val id: String) : RoutePoint()

        // Constant end point
        object End : RoutePoint()

    }

}