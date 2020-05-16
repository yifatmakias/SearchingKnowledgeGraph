package com;

import com.configuration.ValidationFile;
import com.graph.query.*;
import com.graph.util.Util.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/2.
 */
public class Main {
    final static String validationFile = ValidationFile.SEARCH_MAP.getName();
    public static void main(String[] args) throws IOException, InterruptedException{

        final int K = 2;
        HelperFunctionsClass helpClass = new HelperFunctionsClass();

        RDFGraph graph = helpClass.createGraph("result\\RDF\\Search_entity.txt",
                "result\\RDF\\Search_edge.txt");
        GraphQuery queryGraph = new GraphQuery("result\\Queries\\query_entity.txt",
                "result/Queries/query_edge.txt");
        String graphDomain = "Search";

        queryGraph.getGraphQuery().printPathsForAllLeaves();
        ArrayList<ArrayList<String>> queryPaths = queryGraph.getGraphQuery().getPathsForAllLeaves();

        String simFileEdge = "result\\SimilarityFiles\\sim_edge.txt";
        String simFileNode = "result\\SimilarityFiles\\sim_graph_and_query.txt";

        String firstQueryNode = queryGraph.getEntities().get(0).getName();
        ReadSimilarityTxtFile read_first_node_sim_file = new ReadSimilarityTxtFile(simFileNode, firstQueryNode);
        Map<String, Double> map_first_sim_node = read_first_node_sim_file.getMap();
        Map<String, Double> KSimilarGraphNodes = helpClass.getSimilarKGraphNodes(map_first_sim_node, K);
        long startTime = System.currentTimeMillis();
        Process p = Runtime.getRuntime().exec("C:\\Users\\yifat\\PycharmProjects\\SearchingMEMap\\venv\\Scripts\\python.exe C:\\Users\\yifat\\PycharmProjects\\SearchingMEMap\\node_similarity.py result\\RDF\\Search_entity.txt result\\SimilarityFiles\\sim_graph_and_query.txt result\\Queries\\query_entity.txt");
        p.waitFor();
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Similarity file creation Time: " + totalTime);

        startTime = System.currentTimeMillis();
        GreedyQuery greedyQuery = new GreedyQuery(graph, queryGraph, simFileEdge, simFileNode, graphDomain);
        for (Map.Entry<String, Double> entry : KSimilarGraphNodes.entrySet()) {
            for (ArrayList<String> queryPath: queryPaths) {
                greedyQuery.recursiveRun(0, entry.getKey(), K, queryPath);
            }
        }
        endTime   = System.currentTimeMillis();
        totalTime = endTime - startTime;
        greedyQuery.printPathResults();
        System.out.println("Query response Time: " + totalTime);

/*      ReadSimilarityTxtFile read_edge_sim_file = new ReadSimilarityTxtFile(simFileEdge, "Achieved_By");
        Map<String, Double> map_sim_edge = read_edge_sim_file.getMap();
        ReadSimilarityTxtFile read_node_sim_file = new ReadSimilarityTxtFile(simFileNode, "Find");
        Map<String, Double> map_sim_node = read_node_sim_file.getMap();

        String simGraphNode = getSimilarGrphNode("Find", simFileNode);
        String graphNodeId = "";
        Map<Entity, Integer> entitiesMap= graph.getVexIndex();
        for (Map.Entry<Entity, Integer> entry : entitiesMap.entrySet()) {
            if (entry.getKey().getName().equals(simGraphNode)) {
                graphNodeId = entry.getKey().getId();
            }
        }

        // QueryThreadInfo queryThreadInfo2 = new QueryThreadInfo("1", "Achieved_By", map_sim_edge);
        QueryThreadInfo queryThreadInfo2 = new QueryThreadInfo(graphNodeId, "Achieved_By", map_sim_edge, map_sim_node);
        List<QueryThreadInfo> queryThreadInfos = new LinkedList<>();
        queryThreadInfos.add(queryThreadInfo2);

        aStartTest(graph, queryThreadInfos);*/


    }

    public static void aStartTest(RDFGraph graph,List<QueryThreadInfo> queryThreadInfos) throws IOException{

        // int[] tops = new int[]{20,40,80,100,200,300,400,500,600,700,800};
        // int[] tops = new int[]{2, 4, 6, 9};

        // for (int j = 0; j < tops.length; j++) {
            System.out.println("----------------------------------");
            AStarQueryNew aStarQueryNew = new AStarQueryNew(graph, queryThreadInfos, "Search", 100, 4);
            aStarQueryNew.run();
            //AStarQueryNew.evaluation(aStarQueryNew, getCountryCarFromFile(validationFile));
        //}
    }


}
