package ru.viable.bundlepivotalindicesdemo.lib;

import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

public class Normalizer<V, E> {

    public Map<V, Double> normalizeScores(Graph<V, E> graph, Map<V, Double> scores) {
        Map<V, Double> unitScores = new HashMap<>();
        if (!graph.vertexSet().equals(scores.keySet())) {
            throw new IllegalArgumentException("Vertices in graph and in scores do not match");
        }
        double maxScore = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        if (maxScore == 0) {
            int n = graph.vertexSet().size();
            for (V vertex : scores.keySet()) {
                unitScores.put(vertex, 1.0 / n);
            }
        } else {
            for (V vertex : scores.keySet()) {
                unitScores.put(vertex, scores.get(vertex) / maxScore);
            }
        }
        return unitScores;
    }
}
