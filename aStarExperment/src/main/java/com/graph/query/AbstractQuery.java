package com.graph.query;

import java.util.*;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/17.
 */
public abstract class AbstractQuery {
    RDFGraph graph;
    List<QueryThreadInfo> queryThreadInfos;
    String type;
    int topN;

    public AbstractQuery(RDFGraph graph, List<QueryThreadInfo> queryThreadInfos, String type, int topN) {
        this.graph = graph;
        this.queryThreadInfos = queryThreadInfos;
        this.type = type;
        this.topN = topN;
    }

    abstract public void run();

    public boolean checkType(String type) {
        return this.type.equals(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }


    /**
     * @param predicate
     * @return
     */
    public static Map<String, Double> findPreicateSimilar(String predicate) {
        return null;
    }


    public static class MultiSourcePredicatesPaths {
        Map<Integer, ArrayList<Object>> sourceMap = new HashMap<Integer, ArrayList<Object>>();

        public Collection<Object> getMultiPredicatesPaths(int index) {
            return sourceMap.get(index);
        }

        public Collection<Integer> getSourceKey() {
            return sourceMap.keySet();
        }

        public void setMultiPredicatesPaths(int index, ArrayList<Object> multiPredicatesPaths) {
            sourceMap.put(index, multiPredicatesPaths);
        }

        public void addPredicatesPaths(int index, List<List<String>> predicatesPaths) {
            if (!sourceMap.containsKey(index)) {
                sourceMap.put(index, new ArrayList<Object>());
            }
            sourceMap.get(index).add(predicatesPaths);
        }
    }

}