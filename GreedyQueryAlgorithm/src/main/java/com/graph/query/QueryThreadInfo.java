package com.graph.query;

import java.util.Map;

/**
 * @Author: yhj
 * @Date: Created in 2019/1/10.
 */
public class QueryThreadInfo {
    private String source;
    private String predicate;
    private Map<String, Double> similarMap;
    private Map<String, Double> similarMap2;

    public QueryThreadInfo(String source, String predicate, Map<String, Double> similarMap){
        this.source = source;
        this.predicate = predicate;
        this.similarMap = similarMap;
    }

    public QueryThreadInfo(String source, String predicate, Map<String, Double> similarMap, Map<String, Double> similarMap2) {
        this.source = source;
        this.predicate = predicate;
        this.similarMap = similarMap;
        this.similarMap2 = similarMap2;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public Map<String, Double> getSimilarMap() {
        return similarMap;
    }

    public Map<String, Double> getSimilarMap2() {
        return similarMap2;
    }

    public void setSimilarMap2(Map<String, Double> similarMap2) {
        this.similarMap2 = similarMap2;
    }

    public void setSimilarMap(Map<String, Double> similarMap) {
        this.similarMap = similarMap;
    }
}
