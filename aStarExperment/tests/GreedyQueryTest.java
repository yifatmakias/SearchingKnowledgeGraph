import com.HelperFunctionsClass;
import com.graph.query.GraphQuery;
import com.graph.query.GreedyQuery;
import com.graph.query.RDFGraph;
import org.junit.*;

public class GreedyQueryTest {

    private GreedyQuery gq;
    private HelperFunctionsClass helperClass;


    @Before
    public void setUp() throws Exception {
        RDFGraph graph = helperClass.createGraph("result\\RDF\\search_entity.txt",
                "result\\RDF\\search_edge.txt");
        GraphQuery queryGraph = new GraphQuery("result\\GraphQueryFiles\\query_entity.txt",
                "result/GraphQueryFiles/query_edge.txt");

        String simFileEdge = "result\\SimilarityFiles\\sim_edge.txt";
        String simFileNode = "result\\SimilarityFiles\\sim_graph_and_query.txt";
        gq = new GreedyQuery(graph, queryGraph, simFileEdge, simFileNode);
    }
}
