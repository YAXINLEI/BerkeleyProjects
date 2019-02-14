package graph;

/* See restrictions in Graph.java. */

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

/** The shortest paths through an edge-weighted graph.
 *  By overriding methods getWeight, setWeight, getPredecessor, and
 *  setPredecessor, the client can determine how to represent the weighting
 *  and the search results.  By overriding estimatedDistance, clients
 *  can search for paths to specific destinations using A* search.
 *  @author yaxinlei
 */
public abstract class ShortestPaths {

    /** The graph being searched. */
    protected final Graph _G;
    /** The starting vertex. */
    private final int _source;
    /** The target vertex. */
    private final int _dest;
    /** This priority queue gives vertices. */
    private PriorityQueue<Integer> _vertexQueue;



    /** The shortest paths in G from SOURCE. */
    public ShortestPaths(Graph G, int source) {
        this(G, source, 0);
    }


    /** A shortest path in G from SOURCE to DEST. */
    public ShortestPaths(Graph G, int source, int dest) {
        _G = G;
        _source = source;
        _dest = dest;
        Comparator<Integer> newComparator = new PathCompare();
        int vertexCount = _G.vertexSize();
        _vertexQueue = new PriorityQueue<Integer>(vertexCount, newComparator);
    }



    /** A comparator designed for comparing paths. */
    class PathCompare implements Comparator<Integer> {

        /** New comparator method for PathCompare. NODE1 and NODE2
         * are both nodes. @return 0, -1, 1 indicating which node has
         * higher priority. */
        public int compare(Integer node1, Integer node2) {

            double path1 = getWeight(node1) + estimatedDistance(node1);
            double path2 = getWeight(node2) + estimatedDistance(node2);

            if (path1 == path2) {
                return 0;
            }
            if (path1 < path2) {
                return -1;
            } else {
                return 1;
            }
        }
    }


    /** Initialize the shortest paths.  Must be called before using
     *  getWeight, getPredecessor, and pathTo. */
    public void setPaths() {
        setWeight(_source, 0);
        setPredecessor(_source, 0);

        for (int vertex : _G.vertices()) {
            _vertexQueue.add(vertex);
        }

        while (!_vertexQueue.isEmpty()) {
            int vertex = _vertexQueue.remove();
            if (vertex == _dest) {
                return;
            }
            Iterator succ = _G.successors(vertex);
            while (succ.hasNext()) {
                int temp = (Integer) succ.next();
                double weight = getWeight(vertex) + getWeight(vertex, temp);

                if (weight < getWeight(temp)) {
                    setWeight(temp, weight);
                    setPredecessor(temp, vertex);
                    _vertexQueue.remove(temp);
                    _vertexQueue.add(temp);
                }
            }
        }

    }

    /** Returns the starting vertex. */
    public int getSource() {
        return _source;
    }

    /** Returns the target vertex, or 0 if there is none. */
    public int getDest() {
        return _dest;
    }

    /** Returns the current weight of vertex V in the graph.  If V is
     *  not in the graph, returns positive infinity. */
    public abstract double getWeight(int v);

    /** Set getWeight(V) to W. Assumes V is in the graph. */
    protected abstract void setWeight(int v, double w);

    /** Returns the current predecessor vertex of vertex V in the graph, or 0 if
     *  V is not in the graph or has no predecessor. */
    public abstract int getPredecessor(int v);

    /** Set getPredecessor(V) to U. */
    protected abstract void setPredecessor(int v, int u);

    /** Returns an estimated heuristic weight of the shortest path from vertex
     *  V to the destination vertex (if any).  This is assumed to be less
     *  than the actual weight, and is 0 by default. */
    protected double estimatedDistance(int v) {
        return 0.0;
    }

    /** Returns the current weight of edge (U, V) in the graph.  If (U, V) is
     *  not in the graph, returns positive infinity. */
    protected abstract double getWeight(int u, int v);

    /** Returns a list of vertices starting at _source and ending
     *  at V that represents a shortest path to V.  Invalid if there is a
     *  destination vertex other than V. */
    public List<Integer> pathTo(int v) {

        ArrayList<Integer> path = new ArrayList<Integer>();
        path.add((Integer) v);

        int t = v;
        while (t != _source) {
            int pred = getPredecessor(t);
            path.add(pred);
            t = pred;
        }

        Collections.reverse(path);
        return path;
    }


    /** Returns a list of vertices starting at the source and ending at the
     *  destination vertex. Invalid if the destination is not specified. */
    public List<Integer> pathTo() {
        return pathTo(getDest());
    }

}
