package com;

import com.graph.query.*;
import org.jgrapht.graph.DefaultListenableGraph;

import javax.swing.*;
import java.io.*;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        final int K = 2;
        HelperFunctionsClass helpClass = new HelperFunctionsClass();

        RDFGraph graph = helpClass.createGraph("result\\RDF\\Search_entity.txt",
                "result\\RDF\\Search_edge.txt");
        GraphQuery queryGraph = new GraphQuery("result\\Queries\\query_entity.txt",
                "result/Queries/query_edge.txt");
        String graphDomain = "Search";
        final Boolean isMultiPathQuery = true;

        queryGraph.getGraphQuery().printPathsForAllLeaves();
        ArrayList<ArrayList<String>> queryPaths = queryGraph.getGraphQuery().getPathsForAllLeaves();

        String simFileEdge = "result\\SimilarityFiles\\sim_edge.txt";
        String simFileNode = "result\\SimilarityFiles\\sim_graph_and_query.txt";

        String firstQueryNode = queryGraph.getEntities().get(0).getName();
        ReadSimilarityTxtFile read_first_node_sim_file = new ReadSimilarityTxtFile(simFileNode, firstQueryNode);
        Map<String, Double> map_first_sim_node = read_first_node_sim_file.getMap();
        Map<String, Double> KSimilarGraphNodes = helpClass.getSimilarKGraphNodes(map_first_sim_node, K);
        long startTime = System.currentTimeMillis();
        // Process p = Runtime.getRuntime().exec("C:\\Users\\yifat\\PycharmProjects\\SearchingMEMap\\venv\\Scripts\\python.exe C:\\Users\\yifat\\PycharmProjects\\SearchingMEMap\\node_similarity.py result\\RDF\\Search_entity.txt result\\SimilarityFiles\\sim_graph_and_query.txt result\\Queries\\query_entity.txt");
        // p.waitFor();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Similarity file creation Time: " + totalTime);

        startTime = System.currentTimeMillis();
        GreedyQuery greedyQuery = new GreedyQuery(graph, queryGraph, simFileEdge, simFileNode, graphDomain);
        for (Map.Entry<String, Double> entry : KSimilarGraphNodes.entrySet()) {
            for (ArrayList<String> queryPath : queryPaths) {
                greedyQuery.recursiveRun(0, entry.getKey(), K, queryPath);
            }
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        if (!isMultiPathQuery) {
            greedyQuery.printPathResults();
        }
        else {
            for (Map.Entry<DefaultListenableGraph, Double> entry : greedyQuery.getGraphResultsMap().entrySet()) {
                JGraphXAdapter applet = new JGraphXAdapter(entry.getKey());
                applet.init();

                JFrame frame = new JFrame();
                frame.getContentPane().add(applet);
                frame.setTitle("Query result ranking: " + entry.getValue());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        }
        System.out.println("Query response Time: " + totalTime);
    }
}
