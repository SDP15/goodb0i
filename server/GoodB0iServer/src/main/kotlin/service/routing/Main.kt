package service.routing

import service.routing.Graph.Companion.graph

class Main {

    init {

        val graph = graph<String> {
            val cost = 5
            ("a" to "b" cost 1 to "c" cost 2 to "d" cost 5)
            "TEST" toFrom listOf("TEST2" cost 5, "TEST3" cost 6)
            "ENTRANCE" to "FRUITS" cost cost
            "FRUITS" to "VEGETABLES" cost cost
            "VEGETABLES" toFrom "VEG_CORNER" cost 2 * cost
            "VEG_CORNER" toFrom "DAIRY_CORNER" cost 2 * cost
            "DAIRY_CORNER" to "DAIRY" cost cost
            "DAIRY" to listOf("BAKERY" cost cost, "SEAFOOD" cost 2 * cost)
            // "DAIRY" to listOf("BAKERY", "SEAFOOD") costs listOf(cost, 2*cost)
            "BAKERY" to listOf("MEAT", "BOTTOM_CORNER") costs listOf(cost, 2 * cost)
            "BOTTOM_CORNER" to "TILLS" cost 2 * cost
            "BOTTOM_CORNER" to "MEAT" cost cost
            "MEAT" to "SEAFOOD" cost cost
            "MEAT" to "BAKERY" cost 2 * cost
            "SEAFOOD" to "SEAFOOD_CORNER" cost cost
            "SEAFOOD" to "DAIRY" cost 2 * cost
            "SEAFOOD_CORNER" to "FOOD_CUPBOARD_CORNER" cost cost
            "FOOD_CUPBOARD_CORNER" to "FOOD_CUPBOARD" cost cost
            "FOOD_CUPBOARD" to "SWEETS" cost cost
            "SWEETS" to "TILLS" cost cost

        }

        val other = graph<Int> {
            edge(1, 4, 13)
            edge(1, 5, 7)
            edge(1, 6, 19)
            edge(2, 4, 12)
            edge(3, 4, 11)
            edge(3, 6, 19)
            edge(3, 7, 10)
            edge(4, 1, 13)
            edge(4, 2, 12)
            edge(4, 3, 11)
            edge(4, 6, 6)
            edge(5, 1, 7)
            edge(5, 6, 20)
            edge(5, 8, 20)
            edge(5, 10, 40)
            edge(6, 1, 19)
            edge(6, 3, 19)
            edge(6, 4, 6)
            edge(6, 5, 20)
            edge(6, 7, 20)
            edge(6, 8, 11)
            edge(6, 9, 11)
            edge(6, 11, 22)
            edge(7, 3, 10)
            edge(7, 6, 20)
            edge(7, 9, 20)
            edge(7, 12, 40)
            edge(8, 5, 20)
            edge(8, 6, 11)
            edge(8, 10, 15)
            edge(8, 11, 9)
            edge(9, 6, 13)
            edge(9, 7, 20)
            edge(9, 11, 8)
            edge(9, 12, 11)
            edge(10, 5, 40)
            edge(10, 8, 15)
            edge(10, 11, 5)
            edge(10, 13, 6)
            edge(11, 6, 22)
            edge(11, 8, 9)
            edge(11, 9, 8)
            edge(11, 10, 5)
            edge(11, 12, 6)
            edge(11, 13, 12)
            edge(12, 9, 11)
            edge(12, 7, 40)
            edge(12, 11, 6)
            edge(12, 13, 9)
            edge(13, 10, 6)
            edge(13, 11, 12)
            edge(13, 12, 9)
        }
        val path = solver(other, Graph.Node(2), Graph.Node(13), listOf(Graph.Node(3), Graph.Node(10)))
        println("Path $path")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main()

        }
    }

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
            while (temp != current) {
                path.add(temp)
                temp = result.previous[temp]!!
            }
            path.add(current)
            println("Path is now $path")
            current = next
            if (current == end) break
            if (remaining.isEmpty()) remaining.add(end)

        }
        return path
    }

    fun <ID> path(source: Graph.Node<ID>, sink: Graph.Node<ID>, graph: Graph<ID>): List<Graph.Node<ID>> {
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

    fun <ID> simpleSolver(graph: Graph<ID>, waypoints: List<Graph.Node<ID>>) {
        val jumps = waypoints.zip(waypoints.subList(1, waypoints.size))
        println("Jumps $jumps")
        val path = jumps.map { path(it.first, it.second, graph) }
        println("Jumping path $path")
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

    private inner class DijkstraResult<ID>(val distances: HashMap<Graph.Node<ID>, Double>,
                                           val previous: HashMap<Graph.Node<ID>, Graph.Node<ID>>)


//
//    fun prims(graph: Graph) {
//        val costs = HashMap<Node, Edge>()
//        val notIncluded = graph.nodes
//        val forest = Graph(mutableListOf(), mutableMapOf())
//        val initial = notIncluded.first() // Chosen at random
//        addCosts(initial, costs, graph)
//        notIncluded.remove(initial)
//        while (notIncluded.isNotEmpty()) {
//            // Choose minimum cost from available edges
//            val minCost = costs.minBy {
//                it.value.cost
//            }!!
//            //Remove it and add the node to the forest
//            costs.remove(minCost.key)
//            forest.nodes.add(minCost.key)
//
//            // Add the edge from the previous forest to the new node
//            val from = minCost.value.from
//            if (forest.edges.containsKey(from)) {
//                forest.edges[from]?.add(minCost.value)
//            } else {
//                forest.edges[from] = mutableSetOf(minCost.value)
//
//                //Add any new outgoing edges to costs
//                addCosts(minCost.key, costs, graph)
//            }
//        }
//    }
//
//    private fun addCosts(node: Node, costs: MutableMap<Node, Edge>, graph: Graph) {
//        // For each edge from the node outwards, if there is no existing cost to its neighbour,
//        // or the existing cost is higher, add the edge to costs
//        graph.edges[node]?.forEach { edge ->
//            if (!costs.containsKey(edge.to) || costs[edge.to]?.cost ?: 0 > edge.cost) {
//                costs[edge.to] = edge
//            }
//        }
//    }
//
//    fun findTour(graph: Graph) {
//        val tour = Stack<Node>()
//        findTourR(graph.nodes.first(), graph, tour)
//    }
//
//    private fun findTourR(node: Node, graph: Graph, tour: Stack<Node>) {
//        graph.edges[node]?.forEach {
//            //TODO: Concurrent modification exception
//            graph.edges[node]?.remove(it)
//            findTourR(it.to, graph, tour)
//        }
//        tour.push(node)
//    }


}

