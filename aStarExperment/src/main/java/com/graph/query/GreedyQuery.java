package com.graph.query;

import javafx.util.Pair;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GreedyQuery {

    private RDFGraph graph;
    private GraphQuery graphQuery;
    private String simFileEdge;
    private String simFileNode;
    private List<List<String>> pathResults;
    private final double SIM_THRESHOLD = 0.55;

    public GreedyQuery(RDFGraph graph, GraphQuery graphQuery, String simFileEdge, String simFileNode) {
        this.graph = graph;
        this.graphQuery = graphQuery;
        this.simFileEdge = simFileEdge;
        this.simFileNode = simFileNode;
        this.pathResults = new ArrayList<>();
    }


    public void recursiveRun(int index, String graphNode, int topK, ArrayList<String> queryPath) {
        if (index >= queryPath.size() - 2)
            return;
        String firstQueryNode = queryPath.get(index).split(":")[1];
        String edgeName = queryPath.get(index+1);
        String secondQueryNode = queryPath.get(index+2).split(":")[1];

//        if (index == graphQuery.getEdgesInfo().size())
//            return;
//        String firstQueryNode = graphQuery.getEntities().get(index).getName();
//        int firstQueryNodeId = graphQuery.getGraphQuery().getVex(graphQuery.getEntities().get(index));
//        String secondQueryNode = graphQuery.getEntities().get(index+1).getName();
//        int secondQueryNodeId = graphQuery.getGraphQuery().getVex(graphQuery.getEntities().get(index+1));
//        String edgeName = "";
//        List<String> edgeList = graphQuery.getGraphQuery().getEdgeInfo(firstQueryNodeId, secondQueryNodeId);
//        if (edgeList == null)
//            return;
//        else
//            edgeName = edgeList.get(0);

        ReadSimilarityTxtFile read_edge_sim_file = new ReadSimilarityTxtFile(simFileEdge, edgeName);
        Map<String, Double> map_sim_edge = read_edge_sim_file.getMap();
        ReadSimilarityTxtFile read_first_node_sim_file = new ReadSimilarityTxtFile(simFileNode, firstQueryNode);
        Map<String, Double> map_first_sim_node = read_first_node_sim_file.getMap();
        ReadSimilarityTxtFile read_second_node_sim_file = new ReadSimilarityTxtFile(simFileNode, secondQueryNode);
        Map<String, Double> map_second_sim_node = read_second_node_sim_file.getMap();

        // Find the graph index of the most similar graph node.
        String graphNodeId = "";
        Map<Entity, Integer> entitiesMap= graph.getVexIndex();
        for (Map.Entry<Entity, Integer> entry : entitiesMap.entrySet()) {
            if (entry.getKey().getName().equals(graphNode)) {
                graphNodeId = entry.getKey().getId();
            }
        }

        QueryThreadInfo queryThreadInfo = new QueryThreadInfo(graphNodeId, edgeName, map_sim_edge, map_first_sim_node);
        List<QueryThreadInfo> queryThreadInfos = new LinkedList<>();
        queryThreadInfos.add(queryThreadInfo);
        try {
            AStarQueryNew aStarQueryNew = new AStarQueryNew(graph, queryThreadInfos, "Search", 100, 4);
            aStarQueryNew.run();
            AStarQueryNew.PriorityNode [][] results = aStarQueryNew.taskResults;
            List<Pair<Double,String>> graphResults = new ArrayList<>();
            for (int i = 0; i < results.length ; i++) {
                for (int j = 0; j < results[i].length ; j++) {
                    if (results[i][j] != null) {
                        for (int pathNode : results[i][j].getPath().getNodes()) {
                            graphResults.add(new Pair<>(results[i][j].getG(),graph.getNodeData(pathNode).getName()));
                        }
                    }
                }
            }
            Map<String, Double> topKMap = getTopKSimilarNodes(map_second_sim_node, graphResults, topK);
            for (int i = 0; i < results.length ; i++) {
                for (int j = 0; j < results[i].length ; j++) {
                    if (results[i][j] != null) {
                        for (int pathNode : results[i][j].getPath().getNodes()) {
                            if (topKMap.keySet().contains(graph.getNodeData(pathNode).getName())) {
                                double rank = topKMap.get(graph.getNodeData(pathNode).getName());
                                String startNodeName = graph.getNodeData(results[i][j].getPath().getStart()).getName();
                                List<String> predicates = results[i][j].getPath().getPredicates();
                                String endNodeName = graph.getNodeData(pathNode).getName();
                                List<String> subPath = null;
                                for (List<String> path: pathResults) {
                                    // continue an existing path
                                    if (path.get(path.size() - 1).equals(startNodeName)) {
                                        subPath = path;
                                        subPath.add(Double.toString(rank));
                                        subPath.addAll(predicates);
                                        subPath.add(endNodeName);
                                        break;
                                    }
                                    // make duplicate path of an existing one and continue it again.
                                    // when the start of the path is like the start of another path.
                                    if (path.contains(startNodeName)) {
                                        subPath = new ArrayList<>();
                                        subPath.addAll(path.subList(0, path.indexOf(startNodeName) + 1));
                                        subPath.add(Double.toString(rank));
                                        subPath.addAll(predicates);
                                        subPath.add(endNodeName);
                                        pathResults.add(subPath);
                                        break;
                                    }
                                }
                                // no path to continue or duplicate so create a new path
                                if (subPath == null){
                                    subPath = new ArrayList<>();
                                    subPath.add(Double.toString(map_first_sim_node.get(startNodeName)));
                                    subPath.add(startNodeName);
                                    subPath.add(Double.toString(rank));
                                    subPath.addAll(predicates);
                                    subPath.add(endNodeName);
                                    pathResults.add(subPath);
                                }
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, Double> entry : topKMap.entrySet()) {
                if (entry.getValue() > SIM_THRESHOLD)
                    recursiveRun(index+2, entry.getKey(), topK, queryPath);
                else
                    recursiveRun(index, entry.getKey(), topK, queryPath);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printPathResults() {
        Map<Double, List<String>> pathMapResults = getMapPathResults();
        SortedSet<Double> ranks = new TreeSet<>(pathMapResults.keySet());
        DecimalFormat df = new DecimalFormat("#.###");
        int resultCounter = ranks.size();
        for (Double rank : ranks) {
            System.out.println(String.format("***Result Path %s***", resultCounter));
            System.out.print("Path score: " + df.format(rank) + "\nPath: ");
            List<String> path = pathMapResults.get(rank);
            for (int i = 0; i < path.size() - 1; i++) {
                System.out.print(path.get(i) + "->");
            }
            System.out.println(path.get(path.size() -1));
            resultCounter--;
        }
    }

    public List<List<String>> getPathResults() {
        return pathResults;
    }

    private Map<Double, List<String>> getMapPathResults() {
        Map<Double, List<String>> mapResults = new HashMap<>();
        for (List<String> path: pathResults) {
            double finalRank = 0;
            Iterator itr = path.iterator();
            while (itr.hasNext()) {
                String pathItem = (String)itr.next();
                if (isDouble(pathItem)) {
                    finalRank = finalRank + Double.parseDouble(pathItem);
                    itr.remove();
                }
            }
            int normalizationFactor = path.size()/2 + 1;
            finalRank = finalRank / normalizationFactor ;
            mapResults.put(finalRank, path);
        }
        return mapResults;
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Map<String,Double> getTopKSimilarNodes(Map<String, Double> map_sim_node, List<Pair<Double,String>> graphResults, int k) {
        Map<String, Double> rankedGraphResults = new HashMap<>();
        for (Pair<Double,String> graphNode: graphResults) {
            double rank = (map_sim_node.get(graphNode.getValue()) + graphNode.getKey())/2;
            rankedGraphResults.put(graphNode.getValue(), rank);
        }
        Map<String,Double> topk =
                rankedGraphResults.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(k)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return topk;
    }
}
