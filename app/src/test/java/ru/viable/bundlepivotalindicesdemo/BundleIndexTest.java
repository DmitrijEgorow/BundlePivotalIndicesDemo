package ru.viable.bundlepivotalindicesdemo;

import static org.junit.Assert.assertEquals;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ru.viable.bundlepivotalindicesdemo.lib.BundleIndex;

/**
 * Tests for Bundle Index
 */
public class BundleIndexTest {
    private Graph<String, DefaultWeightedEdge> graph;

    @Before
    public void setUp() {
        graph = GraphTypeBuilder.directed()
                .allowingMultipleEdges(true).allowingSelfLoops(true)
                .weighted(true)
                .vertexClass(String.class)
                .edgeClass(DefaultWeightedEdge.class)
                .buildGraph();
    }

    @Test
    public void testBundleIndexSmallGraph() {
        // Create a graph
        String[] vertices = new String[]{"A", "B", "C", "D"};
        double[] expectedIndex = new double[]{1.0, 1.0, 6.0, 0.0};
        Arrays.stream(vertices).forEach(v -> graph.addVertex(v));

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");
        graph.addEdge("D", "C");

        Map<String, Double> ql = new HashMap<>();
        graph.vertexSet().forEach(s -> ql.put(s, 1.0));
        BundleIndex<String, DefaultWeightedEdge> bundleIndex = new BundleIndex<>(graph, 2, ql, false);
        double eps = 0.0001;

        // Verify correctness
        checkIndex(vertices, expectedIndex, bundleIndex, eps);

    }

    @Test
    public void testBundleIndexSingleNode() {
        // Create a graph with a single node
        graph.addVertex("A");
        Map<String, Double> ql = new HashMap<>();
        graph.vertexSet().forEach(s -> ql.put(s, 1.0));
        BundleIndex<String, DefaultWeightedEdge> bundleIndex = new BundleIndex<>(graph, ql, true);
        double eps = 0.0001;

        // Verify correctness
        assertEquals("BI for A is incorrect", 1.0, bundleIndex.getVertexScore("A"), eps);

    }


    @Test
    public void testBundleIndexLargeStarGraph() {
        // Create a graph with vertex A and n - 1 vertices B_i
        int n = 101;
        int weight = 10;
        String[] vertices = new String[n];
        double[] expectedIndex = new double[n];
        vertices[0] = "A";
        graph.addVertex(vertices[0]);
        expectedIndex[0] = 1;
        for (int i = 1; i < n; i++) {
            vertices[i] = "B" + i;
            graph.addVertex(vertices[i]);
            expectedIndex[i] = 0;
            DefaultWeightedEdge e = graph.addEdge(vertices[i], vertices[0]);
            graph.setEdgeWeight(e, weight);
        }

        Map<String, Double> ql = new HashMap<>();
        graph.vertexSet().forEach(s -> ql.put(s, 1.0));
        BundleIndex<String, DefaultWeightedEdge> bundleIndex = new BundleIndex<>(graph, 2, ql, true);
        double eps = 0.0001;

        // Verify correctness
        checkIndex(vertices, expectedIndex, bundleIndex, eps);

    }

    @Test
    public void testBundleIndexDisconnectedGraph() {
        // Create a disconnected graph
        String[] vertices = new String[]{"A", "B", "C", "D"};
        double[] expectedIndex = new double[]{0.0, 1.0, 0.0, 1.0};
        Arrays.stream(vertices).forEach(v -> graph.addVertex(v));

        graph.addEdge("A", "B");
        graph.addEdge("C", "D");

        Map<String, Double> ql = new HashMap<>();
        graph.vertexSet().forEach(s -> ql.put(s, 1.0));
        BundleIndex<String, DefaultWeightedEdge> bundleIndex = new BundleIndex<>(graph, ql, false);
        double eps = 0.0001;

        // Verify correctness
        checkIndex(vertices, expectedIndex, bundleIndex, eps);
    }

    private void checkIndex(String[] vertices, double[] expectedValues, BundleIndex<String, DefaultWeightedEdge> index, double eps) {
        for (int i = 0; i < vertices.length; i++) {
            assertEquals("BI for " + vertices[i] + " is incorrect",
                    expectedValues[i], index.getVertexScore(vertices[i]), eps);
        }
    }
}