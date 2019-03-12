package service.routing

import org.jetbrains.exposed.sql.transactions.transaction
import repository.shelves.Shelf
import repository.shelves.ShelfRack
import repository.shelves.Shelves
import service.ListService
import kotlin.system.measureNanoTime

class RouteFinder(private val listService: ListService, private val graph: Graph<Int>) {

    private val cache = HashMap<Graph.Node<Int>, DijkstraResult<Int>>()

    init {
        generateAllPaths()
    }

    fun generateAllPaths() {
        val dur = measureNanoTime {
            graph.forEach { vertex ->
                cache[vertex.node] = dijkstras(vertex.node, graph)
            }
        } / 1E9
        println("Precomputation complete in $dur")

    }

    fun plan(code: Long): String {
        val list = listService.loadList(code)!!
        // Fruits, Dairy, Seafood, Sweets
        return transaction {
            val shelves = Shelf.find { Shelves.product inList list.products.map { it.product.id } }
            val racks = shelves.map { shelf -> ShelfRack[shelf.rack] }.toSet()

            val rackProductMap = racks.associate { rack ->
                Graph.Node(rack.id.value) to rack.shelves.mapNotNull { shelf ->
                    val index = list.products.indexOfFirst { it.product == shelf.product }
                    if (index == -1) null else index
                }
            }

            val path = convert(graph,
                    Graph.Node(start),
                    Graph.Node(end),
                    rackProductMap,
                    racks.map { rack -> Graph.Node(rack.id.value) })
            println("Generated path $path")
            return@transaction path
        }
    }

    private val start = 10
    private val end = 13

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

        }
    }


    private fun generateSupermarket(aisles: Int, racksPerAisle: Int, splitsPerAisle: Int) {
        (1..aisles).forEach {

        }
    }


    private fun intSolve(graph: Graph<Int>, start: Int, end: Int, rackToListMap: Map<Int, List<Int>>, waypoints: List<Int>): String {
        val route = solver(graph, Graph.Node(start), Graph.Node(end), waypoints.map { Graph.Node(it) })
        val builder = StringBuilder()
        val sep = ','
        val delim = '%'
        var previous = Graph.Node(start)
        route.forEach { node ->
            if (node.id == start) {
                builder.append("start")
            } else if (node.id == end) {
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
                if (node.id in waypoints) {
                    builder.append("stop$delim${node.id}")
                    val products = rackToListMap[node.id]
                    builder.append(products?.joinToString(separator = "$delim", prefix = "$delim"))


                } else {
                    builder.append("pass$delim${node.id}")
                }
            }

            builder.append(sep)
            previous = node
        }
        builder.setLength(builder.length - 1) // Remove last comma
        return builder.toString()
    }

    fun <ID> convert(graph: Graph<ID>,
                     start: Graph.Node<ID>,
                     end: Graph.Node<ID>,
                     productMap: Map<Graph.Node<ID>, List<Int>>,
                     waypoints: List<Graph.Node<ID>>): String {
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
                    val products = productMap[node]
                    builder.append(products?.joinToString(separator = "$delim", prefix = "$delim"))


                } else {
                    builder.append("pass$delim${node.id}")
                }
            }

            builder.append(sep)
            previous = node
        }
        builder.setLength(builder.length - 1) // Remove last comma
        return builder.toString()
    }

    fun <ID> solver(graph: Graph<ID>, start: Graph.Node<ID>, end: Graph.Node<ID>, waypoints: List<Graph.Node<ID>>): List<Graph.Node<ID>> {
        println("Running solver from $start to $end")
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
        path.add(end)
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