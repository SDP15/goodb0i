package service.routing

object RouteFinder {


    fun <ID> solver(graph: Graph<ID>, start: Graph.Node<ID>, end: Graph.Node<ID>, waypoints: List<Graph.Node<ID>>): List<Graph.Node<ID>> {
        var current = start
        val remaining = waypoints.toMutableList()
        val path = mutableListOf<Graph.Node<ID>>()
        while (remaining.isNotEmpty()) {
            val result = dijkstras(current, graph)
            val next = remaining.minBy { result.distances[it]!! }!!
            println("From $current, next best is $next")
            remaining.remove(next)
            var temp = next
            val subPath = mutableListOf<Graph.Node<ID>>()
            while (temp != current) {
                subPath.add(temp)
                temp = result.previous[temp]!!
            }
            subPath.add(current)
            path.addAll(subPath.asReversed())
            println("Path is now $path")
            current = next
            if (current == end) break
            if (remaining.isEmpty()) remaining.add(end)

        }
        return path
    }

    private fun <ID> path(source: Graph.Node<ID>, sink: Graph.Node<ID>, graph: Graph<ID>): List<Graph.Node<ID>> {
        println("Finding path from $source to $sink")
        val previous = dijkstras(source, graph).previous
        val path = mutableListOf<Graph.Node<ID>>()
        var current = sink
        while (current != source) {
            path.add(0, current)
            current = previous[current]!!
        }
        path.add(0, source)
        println("Found path $path from $source to $sink ")
        return path
    }

    private fun <ID> dijkstras(source: Graph.Node<ID>, graph: Graph<ID>): DijkstraResult<ID> {
        println("Running dijkstras from $source")
        val distances = HashMap<Graph.Node<ID>, Double>()
        val previous = HashMap<Graph.Node<ID>, Graph.Node<ID>>()
        val Q = HashSet<Graph.Node<ID>>()
        graph.forEach { vertex: Graph.Vertex<ID> ->
            distances[vertex.node] = 100000.0
            Q.add(vertex.node)
        }
        distances[source] = 0.0
        while (Q.isNotEmpty()) {
            val min = Q.minBy { distances[it]!! }!!
            //println("Min node $min")
            Q.remove(min)
            graph[min]?.forEach { edge ->
                //println("Checking edge $edge")
                val alt = distances[min]!! + edge.cost
                if (alt < distances[edge.to]!!) {
                    //println("Setting previous for ${edge.to} to $min from distance ${distances[edge.to]} to $alt ")
                    distances[edge.to] = alt
                    previous[edge.to] = min
                }
            }
        }
        println("Dijkstras done $previous")
        return DijkstraResult(distances, previous)
    }

    private class DijkstraResult<ID>(val distances: HashMap<Graph.Node<ID>, Double>,
                                     val previous: HashMap<Graph.Node<ID>, Graph.Node<ID>>)

}