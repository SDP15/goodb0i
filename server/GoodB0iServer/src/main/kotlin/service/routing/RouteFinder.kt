package service.routing


interface RouteFinder {

    fun plan(waypoints: Collection<Graph.Node<Int>>): RouteFinder.RoutingResult

    fun plan(start: Graph.Node<Int>, end: Graph.Node<Int>, waypoints: Collection<Graph.Node<Int>>): RoutingResult.Route

    sealed class RoutingResult {

        data class Route(val nodes: List<Graph.Vertex<Int>>) : RoutingResult(), List<Graph.Vertex<Int>> by nodes

        sealed class RoutingError : RoutingResult() {

            data class PointNotInGraph(val point: Int) : RoutingError()

            data class NoPathBetweenPoints(val from: Int, val to: Int) : RoutingError()

        }

    }


}