package org.openscience.cdk.group;

import java.util.Set;

import org.junit.Assert;

import org.junit.Test;
import org.openscience.cdk.CDKTestCase;

/**
 * @author maclean
 * @cdk.module test-group
 */
public class AbstractDiscretePartitionRefinerTest extends CDKTestCase {

    public class Graph {

        public int     vertexCount;
        public int[][] connectionTable;

        public Graph(int vertexCount) {
            this.vertexCount = vertexCount;
        }
    }

    public class MockRefiner extends AbstractDiscretePartitionRefiner {

        public Graph graph;

        public MockRefiner(Graph graph) {
            super();
            this.graph = graph;
        }

        @Override
        public int getVertexCount() {
            return graph.vertexCount;
        }

        @Override
        public int getConnectivity(int vertexI, int vertexJ) {
            return graph.connectionTable[vertexI][vertexJ];
        }

    }

    public class MockEqRefiner extends AbstractEquitablePartitionRefiner implements IEquitablePartitionRefiner {

        public Graph graph;

        public MockEqRefiner(Graph graph) {
            this.graph = graph;
        }

        @Override
        public int getVertexCount() {
            return graph.vertexCount;
        }

        @Override
        public int neighboursInBlock(Set<Integer> block, int vertexIndex) {
            //            System.out.println("calling NIB for " + block + " " + vertexIndex);
            int neighbours = 0;
            int n = getVertexCount();
            for (int i = 0; i < n; i++) {
                if (graph.connectionTable[vertexIndex][i] == 1 && block.contains(i)) {
                    neighbours++;
                }
            }
            //            System.out.println(neighbours + " neighbours");
            return neighbours;
        }

    }

    @Test
    public void emptyConstructor() {
        MockRefiner refiner = new MockRefiner(null);
        Assert.assertNotNull(refiner);
    }

    @Test
    public void getVertexCountTest() {
        int n = 10;
        Graph g = new Graph(n);
        MockRefiner refiner = new MockRefiner(g);
        Assert.assertEquals(g.vertexCount, refiner.getVertexCount());
    }

    @Test
    public void getConnectivityTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
        MockRefiner refiner = new MockRefiner(g);
        Assert.assertEquals(1, refiner.getConnectivity(0, 1));
    }

    @Test
    public void setupTest() {
        int n = 5;
        PermutationGroup group = new PermutationGroup(n);
        Graph g = new Graph(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        Assert.assertEquals(group, refiner.getAutomorphismGroup());
    }

    @Test
    public void firstIsIdentityTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 1}, {1, 0, 0}, {1, 0, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        Assert.assertTrue(refiner.firstIsIdentity());
    }

    @Test
    public void getAutomorphismPartitionTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 1}, {1, 0, 0}, {1, 0, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        Partition autPartition = refiner.getAutomorphismPartition();
        Partition expected = Partition.fromString("0|1,2");
        Assert.assertEquals(expected, autPartition);
    }

    @Test
    public void getHalfMatrixStringTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 1}, {1, 0, 0}, {1, 0, 0}};
        MockRefiner refiner = new MockRefiner(g);
        String hms = refiner.getHalfMatrixString();
        String expected = "110";
        Assert.assertEquals(expected, hms);
    }

    @Test
    public void getBestHalfMatrixStringTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        String hms = refiner.getBestHalfMatrixString();
        String expected = "110";
        Assert.assertEquals(expected, hms);
    }

    @Test
    public void getFirstHalfMatrixStringTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 0, 1}, {0, 0, 1}, {1, 1, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        String hms = refiner.getFirstHalfMatrixString();
        String expected = "110";
        Assert.assertEquals(expected, hms);
    }

    @Test
    public void getGroupTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        Assert.assertNotNull(refiner.getAutomorphismGroup());
    }

    @Test
    public void getBestTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        Permutation best = refiner.getBest();
        Permutation expected = new Permutation(1, 0, 2);
        Assert.assertEquals(expected, best);
    }

    @Test
    public void getFirstTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        Permutation first = refiner.getFirst();
        Permutation expected = new Permutation(1, 0, 2);
        Assert.assertEquals(expected, first);
    }

    @Test
    public void isCanonical_TrueTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 1}, {1, 0, 0}, {1, 0, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        Assert.assertTrue(refiner.isCanonical());
    }

    @Test
    public void isCanonical_FalseTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 0}, {1, 0, 1}, {0, 1, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        Assert.assertFalse(refiner.isCanonical());
    }

    @Test
    public void refineTest() {
        int n = 3;
        Graph g = new Graph(n);
        g.connectionTable = new int[][]{{0, 1, 1}, {1, 0, 0}, {1, 0, 0}};
        PermutationGroup group = new PermutationGroup(n);
        MockRefiner refiner = new MockRefiner(g);
        refiner.setup(group, new MockEqRefiner(g));
        refiner.refine(Partition.unit(n));
        Assert.assertNotNull(refiner);
    }

}
