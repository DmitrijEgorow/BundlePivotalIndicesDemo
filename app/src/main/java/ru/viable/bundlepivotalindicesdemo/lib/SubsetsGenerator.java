package ru.viable.bundlepivotalindicesdemo.lib;

import org.jgrapht.Graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubsetsGenerator<V, E> {

    public void generateSubsets(Graph<V, E> graph, List<V> inputList, Set<Set<V>> result, Set<V> currentSubset, V excludedVertex, int index, int k, boolean incoming) {
        if (currentSubset.size() <= k) {
            if (currentSubset.stream().allMatch(e ->
                    incoming ?
                            graph.containsEdge(e, excludedVertex) :
                            graph.containsEdge(excludedVertex, e)
            )) {
                result.add(new HashSet<>(currentSubset));
            }
        }

        if (currentSubset.size() == k || index == inputList.size()) {
            return;
        }

        V element = inputList.get(index);
        generateSubsets(graph, inputList, result, currentSubset, excludedVertex, index + 1, k, incoming);

        if (currentSubset.size() <= k) {
            currentSubset.add(element);
            generateSubsets(graph, inputList, result, currentSubset, excludedVertex, index + 1, k, incoming);
            currentSubset.remove(element);
        }

    }
}
