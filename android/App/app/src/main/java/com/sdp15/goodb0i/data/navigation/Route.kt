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
            var index = 0
            data.split(separator).forEach { point ->
                val type = point.substringBefore(delim)
                val body = point.substringAfter(delim)
                Timber.i("Type $type id $body")
                when (type) {
                    "start" -> points.add(RoutePoint.IndexPoint.Start)
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

        fun emptyRoute() = Route(emptyList())

    }

    sealed class RoutePoint {

        sealed class IndexPoint(val index: Int) : RoutePoint() {
            // Constant start at
            object Start : IndexPoint(0)

            sealed class IdentifiedPoint(index: Int, val id: String) : IndexPoint(index) {

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