import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class Main {

    init {
        val graph = Graph(mutableListOf(), mutableMapOf())
        graph.nodes.addAll(
            (1..13).map { Node(it) }
        )
        graph.apply {
            addEdge(1, 4, 13)
            addEdge(1, 5, 7)
            addEdge(1, 6, 19)
            addEdge(2, 4, 12)
            addEdge(3, 4, 11)
            addEdge(3, 6, 19)
            addEdge(3, 7, 10)
            addEdge(4, 1, 13)
            addEdge(4, 2, 12)
            addEdge(4, 3, 11)
            addEdge(4, 6, 6)
            addEdge(5, 1, 7)
            addEdge(5, 6, 20)
            addEdge(5, 8, 20)
            addEdge(5, 10, 40)
            addEdge(6, 1, 19)
            addEdge(6, 3, 19)
            addEdge(6, 4, 6)
            addEdge(6, 5, 20)
            addEdge(6, 7, 20)
            addEdge(6, 8, 11)
            addEdge(6, 9, 11)
            addEdge(6, 11, 22)
            addEdge(7, 3, 10)
            addEdge(7, 6, 20)
            addEdge(7, 9, 20)
            addEdge(7, 12, 40)
            addEdge(8, 5, 20)
            addEdge(8, 6, 11)
            addEdge(8, 10, 15)
            addEdge(8, 11, 9)
            addEdge(9, 6, 13)
            addEdge(9, 7, 20)
            addEdge(9, 11, 8)
            addEdge(9, 12, 11)
            addEdge(10, 5, 40)
            addEdge(10, 8, 15)
            addEdge(10, 11, 5)
            addEdge(10, 13, 6)
            //A1B2C3D4E5F6G7H8I9J10K11L12M13
            addEdge(11, 6, 22)
            addEdge(11, 8, 9)
            addEdge(11, 9, 8)
            addEdge(11, 10, 5)
            addEdge(11, 12, 6)
            addEdge(11, 13, 12)
            addEdge(12, 9, 11)
            addEdge(12, 7, 40)
            addEdge(12, 11, 6)
            addEdge(12, 13, 9)
            addEdge(13, 10, 6)
            addEdge(13, 11, 12)
            addEdge(13, 12, 9)
        }
        println("Nodes ${graph.nodes}")
        println("Edges ${graph.edges.values}")
        val path = simpleSolver(graph, listOf(Node(2), Node(5), Node(13)))
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main()

        }
    }

    fun simpleSolver(graph: Graph, waypoints: List<Node>) {
        val jumps = waypoints.zip(waypoints.subList(1, waypoints.size))
        println("Jumps $jumps")
        val path = jumps.map { path(it.first, it.second, graph) }
        println("Jumping path $path")
    }

    fun path(source: Node, sink: Node, graph: Graph): List<Node> {
        println("Finding path from $source to $sink")
        val previous = dijkstras(source, graph)
        val path = mutableListOf<Node>()
        var current = sink
        while (current != source) {
            path.add(0, current)
            current = previous[current]!!
        }
        path.add(0, source)
        println("Found path $path from $source to $sink ")
        return path
    }

    fun dijkstras(source: Node, graph: Graph): HashMap<Node, Node> {
        val distances = HashMap<Node, Int>()
        val previous = HashMap<Node, Node>()
        val Q = HashSet<Node>()
        graph.nodes.forEach { node ->
            distances[node] = 100000
            Q.add(node)
        }
        distances[source] = 0
        while (Q.isNotEmpty()) {
            val min = Q.minBy { distances[it]!! }!!
            //println("Min node $min")
            Q.remove(min)
            graph.edges[min]?.forEach { edge ->
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
        return previous
    }


    fun prims(graph: Graph) {
        val costs = HashMap<Node, Edge>()
        val notIncluded = graph.nodes
        val forest = Graph(mutableListOf(), mutableMapOf())
        val initial = notIncluded.first() // Chosen at random
        addCosts(initial, costs, graph)
        notIncluded.remove(initial)
        while (notIncluded.isNotEmpty()) {
            // Choose minimum cost from available edges
            val minCost = costs.minBy {
                it.value.cost
            }!!
            //Remove it and add the node to the forest
            costs.remove(minCost.key)
            forest.nodes.add(minCost.key)

            // Add the edge from the previous forest to the new node
            val from = minCost.value.from
            if (forest.edges.containsKey(from)) {
                forest.edges[from]?.add(minCost.value)
            } else {
                forest.edges[from] = mutableSetOf(minCost.value)

                //Add any new outgoing edges to costs
                addCosts(minCost.key, costs, graph)
            }
        }
    }

    private fun addCosts(node: Node, costs: MutableMap<Node, Edge>, graph: Graph) {
        // For each edge from the node outwards, if there is no existing cost to its neighbour,
        // or the existing cost is higher, add the edge to costs
        graph.edges[node]?.forEach { edge ->
            if (!costs.containsKey(edge.to) || costs[edge.to]?.cost ?: 0 > edge.cost) {
                costs[edge.to] = edge
            }
        }
    }

    fun findTour(graph: Graph) {
        val tour = Stack<Node>()
        findTourR(graph.nodes.first(), graph, tour)
    }

    private fun findTourR(node: Node, graph: Graph, tour: Stack<Node>) {
        graph.edges[node]?.forEach {
            //TODO: Concurrent modification exception
            graph.edges[node]?.remove(it)
            findTourR(it.to, graph, tour)
        }
        tour.push(node)
    }


}

data class Graph(val nodes: MutableList<Node>, val edges: MutableMap<Node, MutableSet<Edge>>) {

    fun addEdge(idFrom: Int, idTo: Int, cost: Int, reverse: Boolean = true) {
        val from = Node(idFrom)
        val to = Node(idTo)
        val edge = Edge(cost, to, from)
        if (edges.containsKey(from)) {
            edges[from]?.add(edge)
        } else {
            edges[from] = mutableSetOf(edge)
        }
        if (reverse) {
            if (edges.containsKey(to)) {
                edges[to]?.add(edge.reverse())
            } else {
                edges[to] = mutableSetOf(edge.reverse())
            }
        }
    }

}

data class Node(val id: Int)

data class Edge(val cost: Int, val to: Node, val from: Node) {

    fun reverse() = Edge(cost, from, to)
}