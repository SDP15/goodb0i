package service.routing

object RouteFinder {


    fun <ID> convert(graph: Graph<ID>, start: Graph.Node<ID>, end: Graph.Node<ID>, waypoints: List<Graph.Node<ID>>): String {
        val route = solver(graph, start, end, waypoints)
        val builder = StringBuilder()
        val sep = ','
        val delim = '%'
        var previous = start
        route.forEach { node ->
            if (node == start) {
                builder.append("start")
            } else if (node == end) {
                builder.append("end")
            } else {
                // Add a turn if there's more than one way to get to the next node
                val edges = graph[previous]
                if (edges != null && edges.size > 1) {
                    val index = edges.indexOfFirst { edge -> edge.to == node }
                    println("Node $node Edges are $edges. Index is $index")
                    when (index) {
                        //0 -> builder.append("left")
                        0 -> builder.append("center")
                        1 -> builder.append("right")
                    }
                    builder.append(sep)
                }
                if (node in waypoints) {
                    builder.append("stop$delim${node.id}")
                } else {
                    builder.append("pass$delim${node.id}")
                }
            }

            builder.append(sep)
            previous = node
        }

        return builder.toString()
    }

    fun <ID> solver(graph: Graph<ID>, start: Graph.Node<ID>, end: Graph.Node<ID>, waypoints: List<Graph.Node<ID>>): List<Graph.Node<ID>> {
        var current = start
        val remaining = waypoints.toMutableList()
        val path = mutableListOf<Graph.Node<ID>>()
        while (remaining.isNotEmpty()) {
            val result = dijkstras(current, graph)
            val next = remaining.minBy { result.distances[it]!! }!!
            remaining.remove(next)
            var temp = result.previous[next]!! // We don't want to re-add the waypoint
            val subPath = mutableListOf<Graph.Node<ID>>()
            while (temp != current) {
                println("Adding node $temp")
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