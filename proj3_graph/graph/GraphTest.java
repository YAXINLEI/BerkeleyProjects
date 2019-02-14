package graph;

import org.junit.Test;


import static org.junit.Assert.*;

/** Unit tests for the Graph class.
 *  @author yaxinlei
 */
public class GraphTest {

    @Test
    public void emptyGraph() {
        DirectedGraph g = new DirectedGraph();
        assertEquals("Initial graph has vertices", 0, g.vertexSize());
        assertEquals("Initial graph has edges", 0, g.edgeSize());
    }

    @Test
    public void vertexSizeSimple() {
        DirectedGraph g = new DirectedGraph();
        g.add();
        g.add();
        assertEquals("Graph has two vertices", 2, g.vertexSize());
    }

    @Test
    public void vertexContentSimple() {
        DirectedGraph g = new DirectedGraph();
        g.add();
        g.add();
        assertEquals("Graph has two vertices", true, g.contains(1));
        assertEquals("Graph has two vertices", true, g.contains(2));
    }

    @Test
    public void edgeSizeMedium() {
        UndirectedGraph g = new UndirectedGraph();
        g.add();
        g.add();
        g.add();
        g.add(1, 2);
        g.add(1, 3);
        assertEquals("Graph has two edges", 2, g.edgeSize());
    }

    @Test
    public void contentMedium() {
        UndirectedGraph g2 = new UndirectedGraph();
        g2.add();
        g2.add();
        g2.add();
        g2.add();
        g2.add();
        g2.add();
        g2.add(8, 5);
        g2.add(7, 11);
        g2.add(5, 11);
        g2.add(5, 6);
        assertEquals("Initial graph has 6 vertices", 6, g2.vertexSize());
        assertEquals("Initial graph has 1 edges", 1, g2.edgeSize());
        g2.remove(5);
        assertEquals("Initial graph has 1 edges", 0, g2.edgeSize());
    }

    @Test
    public void removeSimple() {
        UndirectedGraph g = new UndirectedGraph();
        g.add();
        g.add();
        g.add();
        g.add();

        g.add(1, 4);
        g.add(1, 3);
        g.remove(2);
        g.remove(3);
        assertEquals("Initial graph has 1 edges", 1, g.edgeSize());
        assertEquals("Initial graph has 1 edges", true, g.contains(1));
        assertEquals("Initial graph has 1 edges", true, g.contains(4));
        assertEquals("Initial graph has 1 edges", false, g.contains(3));
        assertEquals("Initial graph has 1 edges", true, g.contains(1, 4));
    }

    @Test
    public void outdegree() {
        UndirectedGraph g = new UndirectedGraph();
        g.add();
        g.add();
        g.add();
        g.add();
        g.add();

        g.add(1, 4);
        g.add(1, 3);
        g.add(1, 2);
        assertEquals("Initial graph has 1 edges", 3, g.outDegree(1));
        assertEquals("Initial graph has 1 edges", 1, g.outDegree(3));
    }

    @Test
    public void outdegreeDir() {
        DirectedGraph g = new DirectedGraph();
        g.add();
        g.add();
        g.add();
        g.add();
        g.add();

        g.add(1, 4);
        g.add(1, 3);
        g.add(1, 2);
        assertEquals("Initial graph has 1 edges", 3, g.outDegree(1));
        assertEquals("Initial graph has 1 edges", 0, g.outDegree(3));
        assertEquals("Initial graph has 1 edges", 0, g.outDegree(2));
        assertEquals("Initial graph has 1 edges", 0, g.outDegree(4));
    }
}
