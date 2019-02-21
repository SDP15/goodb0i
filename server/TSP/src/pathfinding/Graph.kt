package pathfinding


class Graph2<ID> : Collection<Graph2.Vertex<ID>> {
    private val nodes: MutableSet<Node<ID>> = mutableSetOf()
    private val edges: MutableMap<Node<ID>, MutableSet<Edge<ID>>> = hashMapOf()


    override val size: Int = nodes.size

    override fun contains(element: Vertex<ID>) = nodes.contains(element.node)

    override fun containsAll(elements: Collection<Vertex<ID>>): Boolean = nodes.containsAll(elements.map { it.node })

    override fun isEmpty() = nodes.isEmpty()

    override fun iterator(): Iterator<Vertex<ID>> = object : Iterator<Vertex<ID>> {
        private val internal = nodes.iterator()
        override fun hasNext() = internal.hasNext()

        override fun next(): Vertex<ID> {
            val node = internal.next()
            return Vertex(node, edges[node] ?: emptySet())
        }
    }

    companion object {

        fun <ID> graph(init: Graph2<ID>.() -> Unit): Graph2<ID> {
            val graph = Graph2<ID>()
            graph.init()
            return graph
        }

    }

    data class UnweightedEdge<ID>(val from: ID, val to: ID, var bidirectional: Boolean = false)


    infix fun ID.to(ids: Collection<ID>) = ids.map { UnweightedEdge(this, it) }

    infix fun ID.toFrom(ids: Collection<ID>) = ids.map { UnweightedEdge(this, it, true) }

    infix fun ID.to(id: ID) = UnweightedEdge(this, id)

    infix fun ID.toFrom(id: ID) = UnweightedEdge(this, id, true)


    infix fun UnweightedEdge<ID>.cost(cost: Int) {
        edge(from, to, cost, bidirectional)
    }

    infix fun List<UnweightedEdge<ID>>.cost(cost: Int) {
        forEach {
            edge(it.from, it.to, cost, it.bidirectional)
        }
    }

    infix fun List<UnweightedEdge<ID>>.costs(costs: Collection<Int>) {
        assert(size == costs.size) { "Must have one cost for each edge. $size edges. ${costs.size} costs" }
        zip(costs).forEach { (ue, cost) ->
            edge(ue.from, ue.to, cost, ue.bidirectional)
        }
    }

    fun edge(from: ID, to: ID, cost: Int, bidirectional: Boolean = false) {
        val fromNode = Node(from)
        val toNode = Node(to)
        if (!nodes.contains(fromNode)) nodes.add(fromNode)
        if (!nodes.contains(toNode)) nodes.add(toNode)
        addEdge(fromNode, toNode, cost, bidirectional)
    }

    private fun addEdge(fromNode: Node<ID>, toNode: Node<ID>, cost: Int, bidirectional: Boolean) {
        val edge = Edge(fromNode, toNode, cost)
        if (edges.containsKey(fromNode)) {
            edges[fromNode]?.add(edge)
        } else {
            edges[fromNode] = mutableSetOf(edge)
        }
        if (bidirectional) {
            if (edges.containsKey(toNode)) {
                edges[toNode]?.add(edge.reverse())
            } else {
                edges[toNode] = mutableSetOf(edge.reverse())
            }
        }
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

    operator fun plusAssign(vertex: Vertex<ID>) {
        nodes.add(vertex.node)
        if (edges.containsKey(vertex.node)) {
            edges[vertex.node]?.addAll(vertex.edges)
        } else {
            edges[vertex.node] = vertex.edges.toMutableSet()
        }
    }

    operator fun get(id: ID) = edges[Node(id)]

    operator fun get(node: Node<ID>) = edges[node]

    data class Vertex<ID>(val node: Node<ID>, val edges: Set<Edge<ID>>)

    data class Node<ID>(val id: ID)

    data class Edge<ID>(val from: Node<ID>, val to: Node<ID>, val cost: Int) {

        fun reverse() = Edge(to, from, cost)

    }


}