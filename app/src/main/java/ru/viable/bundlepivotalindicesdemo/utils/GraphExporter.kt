package ru.viable.bundlepivotalindicesdemo.utils

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.jgrapht.io.GmlExporter
import org.jgrapht.io.IntegerComponentNameProvider
import ru.viable.bundlepivotalindicesdemo.utils.Constants.Companion.countries
import java.io.File

class GraphExporter {
    lateinit var graph: Graph<String, DefaultWeightedEdge>

    fun export() {
        graph = GraphTypeBuilder.directed<Any, Any>()
            .allowingMultipleEdges(true).allowingSelfLoops(true)
            .weighted(true)
            .vertexClass(String::class.java)
            .edgeClass(DefaultWeightedEdge::class.java)
            .buildGraph()

        val ql: MutableMap<String, Double> = HashMap()
        for (v in graph.vertexSet()) {
            var sum = 0.0
            for (u in graph.vertexSet()) {
                val edge =
                    graph.getEdge(
                        u,
                        v,
                    )
                if (edge != null) {
                    sum += graph.getEdgeWeight(edge)
                }
            }
            ql[v] = sum
        }

        val exporter: GmlExporter<String, DefaultWeightedEdge> =
            CustomGmlExporter<String, DefaultWeightedEdge>(
                IntegerComponentNameProvider(),
                { component ->
                    countries[component.toInt()]
                },
                IntegerComponentNameProvider(),
                null,
            )

        val file = File(".", "sample_graph.gml")
        exporter.setParameter(GmlExporter.Parameter.EXPORT_EDGE_WEIGHTS, true)
        exporter.setParameter(GmlExporter.Parameter.EXPORT_VERTEX_LABELS, true)
        exporter.exportGraph(graph, file)
    }
}
