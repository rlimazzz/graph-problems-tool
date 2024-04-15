/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import static java.lang.Math.abs;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author strike
 */
public abstract class AbstractHeuristic implements IGraphOperation {

    public Integer K;
    public Integer R;
    public Integer majority;
    public Integer P;
    protected int[] degree = null;

    protected static Random randomUtil = new Random();
    protected int[] kr;
    protected boolean verbose;
    static final String type = "Contamination";

    public static final String MINIMAL = "minimal";

    public Map<String, Boolean> parameters = new LinkedHashMap<>();
    public boolean tentarMinamilzar = false;
    public boolean tentarMinamilzar2 = false;

    protected boolean grafoconexo = true;

    public String getTypeProblem() {
        return type;
    }

    public void setK(Integer K) {
        this.K = K;
        this.majority = null;
        this.R = null;
        this.P = null;
    }

    public void setR(Integer R) {
        this.R = R;
        this.K = null;
        this.majority = null;
        this.P = null;
    }

    public void setMarjority(Integer marjority) {
        this.majority = marjority;
        this.K = null;
        this.R = null;
        this.P = null;
    }

    public void setP(Integer p) {
        this.P = p;
        this.majority = null;
        this.K = null;
        this.R = null;
    }

    public void initKr(UndirectedSparseGraphTO graph) {
        int vertexCount = (Integer) graph.maxVertex() + 1;
        kr = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            int degree = graph.degree(i);
            if (R != null) {
                kr[i] = Math.min(R, graph.degree(i));
            } else if (K != null) {
                kr[i] = K;
            } else if (majority != null) {
                double percent = (double) majority * 0.1;
                //                kr[i] = roundUp(degree, majority);
                double ki = Math.ceil(percent * degree);
                int kii = (int) Math.ceil(ki);
                kr[i] = kii;
            } else if (P != null) {
                if (degree > 0) {
//                    int random = random(degree, P);
//                    kr[i] = random;
                    double ki = (P.doubleValue() * 0.1) * (double) degree;
                    int kii = (int) Math.ceil(ki);
                    kr[i] = kii;
                } else {
                    kr[i] = degree;
                }
            }
        }
    }

    public static int random(int num, Integer probability) {
        //Probability ignored, for future use
        return randomUtil.nextInt(num);
    }

    public static int roundUp(int num, int divisor) {
        int sign = (num > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
        return sign * (abs(num) + abs(divisor) - 1) / abs(divisor);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Set<Integer> tryMinimal(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        Set<Integer> s = tmp;
        if (verbose) {
            System.out.println("tentando reduzir: " + s.size());
//            System.out.println("s: " + s);
        }
        int cont = 0;
        for (Integer v : tmp) {

//        LinkedList<Integer> tmp2 = new LinkedList<>(tmp);
//        Iterator<Integer> descendingIterator = tmp2.descendingIterator();
//        while (descendingIterator.hasNext()) {
//            Integer v = descendingIterator.next();
            cont++;
            if (graphRead.degree(v) < kr[v]) {
                continue;
            }
            Set<Integer> t = new LinkedHashSet<>(s);
            t.remove(v);
            if (checkIfHullSet(graphRead, t)) {
                s = t;
//                if (verbose) {
//                System.out.println("Reduzido removido: " + v + " na posição " + cont + "/" + (tmp.size() - 1));
//                }
                if (cont > (tmp.size() / 2) && grafoconexo) {
//                    System.out.println("Poda de v:  " + v + " realizada depois de 50% em grafo conexo " + cont + "/" + (tmp.size() - 1));
//                    System.out.println(" - Detalhes de v: "
//                            + v + " degree: " + graphRead.degree(v) + " scount: "
//                            + scount[v] + " kr:" + kr[v]);
                }
            }
        }
        if (verbose) {
            System.out.println("reduzido para: " + s.size());
//            System.out.println("s: " + s);
        }
        return s;
    }

    public Set<Integer> tryMinimal2(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp) {
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-2: " + s.size());
//            System.out.println("s: " + s);
        }
        Collection<Integer> vertices = graphRead.getVertices();
        int cont = 0;
        for_p:
        for (int h = 0; h < ltmp.size(); h++) {
            Integer x = ltmp.get(h);
            if (graphRead.degree(x) < kr[x] || !s.contains(x)) {
                continue;
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (graphRead.degree(y) < kr[y] || y.equals(x)
                        || !s.contains(y)) {
                    continue;
                }
                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = new int[(Integer) graphRead.maxVertex() + 1];
                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                }

                Queue<Integer> mustBeIncluded = new ArrayDeque<>();
                for (Integer iv : t) {
                    Integer v = iv;
                    mustBeIncluded.add(v);
                    aux[v] = kr[v];
                }
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    contadd++;
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (vertn.equals(verti)) {
                            continue;
                        }
                        if (!vertn.equals(verti) && aux[vertn] <= kr[vertn] - 1) {
                            aux[vertn] = aux[vertn] + 1;
                            if (aux[vertn] == kr[vertn]) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += kr[verti];
                }

                for (Integer z : vertices) {
                    if (aux[z] >= kr[z] || z.equals(x) || z.equals(y)) {
                        continue;
                    }
                    int contz = contadd;
                    int[] auxb = (int[]) aux.clone();
                    mustBeIncluded.add(z);
                    auxb[z] = kr[z];
                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if (vertn.equals(verti)) {
                                continue;
                            }
                            if (!vertn.equals(verti) && auxb[vertn] <= kr[vertn] - 1) {
                                auxb[vertn] = auxb[vertn] + 1;
                                if (auxb[vertn] == kr[vertn]) {
                                    mustBeIncluded.add(vertn);
                                }
                            }
                        }
                        auxb[verti] += kr[verti];
                    }

                    if (contz == vertices.size()) {
                        if (verbose) {
                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
                            if (scount != null) {
                                Collection<Integer> nsX = graphRead.getNeighborsUnprotected(y);
                                Collection<Integer> nsY = graphRead.getNeighborsUnprotected(x);
                                Collection<Integer> nsZ = graphRead.getNeighborsUnprotected(z);
                                System.out.print("Count s: " + x + ": "
                                        + scount[x] + " " + y + ": " + scount[y]
                                        + " adicionado " + z + ": " + scount[z]);
                                if (Collections.disjoint(nsX, nsY)) {
                                    System.out.print(" ... é disjunto");
                                } else {
                                    System.out.print(" ... tem vizinhos comuns");
                                }
                                if (Collections.disjoint(nsZ, nsY) && Collections.disjoint(nsZ, nsX)) {
                                    System.out.println(" ... z é independente");
                                } else {
                                    System.out.println(" ... z intercept x ou y");
                                }
                            }
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                        }
                        if (cont > (tmp.size() / 2) && grafoconexo) {
                            System.out.println("Poda dupla em grafo conexo removido:  " + x + "," + y + " realizada depois de 50% " + cont + "/" + (tmp.size() - 1));
                            System.out.println(" - Detalhes de v: "
                                    + x + " degree: " + graphRead.degree(x) + " scount: "
                                    + scount[x] + " kr:" + kr[x]);
                            System.out.println(" - Detalhes de v: "
                                    + y + " degree: " + graphRead.degree(y) + " scount: "
                                    + scount[y] + " kr:" + kr[y]);
                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
//                        h = h / 2;
                        continue for_p;
                    }
                }

            }
            cont++;

        }
        return s;
    }
    protected int[] scount = null;

    public Set<Integer> tryMinimal2KeepSize(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int sizeKeep) {
        Set<Integer> s = tmp;
        List<Integer> ltmp = new ArrayList<>(tmp);
        if (verbose) {
            System.out.println("tentando reduzir-keep size: " + s.size());
//            System.out.println("s: " + s);
        }
        Collection<Integer> vertices = graphRead.getVertices();
        int cont = -1;
        for_p:
        for (int h = 0; h < ltmp.size(); h++) {
            cont++;
            Integer x = ltmp.get(h);
            if (graphRead.degree(x) < K || !s.contains(x)) {
                continue;
            }
            for (int j = h + 1; j < ltmp.size(); j++) {
                Integer y = ltmp.get(j);
                if (graphRead.degree(y) < K || y.equals(x)
                        || !s.contains(y)) {
                    continue;
                }
                Set<Integer> t = new LinkedHashSet<>(s);
                t.remove(x);
                t.remove(y);

                int contadd = 0;

                int[] aux = new int[(Integer) graphRead.maxVertex() + 1];
                for (int i = 0; i < aux.length; i++) {
                    aux[i] = 0;
                }

                Queue<Integer> mustBeIncluded = new ArrayDeque<>();
                for (Integer iv : t) {
                    Integer v = iv;
                    mustBeIncluded.add(v);
                    aux[v] = K;
                }
                while (!mustBeIncluded.isEmpty()) {
                    Integer verti = mustBeIncluded.remove();
                    contadd++;
                    Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                    for (Integer vertn : neighbors) {
                        if (vertn.equals(verti)) {
                            continue;
                        }
                        if (!vertn.equals(verti) && aux[vertn] <= K - 1) {
                            aux[vertn] = aux[vertn] + 1;
                            if (aux[vertn] == K) {
                                mustBeIncluded.add(vertn);
                            }
                        }
                    }
                    aux[verti] += K;
                }

                for (Integer z : vertices) {
                    if (aux[z] >= K
                            || z.equals(x)
                            || z.equals(y)) {
                        continue;
                    }
                    int contz = contadd;
                    int[] auxb = (int[]) aux.clone();
                    mustBeIncluded.add(z);
                    auxb[z] = K;
                    while (!mustBeIncluded.isEmpty()) {
                        Integer verti = mustBeIncluded.remove();
                        contz++;
                        Collection<Integer> neighbors = graphRead.getNeighborsUnprotected(verti);
                        for (Integer vertn : neighbors) {
                            if (vertn.equals(verti)) {
                                continue;
                            }
                            if (!vertn.equals(verti) && auxb[vertn] <= K - 1) {
                                auxb[vertn] = auxb[vertn] + 1;
                                if (auxb[vertn] == K) {
                                    mustBeIncluded.add(vertn);
                                }
                            }
                        }
                        auxb[verti] += K;
                    }

                    if (contz == sizeKeep) {
                        if (verbose) {
                            System.out.println("Reduzido removido: " + x + " " + y + " adicionado " + z);
                            System.out.println("Na posição " + cont + "/" + (tmp.size() - 1));
                        }
                        t.add(z);
                        s = t;
                        ltmp = new ArrayList<>(s);
//                        h--;
                        h = 0;
                        continue for_p;
                    }
                }

            }

        }
        return s;
    }

    public boolean checkIfHullSet(UndirectedSparseGraphTO<Integer, Integer> graph,
            Iterable<Integer> currentSet) {
        if (currentSet == null) {
            return false;
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();

        Set<Integer> fecho = new HashSet<>();

        int vertexCount = graph.getVertexCount();

        if (kr == null || kr.length < vertexCount) {
            initKr(graph);
        }
//        if (marjority != null) {
//            throw new IllegalStateException("implementar");
//        }

        int[] aux = new int[(Integer) graph.maxVertex() + 1];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            if (kr[i] == 0) {
                mustBeIncluded.add(i);
            }
        }

        for (Integer iv : currentSet) {
            Integer v = iv;
            mustBeIncluded.add(v);
            aux[v] = kr[v];
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);
            for (Integer vertn : neighbors) {
                if ((++aux[vertn]) == kr[vertn]) {
                    mustBeIncluded.add(vertn);
                }
            }
            fecho.add(verti);
            aux[verti] += kr[verti];
        }
        boolean name = fecho.size() == graph.getVertexCount();
        return name;
    }

    public Map<Integer, Set<Integer>> connectedComponents(UndirectedSparseGraphTO<Integer, Integer> graph) {
        int ret = 0;
        Map<Integer, Set<Integer>> map = new TreeMap<>();
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        if (graph != null && graph.getVertexCount() > 0) {
            Collection<Integer> vertices = graph.getVertices();
            TreeSet<Integer> verts = new TreeSet<>(vertices);
            while (!verts.isEmpty()) {
                Integer first = verts.first();
                bdl.labelDistances(graph, first);
                int contn = 1;
                Set<Integer> acc = new LinkedHashSet<>();
                acc.add(first);
                for (Integer v : vertices) {
                    if (bdl.getDistance(graph, v) >= 0) {
                        verts.remove(v);
                        contn++;
                        acc.add(v);
                    }
                }
                ret++;
                map.put(ret, acc);
            }
        }
        return map;
    }

    public void setTryMinimal() {
        tentarMinamilzar = true;
    }

    public void setTryMinimal2() {
        tentarMinamilzar2 = true;
    }

    public void setTryMinimal(boolean v) {
        tentarMinamilzar = v;
    }

    protected boolean tryMiminal() {
        return tentarMinamilzar;
    }

    protected boolean tryMiminal2() {
        return tentarMinamilzar2;
    }

    public Set<Integer> refineResultStep1(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> tmp, int tamanhoAlvo) {
        Set<Integer> s = new LinkedHashSet<>(tmp);

        for (Integer v : tmp) {
            Collection<Integer> nvs = graphRead.getNeighborsUnprotected(v);
            int scont = 0;
            for (Integer nv : nvs) {
                if (s.contains(nv)) {
                    scont++;
                }
            }
            if (scont >= kr[v]) {
                s.remove(v);
            }
        }
        return s;
    }
}