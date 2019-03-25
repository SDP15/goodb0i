package service.routing

/**
 * This graph structure is fairly generic. There are probably too many ways to construct a graph
 */
class Graph<ID> : Collection<Graph.Vertex<ID>> {
    private val nodes: MutableSet<Node<ID>> = mutableSetOf()
    private val edges: MutableMap<Node<ID>, MutableList<Edge<ID>>> = hashMapOf()
    var start: Node<ID>? = null
        private set
    var end: Node<ID>? = null
        private set
    override val size: Int = nodes.size

    override fun contains(element: Vertex<ID>) = nodes.contains(element.node)

    override fun containsAll(elements: Collection<Vertex<ID>>): Boolean = nodes.containsAll(elements.map { it.node })

    override fun isEmpty() = nodes.isEmpty()

    override fun iterator(): Iterator<Vertex<ID>> = object : Iterator<Vertex<ID>> {
        private val internal = nodes.iterator()
        override fun hasNext() = internal.hasNext()

        override fun next(): Vertex<ID> {
            val node = internal.next()
            return Vertex(node, edges[node] ?: emptyList())
        }
    }

    companion object {

        fun <ID> graph(init: Graph<ID>.() -> Unit): Graph<ID> {
            val graph = Graph<ID>()
            graph.init()
            return graph
        }

    }


    override fun toString(): String {
        return "Graph(${nodes.map { "$it : ${edges[it]}\n" }})"
    }

    data class Vertex<ID>(val node: Node<ID>, val edges: List<Edge<ID>>) : List<Edge<ID>> by edges

    data class Node<ID>(val id: ID)

    data class Edge<ID>(val from: Node<ID>, val to: Node<ID>, val cost: Int) {

        fun reverse() = Edge(to, from, cost)

    }

    // Builder functions

    data class UnweightedEdge<ID>(val from: ID, val to: ID, var bidirectional: Boolean = false)

    infix fun ID.to(ids: Collection<Pair<ID, Int>>) {
        ids.forEach { (to, cost) ->
            edge(this, to, cost)
        }
    }

    infix fun ID.toFrom(ids: Collection<Pair<ID, Int>>) {
        ids.forEach { (to, cost) ->
            edge(this, to, cost, true)
        }
    }

    infix fun ID.to(ids: Collection<ID>) = ids.map { UnweightedEdge(this, it) }

    infix fun ID.toFrom(ids: Collection<ID>) = ids.map { UnweightedEdge(this, it, true) }

    infix fun ID.to(id: ID) = UnweightedEdge(this, id)

    infix fun ID.toFrom(id: ID) = UnweightedEdge(this, id, true)


    infix fun UnweightedEdge<ID>.cost(cost: Int): Edge<ID> = edge(from, to, cost, bidirectional)


    infix fun List<UnweightedEdge<ID>>.cost(cost: Int): List<Edge<ID>> =
            map {
                edge(it.from, it.to, cost, it.bidirectional)
            }


    infix fun ID.cost(cost: Int) = Pair(this, cost)

    infix fun Edge<ID>.to(id: ID) = UnweightedEdge(this.to.id, id)

    infix fun Edge<ID>.toFrom(id: ID) = UnweightedEdge(this.to.id, id, true)

    infix fun List<UnweightedEdge<ID>>.costs(costs: Collection<Int>) {
        assert(size == costs.size) { "Must have one cost for each edge. $size edges. ${costs.size} costs" }
        zip(costs).forEach { (ue, cost) ->
            edge(ue.from, ue.to, cost, ue.bidirectional)
        }
    }

    fun start(id: ID) {
        val node = Node(id)
        start = node
        if (!nodes.contains(node)) nodes.add(node)
    }

    fun end(id: ID) {
        val node = Node(id)
        end = node
        if (!nodes.contains(node)) nodes.add(node)
    }

    fun edge(from: ID, to: ID, cost: Int, bidirectional: Boolean = false): Edge<ID> {
        val fromNode = Node(from)
        val toNode = Node(to)
        println("Edge between $from and $to")
        if (!nodes.contains(fromNode)) {
            println("Adding node for $from")
            nodes.add(fromNode)
        }
        if (!nodes.contains(toNode)) {
            println("Adding node for $to")
            nodes.add(toNode)
        }
        return addEdge(fromNode, toNode, cost, bidirectional)
    }

    private fun addEdge(fromNode: Node<ID>, toNode: Node<ID>, cost: Int, bidirectional: Boolean): Edge<ID> {
        val edge = Edge(fromNode, toNode, cost)
        if (edges.containsKey(fromNode)) {
            edges[fromNode]?.add(edge)
        } else {
            edges[fromNode] = mutableListOf(edge)
        }
        if (bidirectional) {
            if (edges.containsKey(toNode)) {
                edges[toNode]?.add(edge.reverse())
            } else {
                edges[toNode] = mutableListOf(edge.reverse())
            }
        }
        return edge
    }

    operator fun plusAssign(edge: Triple<ID, ID, Int>) {
        edge(edge.first, edge.second, edge.third)
    }


    operator fun plusAssign(id: ID) {
        nodes.add(Node(id))
    }

    operator fun plusAssign(node: Node<ID>) {
        nodes.add(node)
    }

    operator fun minusAssign(node: ID) {
        nodes.remove(Node(node))
    }

    operator fun minusAssign(node: Node<ID>) {
        nodes.remove(node)
    }


    operator fun get(id: ID) = edges[Node(id)]

    operator fun get(node: Node<ID>) = edges[node]

    operator fun get(vertex: Vertex<ID>) = edges[vertex.node]

}

