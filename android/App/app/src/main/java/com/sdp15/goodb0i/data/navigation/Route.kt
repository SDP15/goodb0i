package com.sdp15.goodb0i.data.navigation

class Route private constructor(private val points: List<RoutePoint>) : Collection<Route.RoutePoint> by points {

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
                            points.add(RoutePoint.Product(id, type))
                        } else {
                            return null
                        }
                    }
                }
            }
            return Route(points)
        }

    }

    sealed class RoutePoint(val id: String) {

        class Pass(id: String) : RoutePoint(id)

        class TurnRight(id: String) : RoutePoint(id)

        class TurnLeft(id: String) : RoutePoint(id)

        class Product(id: String, productId: String) : RoutePoint(id)

        class Stop(id: String) : RoutePoint(id)

    }

}