package com.graph.query;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/8/9.
 */

public class Graph<T, K> {
    VexNode<T>[] vexNodes;
    int vexnum, arcnum;
    boolean isDirected;

    class VexNode<T> {
        T data;
        ArcNode firstarc;
        VexNode(T data, ArcNode firstarc){
            this.data = data;
            this.firstarc = firstarc;
        }

        VexNode(T data){
            this(data, null);
        }

        private Iterator<ArcNode> iterator(){
            return new Itr();
        }

        private class Itr implements Iterator<ArcNode>{
            private ArcNode pre;
            private ArcNode current;
            Itr(){
                current = firstarc;
            }
            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public ArcNode next() {
                if(current == null){
                    throw new NoSuchElementException();
                }
                pre = current;
                current = current.next;
                return pre;
            }
        }
    }

    public class ArcNode<K> {
        int adjvex;
        ArcNode next;
        K edgeInfo;
        // K nodeInfo;

        ArcNode(int adjvex, K edgeInfo, ArcNode next){
            this.adjvex = adjvex;
            this.next = next;
            this.edgeInfo = edgeInfo;
           // this.nodeInfo = nodeInfo;
        }

//        ArcNode(int adjvex, K edgeInfo){
//            this(adjvex, edgeInfo, null);
//        }

        ArcNode(int adjvex, K edgeInfo){
            this(adjvex, edgeInfo, null);
        }

        public int getAdjvex() {
            return adjvex;
        }

        public K getEdgeInfo() {
            return edgeInfo;
        }
    }

    Graph(){
        this(true);
    }

    Graph(boolean isDirected){
        this.isDirected = isDirected;
    }

    /**
     * @param vexs
     * @param edges
     * @param edgesInfo
     */
    public void createGraph(T[] vexs, T[][] edges, K[] edgesInfo){
        createVexNode(vexs);
        creeateArcNode(edges, edgesInfo);
    }

    public void createGraph(Collection<T> vexs, Collection<T[]> edges, Collection<K> edgesInfo){
        createVexNode(vexs);
        System.out.println("create node over");
        creeateArcNode(edges, edgesInfo);
    }

    protected void createVexNode(Collection<T> vexs){
        vexNodes = new VexNode[vexs.size()];
        int i = 0;
        for(T vex: vexs){
            vexNodes[i] = new VexNode<T>(vex);
            i++;
        }
    }

    protected void createVexNode(T[] vexs){
        vexNodes = new VexNode[vexs.length];
        for(int i = 0; i < vexs.length; i++){
            vexNodes[i] = new VexNode<T>(vexs[i]);
        }
    }

    protected void creeateArcNode(Collection<T[]> edges, Collection<K> edgesInfo){
        if(edges.size() != edgesInfo.size()){
            throw new IllegalArgumentException("edge's length not equal edgeInfo's length");
        }
        Iterator<T[]> edgeIterator = edges.iterator();
        Iterator<K> edgeInfoIterator = edgesInfo.iterator();
        while (edgeIterator.hasNext() && edgeInfoIterator.hasNext()){
            insertArc(edgeIterator.next(), edgeInfoIterator.next());
        }
    }

    protected void creeateArcNode(T[][] edges, K[] edgesInfo){
        if(edges.length != edgesInfo.length){
            throw new IllegalArgumentException("edge's length not equal edgeInfo's length");
        }
        for(int i = 0; i < edges.length; i++){
            insertArc(edges[i], edgesInfo[i]);
        }
    }

    public T getNodeData(int index){
        return vexNodes[index].data;
    }


    /**
     * @param fromIndex
     * @param toIndex
     * @return
     */
    public K getEdgeInfo(int fromIndex, int toIndex){
        Iterator<ArcNode> iterator = vexNodes[fromIndex].iterator();
        while (iterator.hasNext()){
            ArcNode<K> arcNode = iterator.next();
            if(arcNode.getAdjvex() == toIndex){
                return arcNode.getEdgeInfo();
            }
        }
        return null;
    }

    /**
     * @param t
     * @return
     */
    protected int getVex(T t){
        for(int i = 0; i < vexNodes.length; i++){
            if(vexNodes[i].data.equals(t)){
                return i;
            }
        }
        return -1;
    }

    /**
     * @param edge
     * @param edgeInfo
     */
    private void insertArc(T[] edge, K  edgeInfo){
        int start = getVex(edge[0]);
        int end = getVex(edge[1]);
//        Iterator<ArcNode> iteratorStart = iterator(start);
//        Set<Integer> startSet = new HashSet<>();
//        while (iteratorStart.hasNext()) {
//            startSet.add(iteratorStart.next().getAdjvex());
//        }

        if(start == -1 || end == -1){
            throw new NoSuchElementException("no such node: " + edge[0] + " " + edge[1]);
        }
        if(start == end){
            return;
        }
        ArcNode<K> newArcNode = new ArcNode(end,edgeInfo);
        ArcNode p = vexNodes[start].firstarc;
        vexNodes[start].firstarc = newArcNode;
        newArcNode.next = p;

        if(!isDirected){
            newArcNode = new ArcNode(start,edgeInfo);
            p = vexNodes[end].firstarc;
            vexNodes[end].firstarc = newArcNode;
            newArcNode.next = p;
        }

//        if (!startSet.contains(end)) {
//            ArcNode<K> newArcNode = new ArcNode(end,edgeInfo);
//            ArcNode p = vexNodes[start].firstarc;
//            vexNodes[start].firstarc = newArcNode;
//            newArcNode.next = p;
//
//            if(!isDirected){
//                newArcNode = new ArcNode(start,edgeInfo);
//                p = vexNodes[end].firstarc;
//                vexNodes[end].firstarc = newArcNode;
//                newArcNode.next = p;
//            }
//        }
    }

    public void BFS(int start){
        Set<Integer> visitNodes = new HashSet<Integer>();
        Set<Integer> currentNodes = new HashSet<Integer>();
        visitNodes.add(start);
        currentNodes.add(start);
        while (!currentNodes.isEmpty()){
            Set<Integer> nextNodes = new HashSet<Integer>();
            for(int currentIndex: currentNodes){
                ArcNode p = vexNodes[currentIndex].firstarc;
                while (p != null){
                    if(!visitNodes.contains(p.adjvex)){
                        nextNodes.add(p.adjvex);
                        visitNodes.add(p.adjvex);
                    }
                    p = p.next;
                }
            }
            currentNodes = nextNodes;
//            System.out.println(nextNodes);
        }
    }

    public void show(){
        for(VexNode vexNode: vexNodes){
            System.out.print(vexNode.data);
            ArcNode p = vexNode.firstarc;
            while (p != null){
                System.out.print("===" + p.edgeInfo + "===>" + vexNodes[p.adjvex].data);
                p = p.next;
            }
            System.out.println();
        }
    }

    public Iterator<ArcNode> iterator(int start){
        return  vexNodes[start].iterator();
    }

}
