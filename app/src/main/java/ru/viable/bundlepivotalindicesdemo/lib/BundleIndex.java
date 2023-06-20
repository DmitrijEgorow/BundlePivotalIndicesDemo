package ru.viable.bundlepivotalindicesdemo.lib;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BundleIndex<V, E> implements VertexScoringAlgorithm<V, Double> {


    /**
     * Default value for the maximum cardinality of subsets
     */
    public static final int K_DEFAULT = 1;

    /**
     * Number of threads default value
     */
    public static final int NUM_THREADS_DEFAULT = 1;

    /**
     * Underlying graph
     */
    protected final Graph<V, E> graph;
    /**
     * The maximum cardinality of subsets
     */
    protected final int k;
    /**
     * The quota, not in percentage
     */
    protected final Map<V, Double> ql;
    /**
     * The actual scores
     */
    protected Map<V, Double> scores;
    /**
     * Whether to normalize scores
     */
    protected final boolean normalize;

    /**
     * Whether to use incoming or outgoing paths
     */
    protected final boolean incoming;
    /**
     * The Normalizer for unit vectors
     */
    protected final Normalizer<V, E> normalizer;
    /**
     * The ExecutorService for parallel computing
     */
    protected final ExecutorService executor;


    /**
     * Construct a new instance. By default the centrality is normalized and computed using incoming
     * edges
     *
     * @param graph the input graph
     * @param ql    threshold values for each node
     */
    public BundleIndex(Graph<V, E> graph, Map<V, Double> ql) {
        this(graph, K_DEFAULT, ql, true, true, NUM_THREADS_DEFAULT);
    }

    /**
     * Construct a new instance.
     *
     * @param graph the input graph
     * @param k     the maximum cardinality of critical sets (groups)
     * @param ql    threshold values for each node
     */
    public BundleIndex(Graph<V, E> graph, int k, Map<V, Double> ql) {
        this(graph, k, ql, true, true, NUM_THREADS_DEFAULT);
    }

    /**
     * Construct a new instance.
     *
     * @param graph     the input graph
     * @param ql        threshold values for each node
     * @param normalize whether to normalize the index values
     */
    public BundleIndex(Graph<V, E> graph, Map<V, Double> ql, boolean normalize) {
        this(graph, K_DEFAULT, ql, normalize, true, NUM_THREADS_DEFAULT);
    }

    /**
     * Construct a new instance.
     *
     * @param graph     the input graph
     * @param k         the maximum cardinality of critical sets (groups)
     * @param ql        threshold values for each node
     * @param normalize whether to normalize the index values
     */
    public BundleIndex(Graph<V, E> graph, int k, Map<V, Double> ql, boolean normalize) {
        this(graph, k, ql, normalize, true, NUM_THREADS_DEFAULT);
    }

    /**
     * Construct a new instance.
     *
     * @param graph     the input graph
     * @param ql        threshold values for each node
     * @param normalize whether to normalize the index values
     * @param incoming  if true incoming paths are used, otherwise outgoing paths
     */
    public BundleIndex(Graph<V, E> graph, Map<V, Double> ql, boolean normalize, boolean incoming) {
        this(graph, K_DEFAULT, ql, normalize, incoming, NUM_THREADS_DEFAULT);
    }

    /**
     * Construct a new instance.
     *
     * @param graph      the input graph
     * @param k          the maximum cardinality of critical sets (groups)
     * @param ql         threshold values for each node
     * @param normalize  whether to normalize the index values
     * @param incoming   if true incoming paths are used, otherwise outgoing paths
     * @param numThreads the number of threads reserved for computations
     */
    public BundleIndex(Graph<V, E> graph, int k, Map<V, Double> ql, boolean normalize, boolean incoming, int numThreads) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.k = k;
        this.ql = ql;
        this.scores = new ConcurrentHashMap<>();
        this.normalize = normalize;
        this.incoming = incoming;
        this.normalizer = new Normalizer<>();
        this.executor = Executors.newFixedThreadPool(numThreads);
        validate(graph, ql, k);
        computeBundleIndex();
        executor.shutdown();
    }

    /* Checks for the valid values of the parameters */
    private void validate(Graph<V, E> graph, Map<V, Double> ql, int k) {
        if (!graph.vertexSet().equals(ql.keySet())) {
            throw new IllegalArgumentException("Vertices in graph and in q do not match");
        }

        if (k <= 0) {
            throw new IllegalArgumentException("Maximum cardinality of critical groups must be positive");
        }

    }

    private void computeBundleIndex() {
        List<Future<Void>> futures = new ArrayList<>();

        for (V vertex : graph.vertexSet()) {
            Future<Void> future = executor.submit(() -> {
                double bl = 0;
                Set<V> t = new HashSet<>(graph.vertexSet());
                t.remove(vertex);
                Set<Set<V>> subsets = generateSubsets(t, vertex, k);

                for (Set<V> subset : subsets) {
                    double sum = 0;
                    for (V v : subset) {
                        E edge = incoming ? graph.getEdge(v, vertex) : graph.getEdge(vertex, v);
                        if (edge != null) {
                            sum += graph.getEdgeWeight(edge);
                        }
                    }

                    if (sum >= ql.get(vertex)) {
                        bl++;
                    }
                }

                scores.put(vertex, bl);
                return null;
            });

            futures.add(future);
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (normalize) {
            scores = normalizer.normalizeScores(graph, scores);
        }
    }


    /* Generates a set of subsets of vertices of cardinality not more than k */
    private Set<Set<V>> generateSubsets(Set<V> inputSet, V excludedVertex, int k) {
        List<V> inputList = new ArrayList<>(inputSet);
        Set<Set<V>> result = new HashSet<>();
        SubsetsGenerator<V, E> generator = new SubsetsGenerator<>();
        generator.generateSubsets(graph, inputList, result, new HashSet<>(), excludedVertex, 0, k, incoming);
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Double> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getVertexScore(V v) {
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return scores.get(v);
    }
}

