package com.graph.query;


import com.HelperFunctionsClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphQuery {

    private String entityFile;
    private String edgeFile;
    private RDFGraph graphQuery;
    private List<Entity> entities;
    private List<Entity[]> edges;
    private List<List<String>> edgesInfo;
    private HelperFunctionsClass helperClass;

    public GraphQuery(String entityFile, String edgeFile) throws IOException {
        this.entityFile = entityFile;
        this.edgeFile = edgeFile;
        this.entities = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.edgesInfo = new ArrayList<>();
        helperClass = new HelperFunctionsClass();
        helperClass.readEntity(entityFile, entities);
        helperClass.readEdges(edgeFile, edges, edgesInfo);
        this.graphQuery = helperClass.createGraph(entityFile, edgeFile);
    }

    public String getEntityFile() {
        return entityFile;
    }

    public String getEdgeFile() {
        return edgeFile;
    }

    public void setEntityFile(String entityFile) {
        this.entityFile = entityFile;
    }

    public void setEdgeFile(String edgeFile) {
        this.edgeFile = edgeFile;
    }

    public RDFGraph getGraphQuery() {
        return graphQuery;
    }

    public void setGraphQuery(RDFGraph graphQuery) {
        this.graphQuery = graphQuery;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public List<Entity[]> getEdges() {
        return edges;
    }

    public List<List<String>> getEdgesInfo() {
        return edgesInfo;
    }
}
