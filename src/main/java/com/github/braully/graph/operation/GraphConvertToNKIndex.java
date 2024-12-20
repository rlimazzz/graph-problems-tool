package com.github.braully.graph.operation;

import com.github.braully.graph.CombinationsFacade;
import com.github.braully.graph.GraphWS;
import com.github.braully.graph.GraphTO;
import java.math.BigInteger;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class GraphConvertToNKIndex implements IGraphOperation {

    static final String type = "General";
    static final String description = "Convert to N,M,Index";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(GraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        String code = graphToNMIndexedCode(graph);
        response.put("n,k,indexed", code);
        return response;
    }

    public static synchronized String graphToNMIndexedCode(GraphTO<Integer, Integer> graph) {
        String code = null;
        long n = 0;
        long k = 0;
        try {
            n = graph.getVertexCount();
            k = graph.getEdgeCount();
            long maxEdges = (n * (n - 1)) / 2;
            int[] comb = new int[(int) k];
            int combi = 0;
            int countEdge = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i; j < n - 1; j++) {
                    Integer source = i;
                    Integer target = j + 1;
                    if (graph.isNeighbor(source, target)) {
                        comb[combi++] = countEdge;
                    }
                    countEdge++;
                }
            }
            k = combi;

            System.out.printf("Comb-edge = {");
            for (int i = 0; i < comb.length; i++) {
                System.out.printf("%d", comb[i]);
                if (i < comb.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("}");
            BigInteger index = CombinationsFacade.lexicographicIndexBig((int) maxEdges, (int) combi, comb);
            code = n + "," + k + "," + index.toString();
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return code;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    /**
     * Reference:
     * https://github.com/piyushroshan/GraphTheoryProject/blob/master/src/gtc/MaxTriangleFreeGraph.java
     */
    public boolean isTriangleFree(GraphTO<Integer, Integer> graph) {
        if (graph == null || graph.getVertexCount() == 0) {
            return false;
        }
        int n = graph.getVertexCount();
        int[][] x = new int[n][n];
        int[][] y = new int[n][n];
        int trace = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum = sum + graph.getAdjacency(i, k) * graph.getAdjacency(k, j);
                }
                x[i][j] = sum;
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum = sum + x[i][k] * graph.getAdjacency(k, j);
                }
                y[i][j] = sum;
            }
        }

        for (int i = 0; i < n; i++) {
            trace += y[i][i];
        }
        if (trace == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMaximalTriangleFree(GraphTO<Integer, Integer> graph) {
        if (graph == null || graph.getVertexCount() == 0) {
            return false;
        }
        return isTriangleFree(graph) && isPossibleAddArest(graph);
    }

    protected boolean isPossibleAddArest(GraphTO<Integer, Integer> g) {
        if (g == null || g.getVertexCount() == 0) {
            return false;
        }
        boolean ret = true;
        GraphTO graph = g.clone();
        int n = graph.getVertexCount();
        for (int i = 0; i < n && ret; i++) {
            for (int j = 0; j < i && ret; j++) {
                if (i != j && !graph.isNeighbor(i, j)) {
                    Object edge = graph.addEdge(i, j);
                    boolean triangleFree = isTriangleFree(graph);
                    ret = ret && !triangleFree;
                    graph.removeEdge(edge);
                }
            }
        }
        return ret;
    }
}
