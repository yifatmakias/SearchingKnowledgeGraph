package com.graph.query;

import com.graph.util.QuickSort;
import com.graph.util.TwoTuple;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/17.
 */
public class AStarQueryNew extends AbstractQuery {
//    Logger logger = LoggerFactory.getLogger(AStarQuery.class);

    List<AStarTask> tasks = new ArrayList<>();
    ExecutorService exec = Executors.newCachedThreadPool();
    PriorityNode[][] taskResults;
    CountDownLatch latch;
    List<BoundNode> results;
    List<Map<Integer, Integer>> traversalNodeMaps;
    final int K;
    double limitFactor = 1.2;
    final int limitPath = 50000;
    AtomicInteger maxPath = new AtomicInteger(0);
    boolean isThreadFail = false;


    class Path implements Iterable<TwoTuple<Integer, String>>{
        private int start;
        private Set<Integer> nodes;
        private List<String> predicates;

        public Path(int node){
            this.start = node;
            this.nodes = new LinkedHashSet<>();
            this.predicates = new ArrayList<>();
        }

        public Path(Path path){
            this.start = path.getStart();
            this.nodes = new LinkedHashSet<>(path.getNodes());
            this.predicates = new ArrayList<>(path.getPredicates());
        }

        public int size(){
            return nodes.size();
        }

        public int getStart() {
            return start;
        }

        public Set<Integer> getNodes() {
            return Collections.unmodifiableSet(nodes);
        }

        public List<String> getPredicates() {
            return Collections.unmodifiableList(predicates);
        }

        public void add(int node, String predicate){
            nodes.add(node);
            predicates.add(predicate);
        }

        private class Itr implements Iterator<TwoTuple<Integer, String>>{
            Iterator<Integer> nodesIterator = nodes.iterator();
            Iterator<String> predicaetsIterator = predicates.iterator();

            @Override
            public boolean hasNext() {
                return nodesIterator.hasNext() && predicaetsIterator.hasNext();
            }

            @Override
            public TwoTuple<Integer, String> next() {
                return new TwoTuple<>(nodesIterator.next(), predicaetsIterator.next());
            }
        }

        @Override
        public Iterator<TwoTuple<Integer, String>> iterator() {
            return new Itr();
        }


        public boolean contains(int node){
            return nodes.contains(node);
        }
    }

    /**
     * A*
     */
    class PriorityNode {
        int id;
        double g;
        double h;
        double f;
        // Set<Integer> path;
        // List<String> predicates;
        Path path;

        public PriorityNode(int id, double g, double h) {
            this.id = id;
            this.h = h;
            this.g = g;
            this.f = Math.pow(g * h, 1.0 / AStarQueryNew.this.K);
            this.path = new Path(id);
        }

//        public PriorityNode(int id, double g, double h, int preNodeIndex, String predicate) {
//            this(id, g, h);
//            this.path.add(preNodeIndex, predicate);
//        }

//        public PriorityNode(int id, double g, double h, PriorityNode preNode) {
//            this(id, g, h, preNode.path, preNode.getId());
//        }
//
//        public PriorityNode(int id, double g, double h, Set<Integer> prePath, int preNodeIndex) {
//            this.id = id;
//            this.g = g;
//            this.h = h;
//            if (g == 0.0 || h == 0.0) {
//                f = 0.0;
//            } else {
//                this.f = Math.pow(g * h, 1.0 / AStarQuery.this.K);
//            }
//            this.path = new LinkedHashSet<>(prePath);
//            this.path.add(preNodeIndex);
//        }

        public PriorityNode(int id, double g, double h, PriorityNode preNode, String predicate) {
            this.id = id;
            this.g = g;
            this.h = h;
            if (g == 0.0 || h == 0.0) {
                f = 0.0;
            } else {
                this.f = Math.pow(g * h, 1.0 / AStarQueryNew.this.K);
            }
            this.path = new Path(preNode.getPath());
            path.add(this.id, predicate);
        }

        public int getId() {
            return id;
        }

        public double getG() {
            return g;
        }

        public double getH() {
            return h;
        }

        public double getF() {
            return f;
        }

        public Path getPath() {
            return path;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PriorityNode) {
                return ((PriorityNode) obj).id == this.id;
            }
            return false;
        }
    }

    class AStarTask implements Runnable {
        int start;
        int taskId;
        String predicate;
        Map<String, Double> similarMap;
        Map<String, Double> similarMap2;
        List<PriorityNode> result;
        Map<Integer, Integer> threadTraversalSet;

        AStarTask(QueryThreadInfo info, int taskId) {
            this.start = graph.getVex(info.getSource());
            this.taskId = taskId;
            this.predicate = info.getPredicate();
            this.similarMap = info.getSimilarMap();
            this.similarMap2 = info.getSimilarMap2();
            result = new ArrayList<>();
            threadTraversalSet = traversalNodeMaps.get(taskId);
        }

        /**
         *
         * @param edgeInfo
         * @return
         */
        public double getPredicateSim(String edgeInfo) {
            if (similarMap.containsKey(edgeInfo))
                return similarMap.get(edgeInfo);
            else
                return 0.0;
        }

        public double getNodeSim(String nodeInfo) {
            if (similarMap2.containsKey(nodeInfo))
                return similarMap2.get(nodeInfo);
            else
                return 0.0;
        }

        @Override
        public void run() {
            try {
                process();
            }catch (Exception e){
//                logger.error("get a Exception on query-thread: " + Thread.currentThread().getName() + ": " + BaseService.logExceptionStack(e));
                isThreadFail = true;
            }finally {
                latch.countDown();
            }
        }


        private void process(){
            Queue<PriorityNode> priorityQueue = new PriorityQueue<>(Comparator.comparing(PriorityNode::getF).reversed().thenComparing(p -> p.getPath().size()));

            int countPath = 0;
            priorityQueue.offer(new PriorityNode(start, 1, 1));

            outer:
            while (!priorityQueue.isEmpty()) {
                PriorityNode priorityNode = priorityQueue.poll();

                Iterator<Graph.ArcNode> itr = (Iterator<Graph.ArcNode>) graph.iterator(priorityNode.getId());
                while (itr.hasNext()) {
                    Graph.ArcNode arcNode = itr.next();
                    // Prevent loop path
                    if (priorityNode.path.contains(arcNode.adjvex)) {
                        continue;
                    }
                    int index = arcNode.adjvex;
                    TwoTuple<String, Double> maxLinkSim = getMaxLinkEdgeSim((List<String>) arcNode.getEdgeInfo());
                    // double nodeSim = getNodeSim(graph.getNodeData(arcNode.adjvex).getName());
                    double g = priorityNode.getG() * (maxLinkSim.getSecond());
                    // System.out.println("start node: " + graph.getNodeData(priorityNode.getId()) + ", current node: " + graph.getNodeData(index) + ", edge: " + maxLinkSim.getFirst() + ", score: " + g);
                    double h = getMaxNodeEdgeSim(index, priorityNode.getId());
                    PriorityNode newNode = new PriorityNode(index, g, h, priorityNode, maxLinkSim.getFirst());

                    // Whether the attributes of the node meet the requirements and meet the result set, otherwise join the priority queue
                    if (checkType(graph.getNodeData(newNode.getId()).getType())) {
                        // This if judgment is to prevent duplicate points. If it is topK for the path, this needs to be removed.
                        if (!threadTraversalSet.containsKey(newNode.getId())) {
                            // Where the node Id and element are stored in the result list
                            threadTraversalSet.put(newNode.getId(), result.size());
                            result.add(newNode);
                            // Requires finding enough points and the pss value in the priority queue does not exceed the pss in the current result set
                            if (isFindEnoughNode()){
//                                System.out.println(countPath);
                                break outer;
//                                if (isNoPssMore(priorityQueue, result)){
//                                    break outer;
//                                }
                            }
                        }
                    } else {
                        priorityQueue.add(newNode);
                    }
                    // priorityQueue.add(newNode);
                    // If the number of traversed paths is greater than the limit
                    if (++countPath >= limitPath) {
                        System.out.println(countPath);
//                        logger.info(Thread.currentThread().getName() + " count path > limit path, then quit...");
                        break outer;
                    }
                }
            }

            // Sort by actual path value from big to small
            for (PriorityNode p : result) {
                if (p.getG() > 0.0) {
//                    System.out.println(p.id);
//                    System.out.println((p.getPath().size() - 1));
                    p.g = Math.pow(p.getG(), 1.0 / (p.getPath().size()));
                } else {
                    p.g = 0.0;
                }
            }
            result.sort(Comparator.comparing(PriorityNode::getG).reversed());
            for (int i = 0; i < result.size(); i++) {
                taskResults[i][taskId] = result.get(i);
            }
            // Assign the maximum number of paths
            maxPath.getAndUpdate(x -> x > result.size() ? x : result.size());
        }

        /**
        * @Author: hqf
        * @Date:
        * @Description: Judging that there are no points in the priority queue that exceed the pss in the current result set, that is, the maximum value in the priority queue must be less than the pss value in all results sets to meet the situation
        */
        private boolean isNoPssMore(Queue<PriorityNode> priorityQueue, List<PriorityNode> results){
            double maxQueue = 0.0;
            double minResults = 99999.0;
            for (PriorityNode priorityNode : priorityQueue) {
                double CurPss = Math.pow(priorityNode.getG(), 1.0 / (priorityNode.getPath().size()));
                if (CurPss > maxQueue){
                    maxQueue = CurPss;
                }
            }
            for (PriorityNode priorityNode : results){
                double CurPss = Math.pow(priorityNode.getG(), 1.0 / (priorityNode.getPath().size()));
                if (CurPss < minResults){
                    minResults = CurPss;
                }
            }
            if (maxQueue < minResults){
                return true;
            }else
                return false;
        }

        /**
         *
         * @param nodeIndex
         * @param preNodeInex
         * @return
         */
        private double getMaxNodeEdgeSim(int nodeIndex, int preNodeInex) {
            double h = 0;
            Iterator<Graph.ArcNode> nextIterator = (Iterator<Graph.ArcNode>) graph.iterator(nodeIndex);
            while (nextIterator.hasNext()) {
                Graph.ArcNode nextArcNode = nextIterator.next();
                if (nextArcNode.adjvex == preNodeInex) {
                    continue;
                }
                double tmp = getMaxLinkEdgeSim((List<String>) nextArcNode.edgeInfo).getSecond();
                if (h < tmp) {
                    h = tmp;
                }
            }
            return h;
        }

        /**
         * @param edgeInfos
         * @return
         */
        private TwoTuple<String, Double> getMaxLinkEdgeSim(List<String> edgeInfos) {
//            OptionalDouble result = edgeInfos.parallelStream().mapToDouble(engeInfo -> getPredicateSim(engeInfo)).max();
            Optional<TwoTuple<String, Double>> result = edgeInfos.parallelStream()
                    .map(edgeInfo -> new TwoTuple<>(edgeInfo, getPredicateSim(edgeInfo)))
                    .max(Comparator.comparing(TwoTuple::getSecond));
            if (result.isPresent()) {
                return result.get();
            } else {
                throw new IllegalArgumentException();
            }
        }

    }

    public AStarQueryNew(RDFGraph graph, List<QueryThreadInfo> queryThreadInfos, String type, int topN, final int K) {
        super(graph, queryThreadInfos, type, topN);
        this.K = K;
        if (queryThreadInfos.size() == 1) {
            limitFactor = 1.0;
        }
        traversalNodeMaps = new ArrayList<>(queryThreadInfos.size());
        int i = 0;
        for (QueryThreadInfo info : queryThreadInfos) {
            traversalNodeMaps.add(new ConcurrentHashMap<>());
            tasks.add(new AStarTask(info, i++));
        }
        latch = new CountDownLatch(tasks.size());
    }

    @Override
    public void run(){
        int n = limitPath;
        taskResults = new PriorityNode[n][tasks.size()];
        for (AStarTask task : tasks) {
            exec.execute(task);
        }
        exec.shutdown();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // showResult(taskResults);
        combineNodeTopK(taskResults);
    }

    class BoundNode {
        int isFind;
        int nodeIndex;
        PriorityNode[] certain;
        double[] bound = new double[2];

        BoundNode(boolean isFind, int nodeIdex, int lines) {
            this.nodeIndex = nodeIdex;
            certain = new PriorityNode[lines];

            this.isFind = 0;
            if (isFind) {
                for (int i = 0; i < lines; i++) {
                    setIsFind(i);
                }
            }
        }

        BoundNode(int isFind, int nodeIdex, PriorityNode[] certain, double[] bound) {
            this.nodeIndex = nodeIdex;
            this.certain = certain;
            this.bound = bound;
            this.isFind = isFind;
        }

        public void setIsFind(int i) {
            if (i >= 32) {
                throw new IllegalArgumentException("out of 31");
            }
            isFind |= (1 << i);
        }

        BoundNode(int nodeIdex, PriorityNode[] certain, double[] bound) {
            this(0, nodeIdex, certain, bound);
        }

        BoundNode(int nodeIdex, int lines) {
            this(false, nodeIdex, lines);
        }

        public double getUpperBound(PriorityNode[] nodes) {
            double upperBound = 0;
            for (int i = 0; i < nodes.length; i++) {
                if (isFind(i)) {
                    upperBound += certain[i].getG();
                } else {
                    if (nodes[i] != null)
                        upperBound += nodes[i].getG();
                }
            }
            return upperBound;
        }

        public boolean isFind() {
            return isFind == ((1 << certain.length) - 1);
        }

        public boolean isFind(int i) {
            if (i >= 32) {
                throw new IllegalArgumentException("out of 31");
            }
            return !(0 == (isFind & (1 << i)));
        }

        public int getNodeIndex() {
            return nodeIndex;
        }

        /**
         *
         * @param nodes
         * @return
         */
        private List<Integer> updateBpundInter(PriorityNode[] nodes) {
            bound[0] = 0;
            bound[1] = 0;
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < nodes.length; i++) {
                PriorityNode node = nodes[i];
                if (certain[i] == null) {
                    if (node.getId() == nodeIndex) {
                        bound[0] += node.getG();
                        certain[i] = node;
                        setIsFind(i);
                    } else {
                        bound[1] += node.getG();
                    }
                } else {
                    bound[0] += certain[i].getG();
                    if (node.getId() == nodeIndex) {
                        result.add(i);
                    }
                }
            }

            bound[1] += bound[0];
            return result;
        }

        /**
         *
         * @param nodes
         * @return
         */
        public List<BoundNode> updateBound(PriorityNode[] nodes) {
            List<Integer> updateIndex = updateBpundInter(nodes);
            List<BoundNode> list = new ArrayList<>();
            list.add(this);
            for (int each : updateIndex) {
                List<BoundNode> next = new ArrayList<>();
                for (BoundNode boundNode : list) {
                    PriorityNode[] tmp = Arrays.copyOf(boundNode.certain, boundNode.certain.length);
                    tmp[each] = nodes[each];
                    double chavalue = boundNode.certain[each].getG() - nodes[each].getG();
                    next.add(
                            new BoundNode(boundNode.isFind, boundNode.nodeIndex, tmp,
                                    new double[]{boundNode.bound[0] - chavalue, boundNode.bound[1] - chavalue}));
                }
                list.addAll(next);
            }
            list.remove(this);
            return list;
        }

        public double getLowBound() {
            return bound[0];
        }

        public double getUpperBound() {
            return bound[1];
        }
    }

    /**
     *
     * @param target
     */
    private void combineTopK(PriorityNode[][] target) {
        Set<Integer> visit = new HashSet<>();
        results = new ArrayList<>();
//        int n = topN*4;
        int n = topN;
        for (int i = 0; i < target.length; i++) {
            double unknowBound = 0;
            for (int j = 0; j < target[i].length; j++) {
                unknowBound += target[i][j].getG();
                if (!visit.contains(target[i][j].getId())) {
                    results.add(new BoundNode(false, target[i][j].getId(), target[i].length));
                    visit.add(target[i][j].getId());
                }
            }

            List<BoundNode> tmp = new ArrayList<>();
            for (BoundNode boundNode : results) {
                List<BoundNode> addNodes = boundNode.updateBound(target[i]);
                if (!addNodes.isEmpty()) {
                    tmp.addAll(addNodes);
                }
            }
            results.addAll(tmp);
            results.sort(Comparator.comparing(BoundNode::getLowBound).reversed());

            if (results.size() < n) {
                continue;
            }
            int m = n;
            double topKLowBound = results.get(m - 1).getLowBound();
            if (results.get(m - 1).isFind()) {
                for (; m < results.size(); m++) {
                    if (!results.get(m).isFind() || results.get(m).getUpperBound() != topKLowBound) {
                        break;
                    }
                }
            }

            boolean flag = topKLowBound > unknowBound;
            for (int j = m; j < results.size() && flag; j++) {
                if (results.get(j).getUpperBound() > topKLowBound) {
                    flag = false;
                }
            }
            if (flag) {
                n = m;
                break;
            }
        }

        System.out.println("==============================topN result==============================");
        results = results.stream().filter(BoundNode::isFind).collect(Collectors.toList());
        results.stream().limit(n).forEach(each -> System.out.println(graph.getNodeData(each.nodeIndex) + " : " + each.getLowBound() + ": " + each.getUpperBound() + each.isFind() + ", "));
        System.out.println();
        System.out.println(results.size());
    }

    /**
     *
     * @param target
     */
    private void combineNodeTopK(PriorityNode[][] target) {
        Map<Integer, BoundNode> map = new HashMap<>();
        results = new ArrayList<>();
        int lines = tasks.size();
        out:
        for (int i = 0; i < maxPath.get(); i++) {
            for (int j = 0; j < lines; j++) {
                if (target[i][j] == null) {
                    continue;
                }
                BoundNode tmp = map.get(target[i][j].getId());
                if (tmp == null) {
                    tmp = new BoundNode(target[i][j].getId(), lines);
                    map.put(target[i][j].getId(), tmp);
                }
                tmp.certain[j] = target[i][j];
                tmp.setIsFind(j);
                if (tmp.isFind()) {
                    double tmpBound = 0;
                    for (PriorityNode p : tmp.certain) {
                        tmpBound += p.getG();
                    }
                    tmp.bound[0] = tmp.bound[1] = tmpBound;
                    results.add(tmp);
                    if (results.size() % topN == 0) {
                        Double[] arr = new Double[results.size()];
                        int k = 0;
                        for (BoundNode b : results) {
                            arr[k++] = b.getLowBound();
                        }

                        double minK = QuickSort.arrKth(arr, topN, Comparator.reverseOrder());
                        boolean flag = true;
                        for (BoundNode b : map.values()) {
                            if (!b.isFind()) {
                                if (b.getUpperBound(target[i]) > minK) {
                                    flag = false;
                                    break;
                                }
                            }
                        }
                        if (flag) {
                            break out;
                        }
                    }
                }

            }
        }
//        System.out.println("==============================topN result==============================");
        results.sort(Comparator.comparing(BoundNode::getLowBound).reversed());
//        results.stream().limit(topN).forEach(each -> System.out.println(graph.getNodeData(each.nodeIndex) + " : " + each.getLowBound() + ": " + each.getUpperBound() + each.isFind() + ", "));
//        System.out.println();
//        System.out.println(results.size());
    }

    private void showResult(PriorityNode[][] results) {
        Map<Double, Integer> countMap = new HashMap<>();
        for (PriorityNode[] result : results) {
            for (PriorityNode priorityNode : result) {
                if (priorityNode != null) {
                    StringBuilder s = new StringBuilder();
                    s.append("[");
                    for (int pathNode : priorityNode.getPath().getNodes()) {
                        s.append(graph.getNodeData(pathNode));
                        s.append(", ");
                    }
                    s.delete(s.length() - 2, s.length());
                    s.append("]");
                    s.append(priorityNode.getId());
                    s.append("  : ");
                    s.append(priorityNode.getG());
                    System.out.println(s);
                }
            }
        }
    }

    public List<BoundNode> getResults() {
        return results;
    }


    public AStarResult getAStarResult() {
        List<AStarPathResult> pathResults = new ArrayList<>();
        int count = 0;
        Map<String, Integer> pathModelMap = new HashMap();
        for (BoundNode each : results) {
            List<List<String>> paths = new ArrayList<>();
            for (PriorityNode priorityNode : each.certain) {
                Entity node = graph.getNodeData(priorityNode.getPath().getStart());
                List<String> path = new ArrayList<>();
                StringBuilder model = new StringBuilder(node.getType());
                path.add(node.getName());
                for (TwoTuple<Integer, String> nodeAndPredicate : priorityNode.getPath()) {
                    node = graph.getNodeData(nodeAndPredicate.getFirst());
                    path.add(nodeAndPredicate.getSecond());
                    path.add(node.getName());
                    model.append("\t");
                    model.append(nodeAndPredicate.getSecond());
                    model.append("\t");
                    model.append(node.getType());
                }
                paths.add(path);
                pathModelMap.merge(model.toString(), 1, (oldValue, value) -> oldValue+1);
            }

            // pathResults.add(new AStarPathResult(graph.getNodeData(each.getNodeIndex()).getName(), paths));
            pathResults.add(new AStarPathResult(graph.getNodeData(each.getNodeIndex()).getName(), graph.getNodeData(each.getNodeIndex()).getId(), each.getUpperBound(), paths));

            if (++count >= topN) {
                break;
            }
        }
        List<AStarPathModel> pathModels = new ArrayList<>();
        for(Map.Entry<String, Integer> entry: pathModelMap.entrySet()){
            pathModels.add(new AStarPathModel(Arrays.asList(entry.getKey().split(" ")), entry.getValue()));
            //pathModels.add(new AStarPathModel(Arrays.asList(entry.getKey().split("\t")), entry.getValue()));
        }
        pathModels.sort(Comparator.comparing(AStarPathModel::getCount).reversed());
        return new AStarResult(pathResults, pathModels);
    }

    /**
     *
     * @return
     */
    private boolean isFindEnoughNode() {
        Set<Integer> interSet = new HashSet<>(traversalNodeMaps.get(0).keySet());
        for (int i = 1; i < traversalNodeMaps.size(); i++) {
            interSet.retainAll(traversalNodeMaps.get(i).keySet());
        }
        return interSet.size() >= limitFactor * topN;
    }


    public static void evaluation(AStarQueryNew aStarQuery, Collection<String> validation) throws IOException {
        int topN = aStarQuery.topN;
        List<BoundNode> testSet = new ArrayList<>();
        Set<Integer> filterSet = new HashSet<>();
        for (BoundNode boundNode : aStarQuery.getResults()) {
            if (!filterSet.contains(boundNode.getNodeIndex())) {
                testSet.add(boundNode);
                filterSet.add(boundNode.getNodeIndex());
            }
            if (filterSet.size() == topN) {
                break;
            }
        }
        if (filterSet.size() < topN) {
            throw new IllegalStateException(filterSet.size() + " elements to sort is less top k : " + topN);
        }
        Set<BoundNode> set = new LinkedHashSet<>();
        int TP = 0;
        int TPMore = 0;
//        writeBoundNodes(aStarQuery.graph, testSet, topN, "E:\\JavaProjects\\rdf_conputer\\result\\Query.Automobile\\compare\\astar_all.txt");
        // System.out.println(filterSet.size());
        for (int i = 0; i < topN; i++) {
            BoundNode each = testSet.get(i);
            boolean flag = true;
            for (String v : validation) {
                String name = aStarQuery.graph.getNodeData(each.getNodeIndex()).getId();
                if (v.equals(name)) {
                    TP++;
                    flag = false;
                    break;
                }
            }
        }
        for (BoundNode each : testSet) {
           // System.out.println(aStarQuery.graph.getNodeData(each.getNodeIndex()));
//            each.certain[0].getPath().forEach(index -> System.out.print("-->" + aStarQuery.graph.getNodeData(index)));
//            System.out.println();
        }
        double P = (TP + TPMore) * 1.0 / testSet.size();
        double R = (TP + TPMore) * 1.0 / (validation.size() + TPMore);
        System.out.println("TP: " + TP + "\tTPMore: " + TPMore + "\tN: " + topN + "\tM: " + validation.size());
        System.out.println((TP + TPMore) + "/" + topN + "     " + (TP + TPMore) + "/" + (validation.size() + TPMore));
        System.out.println("Precision : " + P + "\n" + "recall : " + R);
    }


    public static void writeBoundNodes(RDFGraph graph, Collection<BoundNode> collection, int maxNumber, String path) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
        int count = 0;
        for (BoundNode each : collection) {
            writer.write(graph.getNodeData(each.getNodeIndex()).toString());
            writer.write("[[");
            each.certain[0].getPath().getNodes().forEach(index -> {
                try {
                    writer.write("-->" + graph.getNodeData(index));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("]]");
            writer.write("[" + each.getLowBound() + "," + each.getUpperBound() + "]");
            writer.newLine();
            if (count++ == maxNumber) {
                break;
            }
        }
        writer.close();
    }

    /**
     * @param graph
     * @param source
     * @return
     */
    private static MultiSourcePredicatesPaths getPredicates(RDFGraph graph, BoundNode source) {
        MultiSourcePredicatesPaths result = new MultiSourcePredicatesPaths();
        for (PriorityNode paths : source.certain) {
            List<List<String>> predicatePath = new ArrayList<>();
            List<Integer> list = new ArrayList<>(paths.getPath().getNodes());
            for (int i = list.size() - 1; i > 0; i--) {
                predicatePath.add(graph.getEdgeInfo(list.get(i), list.get(i - 1)));
            }
            result.addPredicatesPaths(list.get(0), predicatePath);
        }
        return result;
    }

}

