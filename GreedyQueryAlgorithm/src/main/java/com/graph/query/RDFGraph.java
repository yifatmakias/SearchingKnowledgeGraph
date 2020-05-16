package com.graph.query;

import java.util.*;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/8/10.
 */
public class RDFGraph extends Graph<Entity, List<String>> {
    Map<String, List<Integer>> vexTypeIndex;
    Map<Entity, Integer> vexIndex;

    public Map<Entity, Integer> getVexIndex() {
        return vexIndex;
    }

    public void setVexIndex(Map<Entity, Integer> vexIndex) {
        this.vexIndex = vexIndex;
    }

    public RDFGraph(){
        this(true);
    }

    public RDFGraph(boolean isDirected){
        super(isDirected);
        vexTypeIndex = new HashMap<>();
        vexIndex = new HashMap<>();
    }

    public Map<String, List<Integer>> getVexTypeIndex() {
        return vexTypeIndex;
    }

    @Override
    protected void createVexNode(Entity[] vexs){
        vexNodes = new VexNode[vexs.length];
        for(int i = 0; i < vexs.length; i++){
            vexNodes[i] = new VexNode<Entity>(vexs[i]);
            List<Integer> indexs = vexTypeIndex.get(vexs[i].getType());
            if(indexs == null){
                vexTypeIndex.put(vexs[i].getType(), new ArrayList<>(Arrays.asList(i)));
            }else {
                indexs.add(i);
            }
            vexIndex.put(vexs[i], i);
        }
    }

    @Override
    protected void createVexNode(Collection<Entity> vexs){
        vexNodes = new VexNode[vexs.size()];
        int  i = 0;
        for(Entity vex: vexs){
            vexNodes[i] = new VexNode<Entity>(vex);
            List<Integer> indexs = vexTypeIndex.get(vex.getType());
            if(indexs == null){
                vexTypeIndex.put(vex.getType(), new ArrayList<>(Arrays.asList(i)));
            }else {
                indexs.add(i);
            }
            vexIndex.put(vex, i);
            i++;
        }
    }

    /**
     * @param t
     * @return
     */
    @Override
    protected int getVex(Entity t){
        Integer index = vexIndex.get(t);
        if(index != null){
            return index;
        }
        System.out.println(t.getId());
        return -1;
    }


    public int outNearNodesRandom(int start){
        if (vexNodes[start].firstarc == null){
            System.out.println("No near Nodes!");
            return -1;
        } else{
            List<ArcNode> arcNodes = new ArrayList<>();
            ArcNode cur = vexNodes[start].firstarc;
            while (cur != null){
                arcNodes.add(cur);
                cur = cur.next;
            }
            Random random = new Random();
            int i = random.nextInt(arcNodes.size());
            return arcNodes.get(i).adjvex;
        }
    }

    public int getVex(String vexId) {
        return  getVex(new Entity(vexId, null, null));
    }
}
