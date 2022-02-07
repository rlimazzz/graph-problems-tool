package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;
import util.BFSUtil;
import util.MapCountOpt;

public class GraphCycleChordlessDetec implements IGraphOperation {

    static final String type = "General";
    static final String description = "Cycle chordless detect";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        Integer size = null;

        try {
            String inputData = graph.getInputData();
            size = Integer.parseInt(inputData);
        } catch (Exception e) {

        }
        if (size == null) {
            size = 6;
//            throw new IllegalArgumentException("Input invalid (not integer): " + graph.getInputData());
        }

        Collection cycle = null;
        if (size >= 2) {
            cycle = findCycleBruteForce(graph, size);
        }
        if (cycle != null) {
            response.put("Cycle find ", cycle);
        } else {
            response.put("Cycle not find ", 0);
        }
        return response;
    }

    public List<Integer> findCycleBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSize) {
        Collection vertices = graph.getVertices();
        List<Integer> cycle = null;
        int veticesCount = vertices.size();
        MapCountOpt mcount = new MapCountOpt(veticesCount);
        BFSUtil bfsUtil = BFSUtil.newBfsUtilCompactMatrix(veticesCount);
        bfsUtil.labelDistancesCompactMatrix(graph);

        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSize);
        Boolean isCycle = null;
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            mcount.clear();
            isCycle = null;
            for (int iv : currentSet) {
                Integer v = graph.verticeByIndex(iv);
                for (int iw : currentSet) {
                    Integer w = graph.verticeByIndex(iw);
                    if (bfsUtil.get(v, w) == 1) {
                        Integer inc = mcount.inc(v);
                        if (inc > 2) {
                            //V tem mais de dois vizinhos no ciclo, 
                            // não é permitido em um ciclo chordless
                            isCycle = false;
                            break;
                        }
                    }
                }
            }
            if (isCycle == null) {
                isCycle = true;
                for (int iv : currentSet) {
                    Integer v = graph.verticeByIndex(iv);
                    isCycle = isCycle && mcount.getCount(v) == 2;
                }
            }
            if (isCycle) {
                cycle = new ArrayList<>();
                for (int i : currentSet) {
                    cycle.add(i);
                }
                break;
            }
        }
        return cycle;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    //https://www.geeksforgeeks.org/detect-cycle-in-an-undirected-graph-using-bfs/
    public boolean isChordlessCycle(UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        boolean ret = false;

        return ret;
    }
}
