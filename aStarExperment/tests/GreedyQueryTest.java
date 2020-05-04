import com.HelperFunctionsClass;
import com.Main;
import com.graph.query.GraphQuery;
import com.graph.query.GreedyQuery;
import com.graph.query.RDFGraph;
import com.graph.query.ReadSimilarityTxtFile;
import org.junit.*;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Map;

public class GreedyQueryTest {

    private GreedyQuery greedyQuery;
    private HelperFunctionsClass helperClass;
    private RDFGraph graph;
    private GraphQuery queryGraph;
    private String simFileEdge;
    private String simFileNode;
    private ArrayList<ArrayList<String>> queryPaths;
    private String firstQueryNode;
    private ReadSimilarityTxtFile read_first_node_sim_file;
    private Map<String, Double> map_first_sim_node;

    @Before
    public void setUp() throws Exception {
        helperClass = new HelperFunctionsClass();
        graph = helperClass.createGraph("tests\\search_entity.txt",
                "tests\\search_edge.txt");
        queryGraph = new GraphQuery("tests\\query_entity.txt",
                "tests\\query_edge.txt");

        simFileEdge = "tests\\sim_edge.txt";
        simFileNode = "tests\\sim_graph_and_query.txt";
        greedyQuery = new GreedyQuery(graph, queryGraph, simFileEdge, simFileNode);
        queryPaths = queryGraph.getGraphQuery().getPathsForAllLeaves();
        firstQueryNode = queryGraph.getEntities().get(0).getName();
        read_first_node_sim_file = new ReadSimilarityTxtFile(simFileNode, firstQueryNode);
        map_first_sim_node = read_first_node_sim_file.getMap();
    }

    @BeforeEach
    public void setUpBeforeEachTest() {
        queryPaths = queryGraph.getGraphQuery().getPathsForAllLeaves();
        firstQueryNode = queryGraph.getEntities().get(0).getName();
        read_first_node_sim_file = new ReadSimilarityTxtFile(simFileNode, firstQueryNode);
        map_first_sim_node = read_first_node_sim_file.getMap();
    }

    @After
    public void tearDown() {
        greedyQuery = null;
    }

    @Test
    public void recursiveRunK2() {
        Map<String, Double> KSimilarGraphNodes1 = helperClass.getSimilarKGraphNodes(map_first_sim_node, 2);
        for (Map.Entry<String, Double> entry : KSimilarGraphNodes1.entrySet()) {
            for (ArrayList<String> queryPath: queryPaths) {
                greedyQuery.recursiveRun(0, entry.getKey(), 2, queryPath);
            }
        }
        Assert.assertNotNull(greedyQuery.getPathResults());
        Assert.assertEquals(4, greedyQuery.getPathResults().size());
    }

    @Test
    public void recursiveRunK3() {
        Map<String, Double> KSimilarGraphNodes2 = helperClass.getSimilarKGraphNodes(map_first_sim_node, 3);
        for (Map.Entry<String, Double> entry : KSimilarGraphNodes2.entrySet()) {
            for (ArrayList<String> queryPath: queryPaths) {
                greedyQuery.recursiveRun(0, entry.getKey(), 3, queryPath);
            }
        }
        Assert.assertNotNull(greedyQuery.getPathResults());
        Assert.assertEquals(5, greedyQuery.getPathResults().size());
    }
}
