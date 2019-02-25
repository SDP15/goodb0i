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
                val id = point.substringBefore(delim)
                val type = point.substringAfter(delim)
                if (id.isEmpty()) return null
                when (type) {
                    "pass" -> points.add(RoutePoint.Pass(id))
                    "turnr" -> points.add(RoutePoint.TurnRight(id))
                    "turnl" -> points.add(RoutePoint.TurnLeft(id))
                    "stop" -> points.add(RoutePoint.Stop(id))
                    else -> {
                        if (type.length == 36) { //UUID string is always 36 characters
                            points.add(RoutePoint.EntryCollectionPoint(id, type))
                        } else {
                            return null
                        }
                    }
                }
            }
            return Route(points)
        }

        fun emptyRoute() = Route(emptyList())

    }

    sealed class RoutePoint(val id: String) {

        // Constant start point
        object Start : RoutePoint("start")

        // A point to be passed through (only used to ensure that we are still on track)
        class Pass(id: String) : RoutePoint(id)

        class TurnRight(id: String) : RoutePoint(id)

        class TurnLeft(id: String) : RoutePoint(id)

        // A shelf at which we should stop to collect a product
        class EntryCollectionPoint(id: String, val productId: String) : RoutePoint(id)

        // A point at which to stop
        class Stop(id: String) : RoutePoint(id)

        // Constant end point
        object End : RoutePoint("end")

    }

}