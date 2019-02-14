package graph;

/* See restrictions in Graph.java. */

import java.util.ArrayList;
import java.util.Iterator;

/** Represents a general unlabeled directed graph whose vertices are denoted by
 *  positive integers. Graphs may have self edges.
 *
 *  @author yaxinlei
 */
public class DirectedGraph extends GraphObj {

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public int inDegree(int v) {
        Iterator<Integer> allPredecessors = predecessors(v);

        int myInDegree = 0;

        while (allPredecessors.hasNext()) {
            allPredecessors.next();
            myInDegree += 1;
        }
        return myInDegree;
    }


    @Override
    public Iteration<Integer> predecessors(int v) {
        ArrayList<Integer> allPredecessors = new ArrayList<>();

        for (int i = 0; i < _vertex.size(); i++) {
            if (_graph.get(i).contains(v)) {
                allPredecessors.add((Integer) _vertex.get(i));
            }
        }
        return Iteration.iteration(allPredecessors);
    }
}
