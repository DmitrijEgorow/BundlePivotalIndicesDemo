package ru.viable.bundlepivotalindicesdemo;

import static org.junit.Assert.assertEquals;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.viable.bundlepivotalindicesdemo.lib.SubsetsGenerator;

/**
 * Tests for Subsets
 */
public class SubsetGeneratorTest {
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
    public void testGeneratorWithDisconnectedGraph() {
        // Create a disconnected graph
        String[] vertices = new String[]{"A", "B", "C", "D"};
        Arrays.stream(vertices).forEach(v -> graph.addVertex(v));

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        Set<String> inputSet = new HashSet<>(graph.vertexSet());
        String excludedVertex = "D";
        inputSet.remove(excludedVertex);
        List<String> inputList = new ArrayList<>(inputSet);
        Set<Set<String>> result = new HashSet<>();
        SubsetsGenerator<String, DefaultWeightedEdge> generator = new SubsetsGenerator<>();
        generator.generateSubsets(graph, inputList, result, new HashSet<>(), excludedVertex, 0, 10, true);
        Set<Set<String>> answer = new HashSet<>();
        answer.add(new HashSet<>());
        assertEquals(answer, result);
    }

    @Test
    public void testGeneratorWithConnectedGraph() {
        // Create a disconnected graph
        String[] vertices = new String[]{"A", "B", "C", "D"};
        Arrays.stream(vertices).forEach(v -> graph.addVertex(v));

        graph.addEdge("A", "B");
        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        Set<String> inputSet = new HashSet<>(graph.vertexSet());
        String excludedVertex = "D";
        inputSet.remove(excludedVertex);
        List<String> inputList = new ArrayList<>(inputSet);
        Set<Set<String>> result = new HashSet<>();
        SubsetsGenerator<String, DefaultWeightedEdge> generator = new SubsetsGenerator<>();
        generator.generateSubsets(graph, inputList, result, new HashSet<>(), excludedVertex, 0, 2, true);
        Set<Set<String>> answer = new HashSet<>();
        answer.add(new HashSet<>());
        answer.add(Collections.singleton("B"));
        answer.add(Collections.singleton("C"));
        Set<String> pairedSet = new HashSet<>();
        pairedSet.add("B");
        pairedSet.add("C");
        answer.add(pairedSet);
        assertEquals(answer, result);
    }
}

