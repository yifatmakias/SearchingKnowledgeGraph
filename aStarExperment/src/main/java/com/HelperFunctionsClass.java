package com;

import com.graph.query.Entity;
import com.graph.query.RDFGraph;
import com.graph.query.ReadSimilarityTxtFile;
import com.graph.util.Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class HelperFunctionsClass {


    public RDFGraph createGraph(String entityFile, String edgeFile) throws IOException {
        List<Entity> entities = new ArrayList<>();
        List<Entity[]> edges = new ArrayList<>();
        List<List<String>> edgesInfo = new ArrayList<>();
        readEntity(entityFile, entities);
        readEdges(edgeFile, edges, edgesInfo);
        System.out.println("read is over");
        RDFGraph graph = new RDFGraph(true);
        graph.createGraph(entities, edges, edgesInfo);
        // graph.show();
        return graph;
    }

    /**
     * @param edgeFile
     * @param edges
     * @param edgesInfo
     * @throws IOException
     */
    public void readEdges(String edgeFile, List<Entity[]> edges, List<List<String>> edgesInfo) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(edgeFile), "utf-8"));
        Map<String, List<String>> map = new HashMap<>();
        String s;
        int i = 0;
        while ((s = reader.readLine()) != null){
            String[] infos = s.split(" ");
            //String[] infos = s.split("\t");
            if(!infos[3].equals("type")){
                String entityInfo = infos[0] + " " + infos[1] + " " + infos[2] + " " + infos[4];
                if(map.containsKey(entityInfo)){
                    map.get(entityInfo).add(infos[3]);
                }else {
                    map.put(entityInfo, new ArrayList<String>(Arrays.asList(infos[3])));
                }
            }
        }
        for(Map.Entry<String, List<String>> entry: map.entrySet()){
            String[] infos = entry.getKey().split(" ");
            //String[] infos = entry.getKey().split("\t");
            Entity e1 = new Entity(infos[0], infos[2], null);
            Entity e2 = new Entity(infos[1], infos[3], null);
            edges.add(new Entity[]{e1, e2});
            edgesInfo.add(entry.getValue());
        }
        reader.close();
    }

    /**
     * @param entityFile
     * @return
     * @throws IOException
     */
    public void readEntity(String entityFile, List<Entity> entities) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(entityFile), "utf-8"));
        String s;
        while ((s = reader.readLine()) != null){
            String[] infos = s.split(" ");
            //String[] infos = s.split("\t");
            entities.add(new Entity(infos[0], infos[1], infos[2]));
        }
        reader.close();
    }

    public Map<String, Double> getSimilarKGraphNodes(Map<String, Double> map_sim_node, int k) {
        return map_sim_node.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(k)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String getSimilarGrphNode(String queryNode, String simFileNode) {
        ReadSimilarityTxtFile read_node_sim_file = new ReadSimilarityTxtFile(simFileNode, queryNode);
        Map<String, Double> map_sim_node = read_node_sim_file.getMap();
        String graphNode = Collections.max(map_sim_node.entrySet(), Map.Entry.comparingByValue()).getKey();
        return graphNode;
    }

    /**
     * @return
     * @throws IOException
     */
    public List<String> getCountryCarFromFile(String path) throws IOException{
        return new ArrayList<>(new HashSet<>(Util.readFileAbsolute(path)));
    }
}
