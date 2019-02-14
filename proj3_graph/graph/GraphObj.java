package graph;

/* See restrictions in Graph.java. */

import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A partial implementation of Graph containing elements common to
 *  directed and undirected graphs.
 *
 *  @author yaxinlei
 */
abstract class GraphObj extends Graph {

    /** This is the whole graph. */
    protected ArrayList<ArrayList<Integer>> _graph;

    /** This is the list of _vertices. */
    protected ArrayList<Integer> _vertex;

    /** Keeps record of all the edge counts. */
    private int _edgeCount;


    /** A new, empty Graph. */
    GraphObj() {
        _graph = new ArrayList<>();
        _vertex = new ArrayList<>();
        _edgeCount = 0;
    }

    @Override
    public int vertexSize() {
        return _vertex.size();
    }


    @Override
    public int maxVertex() {
        if (_vertex.size() == 0) {
            return 0;
        }
        int temp = _vertex.get(0);
        for (int i = 1; i < _vertex.size(); i++) {
            if (temp < _vertex.get(i)) {
                temp = _vertex.get(i);
            }
        }
        return temp;
    }

    @Override
    public int edgeSize() {
        return _edgeCount;
    }

    @Override
    public abstract boolean isDirected();


    @Override
    public int outDegree(int v) {
        if (contains(v)) {
            int index = _vertex.indexOf(v);
            return _graph.get(index).size();
        }

        return 0;
    }

    @Override
    public abstract int inDegree(int v);

    @Override
    public boolean contains(int u) {
        for (int index = 0; index < _vertex.size(); index += 1) {
            if (_vertex.get(index) == u) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(int u, int v) {
        if (!contains(u) || !contains(v)) {
            return false;
        }

        int indexU = _vertex.indexOf(u);
        ArrayList<Integer> neighborsU = _graph.get(indexU);
        for (int i : neighborsU) {
            if (i == v) {
                return true;
            }
        }

        if (!isDirected()) {
            int indexV = _vertex.indexOf(v);
            ArrayList<Integer> neighborsV = _graph.get(indexV);
            for (int j : neighborsV) {
                if (j == u) {
                    return true;
                }
            }
        }

        return false;
    }

    /** A comparator designed for comparing paths. */
    class Compare implements Comparator<Integer> {

        /** New comparator method for PathCompare. @param i and
         * @param j are both nodes. @return 0, -1, 1 indicating which node has
         * higher priority. */
        public int compare(Integer i, Integer j) {

            if (i == j) {
                return 0;
            }
            if (i < j) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    @Override
    public int add() {
        List<Integer> vertexCopy = new ArrayList<Integer>();
        for (int index = 0; index < _vertex.size(); index++) {
            vertexCopy.add((Integer) _vertex.get(index));
        }
        Collections.sort(vertexCopy, new Compare());

        int newVertex = 1;
        Boolean found = false;

        if (vertexCopy.size() == 0 || !vertexCopy.contains((Integer) 1)) {
            newVertex = 1;
        } else {
            for (int index = 0; index < vertexCopy.size() - 1; index++) {
                if ((int) vertexCopy.get(index + 1)
                        - (int) vertexCopy.get(index) > 1) {
                    newVertex = (int) vertexCopy.get(index) + 1;
                    found = true;
                    break;
                }
            }
            if (!found) {
                newVertex = (int) vertexCopy.get(vertexCopy.size() - 1) + 1;
            }
        }

        _vertex.add(newVertex);
        _graph.add(new ArrayList<Integer>());

        return newVertex;
    }


    @Override
    public int add(int u, int v) {

        if (_vertex.contains(u) && _vertex.contains(v)) {

            int indexU = _vertex.indexOf(u);
            int indexV = _vertex.indexOf(v);
            ArrayList<Integer> neighborsU = _graph.get(indexU);

            if (!neighborsU.contains((Integer) v)) {
                if (isDirected()) {
                    _edgeCount += 1;
                    _graph.get(indexU).add(v);
                } else {
                    if (u == v) {
                        _graph.get(indexU).add(v);
                        _edgeCount += 1;
                    } else {
                        _graph.get(indexU).add(v);
                        _graph.get(indexV).add(u);
                        _edgeCount += 1;
                    }
                }
            }

        }
        return edgeId(u, v);
    }



    @Override
    public void remove(int v) {
        if (contains(v)) {
            if (isDirected()) {
                int indexV = _vertex.indexOf(v);
                _edgeCount -= _graph.get(indexV).size();

                _vertex.remove(indexV);
                _graph.remove(indexV);

                for (ArrayList temp : _graph) {
                    if (temp.contains((Integer) v)) {
                        temp.remove((Integer) v);
                        _edgeCount -= 1;
                    }
                }
            } else {
                int indexV = _vertex.indexOf(v);
                _edgeCount -= _graph.get(indexV).size();
                _vertex.remove(indexV);
                _graph.remove(indexV);

                for (ArrayList temp : _graph) {
                    if (temp.contains((Integer) v)) {
                        temp.remove((Integer) v);
                    }
                }
            }
        }
    }



    @Override
    public void remove(int u, int v) {
        if (contains(u) && contains(v)) {
            if (isDirected()) {
                int indexU = _vertex.indexOf(u);
                if (_graph.get(indexU).contains((Integer) v)) {
                    _graph.get(indexU).remove((Integer) v);
                }
            } else {
                int indexU = _vertex.indexOf(u);
                int indexV = _vertex.indexOf(v);
                if (_graph.get(indexU).contains((Integer) v)) {
                    _graph.get(indexU).remove((Integer) v);
                    _graph.get(indexV).remove((Integer) u);
                }
            }
        }
    }

    @Override
    public Iteration<Integer> vertices() {
        return Iteration.iteration(_vertex);
    }

    @Override
    public Iteration<Integer> successors(int v) {
        if (!_vertex.contains(v)
                || _graph.get(_vertex.indexOf(v)).size() == 0) {
            return Iteration.iteration(new ArrayList<Integer>());
        } else {
            return Iteration.iteration(_graph.get(_vertex.indexOf(v)));
        }
    }

    @Override
    public abstract Iteration<Integer> predecessors(int v);

    @Override
    public Iteration<int[]> edges() {
        ArrayList<int[]> edgeList = new ArrayList<int[]>();

        if (isDirected()) {
            for (int index = 0; index < _vertex.size(); index++) {
                for (int latter : _graph.get(index)) {
                    int[] temp = {_vertex.get(index), latter};
                    edgeList.add(temp);
                }
            }
        } else {
            for (int index = 0; index < _vertex.size(); index++) {
                for (int latter : _graph.get(index)) {
                    int[] temp = {_vertex.get(index), latter};
                    boolean toAdd = true;
                    for (int j = 0; j < index; j++) {
                        if (latter == _vertex.get(j)) {
                            toAdd = false;
                        }
                    }
                    if (toAdd) {
                        edgeList.add(temp);
                    }
                }
            }
        }
        return Iteration.iteration(edgeList);
    }


    @Override
    protected void checkMyVertex(int v) {
        if (!contains(v)) {
            throw new IllegalArgumentException("vertex not from Graph");
        }

    }

    @Override
    protected int edgeId(int u, int v) {
        if (isDirected()) {
            return (u + v) * (u + v + 1) / 2 + v;
        } else {
            if (u >= v) {
                return ((u + v) * (u + v + 1)) / 2 + v;
            } else {
                return ((v + u) * (v + u + 1)) / 2 + u;
            }
        }
    }

}
