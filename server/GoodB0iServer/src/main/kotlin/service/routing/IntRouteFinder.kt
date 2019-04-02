package service.routing

import kotlin.system.measureNanoTime

class IntRouteFinder(private val graph: Graph<Int>): RouteFinder {
    
    private val cache = HashMap<Graph.Node<Int>, DijkstraResult>()

    init {
        generateAllPaths()
    }

    private fun generateAllPaths() {
        val dur = measureNanoTime {
            graph.forEach { vertex ->
                cache[vertex.node] = dijkstras(vertex.node, graph)
            }
        } / 1E9
        println("Precomputation complete in $dur")

    }


    override fun plan(waypoints: Collection<Graph.Node<Int>>): RouteFinder.RoutingResult {
        return plan(graph.start!!, graph.end!!, waypoints)
    }

    override fun plan(start: Graph.Node<Int>, end: Graph.Node<Int>, waypoints: Collection<Graph.Node<Int>>): RouteFinder.RoutingResult.Route {
        var current = start
        val remaining = waypoints.toMutableList()
        if (remaining.isEmpty()) remaining.add(end)
        val path = mutableListOf<Graph.Node<Int>>()
        println("Edges are ${graph.flatMap { it.edges }}")
        println("Calculating route through $waypoints from $start to $end")
        while (remaining.isNotEmpty()) {
            val result = cache.getOrPut(current) {
                println("Calculating result for point not in cache")
                dijkstras(current, graph)
            }
            println("Distances from $current ${remaining.map { it.toString() + result.distances[it]?.toString() }}")
            val next = remaining.minBy { result.distances[it]!! }!!
            remaining.remove(next)
            var temp = result.previous[next]!! // We don't want to re-add the waypoint
            val subPath = mutableListOf<Graph.Node<Int>>()
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
        path.add(end)
        return RouteFinder.RoutingResult.Route(path.map { Graph.Vertex(it, graph[it] ?: emptyList()) })
    }
    
    private fun dijkstras(source: Graph.Node<Int>, graph: Graph<Int>): DijkstraResult {
        println("Running dijkstras return RouteFinder.RoutingResult.Route(solver(graph, start, end, waypoints))from $source")
        val distances = HashMap<Graph.Node<Int>, Double>()
        val previous = HashMap<Graph.Node<Int>, Graph.Node<Int>>()
        val Q = HashSet<Graph.Node<Int>>()
        graph.forEach { vertex: Graph.Vertex<Int> ->
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

    private class DijkstraResult(val distances: HashMap<Graph.Node<Int>, Double>,
                                     val previous: HashMap<Graph.Node<Int>, Graph.Node<Int>>)
    
    
}