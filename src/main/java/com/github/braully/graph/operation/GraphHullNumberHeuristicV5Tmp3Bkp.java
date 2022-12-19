package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.generator.GraphGeneratorRandomGilbert;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_NUMBER;
import static com.github.braully.graph.operation.GraphHullNumber.PARAM_NAME_HULL_SET;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;
import static tmp.DensityHeuristicCompare.INI_V;
import static tmp.DensityHeuristicCompare.MAX_V;
import util.UtilProccess;

public class GraphHullNumberHeuristicV5Tmp3Bkp
        extends GraphHullNumberHeuristicV1 implements IGraphOperation {

    public int K = 2;

    private static final Logger log = Logger.getLogger(GraphHullNumberHeuristicV5Tmp3.class);

    static final String description = "Hull Number Heuristic V5-tmp3";
    int etapaVerbose = -1;

    @Override
    public String getName() {
        return description;
    }

    public GraphHullNumberHeuristicV5Tmp3Bkp() {
    }

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer hullNumber = -1;
        Set<Integer> minHullSet = null;

        try {
            minHullSet = findMinHullSetGraph(graph);
            if (minHullSet != null && !minHullSet.isEmpty()) {
                hullNumber = minHullSet.size();
            }
        } catch (Exception ex) {
            log.error(null, ex);
        }

        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        response.put(PARAM_NAME_HULL_NUMBER, hullNumber);
        response.put(PARAM_NAME_HULL_SET, minHullSet);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, hullNumber);
        return response;
    }

    public Set<Integer> findMinHullSetGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        return buildOptimizedHullSet(graph);

    }

    protected Set<Integer> buildOptimizedHullSetFromStartVertice(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> sini, int[] auxini, int sizeHsini) {
//        Set<Integer> s = new HashSet<>(sini);
        Set<Integer> s = new LinkedHashSet<>(sini);

        int vertexCount = graph.getVertexCount();
        int[] aux = auxini.clone();
        int sizeHs = addVertToS(v, s, graph, aux) + sizeHsini;
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        bdl.labelDistances(graph, s);

        int bestVertice;
        boolean esgotado = false;
        List<Integer> melhores = new ArrayList<Integer>();
        while (sizeHs < vertexCount) {
            bestVertice = -1;
            int maiorGrau = 0;
            int maiorGrauContaminacao = 0;
            int maiorDeltaHs = 0;
            int maiorContaminadoParcialmente = 0;
            int maiorProfundidade = 0;
            int maiorAux = 0;
            melhores.clear();
            if (etapaVerbose == s.size()) {
                System.out.println("- Verbose etapa: " + etapaVerbose);
                System.out.println("Size:");
                System.out.println(" * s: " + s.size());
                System.out.println(" * hs: " + sizeHs);
                System.out.println(" * n: " + vertexCount);
                System.out.printf("- vert: del conta pconta prof aux grau\n");
            }
            for (int i = 0; i < vertexCount; i++) {
                //Se vertice já foi adicionado, ignorar
                if (aux[i] >= K) {
                    continue;
                }
                int[] auxb = aux.clone();
                int deltaHsi = addVertToS(i, null, graph, auxb);

                int grauContaminacao = 0;
                int contaminadoParcialmente = 0;
                //Contabilizar quantos vertices foram adicionados
                for (int j = 0; j < vertexCount; j++) {
                    if (auxb[j] >= K) {
                        grauContaminacao++;
                    }
                    if (auxb[j] > 0 && auxb[j] < K) {
                        contaminadoParcialmente++;
                    }
                }

                int di = graph.degree(i);
                int deltadei = di - aux[i];
                int prof = bdl.getDistance(graph, i);
                if (etapaVerbose == s.size()) {
                    System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
                            i, deltaHsi, grauContaminacao,
                            contaminadoParcialmente, prof, aux[i], di);
                }
//                System.out.printf("- vert: del conta pconta prof aux grau");
//                System.out.printf(" %d: ");
                if (bestVertice == -1) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrauContaminacao = grauContaminacao;
                    maiorContaminadoParcialmente = contaminadoParcialmente;
                    bestVertice = i;
                    maiorProfundidade = prof;
//                    maiorProfundidade = bdl.getDistance(graph, i);
                    maiorAux = aux[i];
                    maiorGrau = di;
                    if (etapaVerbose == s.size()) {
                        System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
                                i, deltaHsi, grauContaminacao,
                                contaminadoParcialmente, prof, aux[i], di);
//                        System.out.print("  * ");
                        for (int j = 0; j < vertexCount; j++) {
                            if (j != i) {
                                if (aux[j] < K && auxb[j] >= K) {
                                    System.out.print(" +" + j);
                                } else if (aux[j] == 0 && auxb[j] > 0) {
                                    System.out.print(" +/-" + j);
                                }
                            }
                        }
                        System.out.println();

                    }
                } else {
                    Boolean greater = isGreater(
                            deltaHsi, maiorDeltaHs,
                            grauContaminacao, maiorGrauContaminacao,
                            prof, maiorProfundidade,
                            contaminadoParcialmente, maiorContaminadoParcialmente,
                            -aux[i], -maiorAux
                    );
                    if (greater == null) {
                        melhores.add(i);
                        if (etapaVerbose == s.size()) {
                            System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
                                    i, deltaHsi, grauContaminacao,
                                    contaminadoParcialmente, prof, aux[i], di);

                            System.out.print("  * ");
                            for (int j = 0; j < vertexCount; j++) {
                                if (j != i) {
                                    if (aux[j] < K && auxb[j] >= K) {
                                        System.out.print(" +" + j);
                                    } else if (aux[j] == 0 && auxb[j] > 0) {
                                        System.out.print(" +/-" + j);
                                    }
                                }
                            }
                            System.out.println();
                        }
                    } else if (greater) {
                        melhores.clear();
                        maiorDeltaHs = deltaHsi;
                        maiorGrauContaminacao = grauContaminacao;
                        maiorContaminadoParcialmente = contaminadoParcialmente;
                        bestVertice = i;
                        maiorProfundidade = prof;
                        maiorGrau = di;
//                        System.out.printf("- vert: del conta pconta prof aux grau");
                        if (etapaVerbose == s.size()) {
                            System.out.printf(" * %3d: %3d %3d %3d %3d %3d %3d \n",
                                    i, deltaHsi, grauContaminacao,
                                    contaminadoParcialmente, prof, aux[i], di);

                            System.out.print("  * ");
                            for (int j = 0; j < vertexCount; j++) {
                                if (j != i) {
                                    if (aux[j] < K && auxb[j] >= K) {
                                        System.out.print(" +" + j);
                                    } else if (aux[j] == 0 && auxb[j] > 0) {
                                        System.out.print(" +/-" + j);
                                    }
                                }
                            }
                            System.out.println();
                        }
                    }
                }
            }
            if (etapaVerbose == s.size()) {
                System.out.println(" - " + bestVertice);
                System.out.println(" - " + melhores);
            }
            sizeHs = sizeHs + addVertToS(bestVertice, s, graph, aux);
            bdl.labelDistances(graph, s);
        }

        s = tryMinimal(graph, s);
        return s;
    }

    protected boolean isGreaterSimple(int... compare) {
        boolean ret = false;
        int i = 0;
        while (i < compare.length - 2
                && compare[i] == compare[i + 1]) {
            i += 2;
        }
        if (i <= compare.length - 2 && compare[i] > compare[i + 1]) {
            ret = true;
        }
        return ret;
    }

    public Boolean isGreater(int... compare) {
        Boolean ret = false;
        int i = 0;
        while (i < compare.length - 2 && compare[i] == compare[i + 1]) {
            i += 2;
        }
        if (i <= compare.length - 2) {
            if (compare[i] > compare[i + 1]) {
                ret = true;
            } else if (compare[i] == compare[i + 1]) {
                ret = null;
            }
        }
        return ret;
    }

    public int addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        int countIncluded = 0;
        if (verti == null || aux[verti] >= K) {
            return countIncluded;
        }

        aux[verti] = aux[verti] + K;
        if (s != null) {
            s.add(verti);
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if (vertn.equals(verti)) {
                    continue;
                }
                if (!vertn.equals(verti) && (++aux[vertn]) == K) {
                    mustBeIncluded.add(vertn);
                }
            }
            countIncluded++;
        }
        return countIncluded;
    }

//    @Override
    public Set<Integer> buildOptimizedHullSet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        List<Integer> vertices = new ArrayList<>((List<Integer>) graphRead.getVertices());
        Set<Integer> hullSet = null;
        Integer vl = null;
        Set<Integer> sini = new LinkedHashSet<>();

        int vertexCount = graphRead.getVertexCount();
        int[] auxini = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            auxini[i] = 0;
        }
        int sizeHs = 0;
        for (Integer v : vertices) {
            if (graphRead.degree(v) <= K - 1) {
                sizeHs = sizeHs + addVertToS(v, sini, graphRead, auxini);
            }
        }
        vertices.sort(Comparator
                .comparingInt((Integer v) -> -graphRead.degree(v))
                .thenComparing(v -> -v));

        int total = graphRead.getVertexCount();
        int cont = 0;
//        int ret = total & cont;
        Integer vi = vertices.get(0);
        List<Integer> verticeStart = new ArrayList<>();
        Integer bestN = null;
        for (Integer v : graphRead.getNeighborsUnprotected(vi)) {
            if (bestN == null) {
                bestN = v;
            } else if (graphRead.degree(v) > graphRead.degree(bestN)
                    || (graphRead.degree(bestN) == graphRead.degree(v)
                    && bestN > v)) {
                bestN = v;
            }
        }
        verticeStart.add(vi);
//        verticeStart.add(bestN);

        for (Integer v : verticeStart) {
            if (sini.contains(v)) {
                continue;
            }
            if (verbose) {
//                System.out.println("Trying ini vert: " + v);
//                UtilProccess.printCurrentItme();

            }
            Set<Integer> tmp = buildOptimizedHullSetFromStartVertice(graphRead, v, sini, auxini, sizeHs);
            tmp = tryMinimal(graphRead, tmp);
            if (hullSet == null) {
                hullSet = tmp;
                vl = v;
            } else if (tmp.size() < hullSet.size()) {
                if (verbose) {
                    System.out.println("Melhorado em: " + (hullSet.size() - tmp.size()));
                    System.out.println(" em i " + v + " vindo de " + vl);
                    System.out.println("d(" + v + ")=" + graphRead.degree(v) + " d(" + vl + ")=" + graphRead.degree(vl));
                    System.out.println(hullSet);
                    System.out.println(tmp);
                }
                hullSet = tmp;
            }
            cont++;
            if (verbose) {
//                UtilProccess.printCurrentItmeAndEstimated(total - cont);
//                System.out.println(" s size: " + tmp.size());
            }
        }
        if (hullSet == null) {
            hullSet = sini;

        }
        return hullSet;
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead, Set<Integer> tmp) {
        Set<Integer> s = tmp;
//        System.out.println("tentando reduzir");

        for (Integer v : tmp) {
            if (graphRead.degree(v) < K) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);
            if (checkIfHullSet(graphRead, t.toArray(new Integer[0]))) {
                s = t;
                if (verbose) {
                    System.out.println("Reduzido removido: " + v);
                }
            }
        }
        return s;
    }

    public void printPesoAux(int[] auxb) {
        int peso = 0;
        for (int i = 0; i < auxb.length; i++) {
            peso = peso + auxb[i];
        }
        System.out.print("{" + peso + "}");
        UtilProccess.printArray(auxb);
    }

    public static void main(String... args) throws IOException {

        GraphHullNumberHeuristicV1 opref = new GraphHullNumberHeuristicV1();
        opref.setVerbose(false);
        GraphHullNumberHeuristicV5Tmp3 op = new GraphHullNumberHeuristicV5Tmp3();

        System.out.println("Teste greater: ");
        Boolean greater = op.isGreater(1, 1, 2, 2, 3, 3);
        if (greater != null) {
            throw new IllegalStateException("fail on greater: " + greater);
        }

        greater = op.isGreater(1, 1, 2, 2, 2, 3);
        if (greater) {
            throw new IllegalStateException("fail on greater: " + greater);
        }

        greater = op.isGreaterSimple(1, 1, 2, 2, 3, 3);
        if (greater) {
            throw new IllegalStateException("fail on greater");
        }

//        op.etapaVerbose = 3;
        UndirectedSparseGraphTO<Integer, Integer> graph = null;
//        graph = new UndirectedSparseGraphTO("0-1,0-3,1-2,3-4,3-5,4-5,");
//        graph = UtilGraph.loadGraphG6("S??OOc_OAP?G@_?KQ?C????[?EPWgF??W");

        op.K = 3;
        graph = UtilGraph.loadBigDataset(new FileInputStream("/home/strike/Workspace/tss/TSSGenetico/Instancias/ca-GrQc/ca-GrQc.txt"));

//        System.out.println(graph);
        Set<Integer> buildOptimizedHullSet = op.buildOptimizedHullSet(graph);
        System.out.println("S[" + buildOptimizedHullSet.size() + "]: " + buildOptimizedHullSet);

        Set<Integer> findMinHullSetGraph = opref.findMinHullSetGraph(graph);
        System.out.println("REF-S[" + findMinHullSetGraph.size() + "]: " + findMinHullSetGraph);

        Set<Integer> findHullSubSetBruteForce = op.findHullSubSetBruteForce(graph, findMinHullSetGraph.size(), 19, 14);
        System.out.println("Try search[" + findHullSubSetBruteForce.size() + "]: " + findHullSubSetBruteForce);

        if (true) {
            return;
        }
        GraphGeneratorRandomGilbert generator = new GraphGeneratorRandomGilbert();

        op.setVerbose(true);

        System.out.print("Dens:\t");
        for (double density = 0.1; density <= 0.9; density += 0.1) {
            System.out.printf("%.1f \t", density);

        }

        for (int nv = INI_V; nv <= MAX_V; nv++) {

            System.out.printf("%3d: \t", nv);

            for (double density = 0.1; density <= 0.9; density += 0.1) {
                graph = generator.generate(nv, density);
                findMinHullSetGraph = op.findMinHullSetGraph(graph);
                System.out.printf("%d", findMinHullSetGraph.size());

                System.out.printf("\t");

            }
            System.out.println();
        }
    }
}
