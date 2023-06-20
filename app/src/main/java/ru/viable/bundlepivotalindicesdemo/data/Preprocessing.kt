package ru.viable.bundlepivotalindicesdemo.data

import android.content.Context
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.jgrapht.io.Attribute
import org.jgrapht.io.EdgeProvider
import org.jgrapht.io.GmlImporter
import org.jgrapht.io.VertexProvider
import ru.viable.bundlepivotalindicesdemo.lib.BundleIndex
import ru.viable.bundlepivotalindicesdemo.lib.PivotalIndex
import ru.viable.bundlepivotalindicesdemo.presentation.CalculationCallback
import ru.viable.bundlepivotalindicesdemo.utils.Constants.Companion.countries

class Preprocessing {
    fun calculate(context: Context, file: String, callback: CalculationCallback) {
        val vertexProvider =
            VertexProvider { label: String?, attributes: Map<String?, Attribute?>? -> label }
        val edgeProvider =
            EdgeProvider { from: String?, to: String?, label: String?, attributes: Map<String?, Attribute?>? -> DefaultWeightedEdge() }

        val importer: GmlImporter<String, DefaultWeightedEdge> =
            GmlImporter<String, DefaultWeightedEdge>(vertexProvider, edgeProvider)
        val graph = GraphTypeBuilder
            .directed<String, DefaultWeightedEdge>()
            .allowingMultipleEdges(true).allowingSelfLoops(true)
            .weighted(true)
            .edgeClass(DefaultWeightedEdge::class.java)
            .buildGraph()

        importer.importGraph(graph, context.assets.open(file))
        println(graph.vertexSet())

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

        graph.vertexSet().forEach { v -> ql[v] = 0.45 * ql[v]!! }
        var w = StringBuilder()
        val bundleIndex = BundleIndex(
            graph,
            2,
            ql,
            true,
            true,
            8,
        )
        var i = 0
        w.append("Top-10 BI\n")
        bundleIndex.scores.mapKeys { countries[Integer.valueOf(it.key)] }.mapValues { it.value / 1 }
            .entries.sortedBy { -it.value }.take(15).forEach { (e, v) ->
                i++; w.append(
                    "$i.\t\t$e\t\t" + String.format(
                        "%.3f",
                        v,
                    ) + "\n",
                )
            }
        val pivotalIndex = PivotalIndex(graph, 2, ql, true, true, 8)
        var j = 1
        w.append("\nTop-10 PI\n")
        pivotalIndex.scores.mapKeys { countries[Integer.valueOf(it.key) ] }.mapValues { it.value / 1 }
            .entries.sortedBy { -it.value }.take(15).forEach { (e, v) ->
                w.append(
                    "$j.\t\t$e\t\t" + String.format(
                        "%.3f",
                        v,
                    ) + "\n",
                ); j++
            }
        callback.onReceiveResults(w.toString())
    }
}
