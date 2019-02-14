package graph;

/* See restrictions in Graph.java. */

import java.util.ArrayList;

/** A partial implementation of ShortestPaths that contains the weights of
 *  the vertices and the predecessor edges.   The client needs to
 *  supply only the two-argument getWeight method.
 *  @author yaxinlei
 */
public abstract class SimpleShortestPaths extends ShortestPaths {
    /** Stores all vertex. */
    private ArrayList<Integer> _vertexKey;
    /** Stores all corresponding weights of the keys. */
    private ArrayList<Double> _vertexWeight;
    /** Stores keys of predecessors. */
    private ArrayList<Integer> _predecessorKey;
    /** Stores of the corresponding predecessors for a key of node. */
    private ArrayList<Integer> _predecessorContent;

    /** The shortest paths in G from SOURCE. */
    public SimpleShortestPaths(Graph G, int source) {
        this(G, source, 0);
    }

    /** A shortest path in G from SOURCE to DEST. */
    public SimpleShortestPaths(Graph G, int source, int dest) {

        super(G, source, dest);
        _vertexKey = new ArrayList<Integer>();
        _vertexWeight = new ArrayList<Double>();

        _predecessorKey = new ArrayList<Integer>();
        _predecessorContent = new ArrayList<Integer>();
    }

    /** Returns the current weight of edge (U, V) in the graph.  If (U, V) is
     *  not in the graph, returns positive infinity. */

    @Override
    protected abstract double getWeight(int u, int v);

    @Override
    public double getWeight(int v) {
        if (_vertexKey.contains((Integer) v)) {
            int index = _vertexKey.indexOf((Integer) v);
            return _vertexWeight.get(index);
        }
        return Double.MAX_VALUE;
    }

    @Override
    protected void setWeight(int v, double w) {
        if (_vertexKey.contains((Integer) v)) {
            int index = _vertexKey.indexOf((Integer) v);
            _vertexWeight.set(index, (Double) w);
        } else {
            _vertexKey.add((Integer) v);
            _vertexWeight.add((Double) w);
        }
    }

    @Override
    /** Returns the current predecessor vertex of vertex V in the graph, or 0 if
     *  V is not in the graph or has no predecessor. */
    public int getPredecessor(int v) {
        if (!super._G.contains(v)) {
            return 0;
        }
        if (_predecessorKey.contains((Integer) v)) {
            int index = _predecessorKey.indexOf((Integer) v);
            return (int) _predecessorContent.get(index);
        }
        return 0;
    }

    @Override
    protected void setPredecessor(int v, int u) {
        if (_predecessorKey.contains((Integer) v)) {
            int index = _predecessorKey.indexOf((Integer) v);
            _predecessorContent.set(index, (Integer) u);
        } else {
            _predecessorKey.add((Integer) v);
            _predecessorContent.add((Integer) u);
        }

    }
}
