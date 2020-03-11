import com.HelperFunctionsClass;
import com.graph.query.GraphQuery;
import com.graph.query.GreedyQuery;
import com.graph.query.RDFGraph;
import org.junit.*;

public class GreedyQueryTest {

    private GreedyQuery greedyQuery;
    private HelperFunctionsClass helperClass;
    private RDFGraph graph;
    private GraphQuery queryGraph;
    private String simFileEdge;
    private String simFileNode;

    @Before
    public void setUp() throws Exception {
        helperClass = new HelperFunctionsClass();
        graph = helperClass.createGraph("result\\RDF\\search_entity.txt",
                "result\\RDF\\search_edge.txt");
        queryGraph = new GraphQuery("result\\GraphQueryFiles\\query_entity.txt",
                "result/GraphQueryFiles/query_edge.txt");

        simFileEdge = "result\\SimilarityFiles\\sim_edge.txt";
        simFileNode = "result\\SimilarityFiles\\sim_graph_and_query.txt";
        greedyQuery = new GreedyQuery(graph, queryGraph, simFileEdge, simFileNode);
    }

    @After
    public void tearDown() {
        greedyQuery = null;
    }

    @Test
    public void recursiveRun() {
        greedyQuery.recursiveRun(0, null, 2);
        Assert.assertNotNull(greedyQuery.getPathResults());
        Assert.assertEquals(4, greedyQuery.getPathResults().size());

        greedyQuery = new GreedyQuery(graph, queryGraph, simFileEdge, simFileNode);
        greedyQuery.recursiveRun(0, null, 3);
        Assert.assertNotNull(greedyQuery.getPathResults());
        Assert.assertEquals(5, greedyQuery.getPathResults().size());
    }
}
