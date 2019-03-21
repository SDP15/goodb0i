package service.routing

import org.jetbrains.exposed.sql.transactions.transaction
import repository.shelves.Shelf
import repository.shelves.ShelfRack
import repository.shelves.Shelves
import service.ListService
import kotlin.system.measureNanoTime

class IntRouteFinder(private val listService: ListService, private val graph: Graph<Int>): RouteFinder {
    
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

    override fun plan(code: Long): String {
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
                    graph.start!!,
                    graph.end!!,
                    rackProductMap,
                    racks.map { rack -> Graph.Node(rack.id.value) })
            println("Generated path $path")
            return@transaction path
        }
    }

    private fun convert(graph: Graph<Int>,
                     start: Graph.Node<Int>,
                     end: Graph.Node<Int>,
                     productMap: Map<Graph.Node<Int>, List<Int>>,
                     waypoints: List<Graph.Node<Int>>): String {
        val route = solver(graph, start, end, waypoints)
        val builder = StringBuilder()
        val sep = ','
        val delim = '%'
        var previous = start
        route.forEach { node ->
            if (node == start) {
                builder.append("start")
            } else if (node == end) {
                builder.append("end$delim${node.id}")
            } else {
                // Add a turn if there's more than one way to get to the next node
                val edges = graph[previous]
                if (edges != null && edges.size > 1) {
                    val index = edges.indexOfFirst { edge -> edge.to == node }
                    println("Node $node Edges are $edges. Index is $index")
                    when (index) {
                        //0 -> builder.append("left")
                        0 -> builder.append("forward")
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
    
    private fun solver(graph: Graph<Int>, start: Graph.Node<Int>, end: Graph.Node<Int>, waypoints: List<Graph.Node<Int>>): List<Graph.Node<Int>> {
        println("Running solver from $start to $end through $waypoints")
        var current = start
        val remaining = waypoints.toMutableList()
        val path = mutableListOf<Graph.Node<Int>>()
        while (remaining.isNotEmpty()) {
            val result = cache.getOrPut(current) {
                println("Calculating result for point not in cache")
                dijkstras(current, graph)
            }
            println("Distances for ${remaining.map { it.toString() + result.distances[it]?.toString() }}")
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
        return path
    }
    
    private fun dijkstras(source: Graph.Node<Int>, graph: Graph<Int>): DijkstraResult {
        println("Running dijkstras from $source")
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