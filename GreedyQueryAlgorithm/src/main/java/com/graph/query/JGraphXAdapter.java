package com.graph.query;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graphs;
import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import javax.swing.*;
import java.awt.*;

/**
 * A demo applet that shows how to use JGraphX to visualize JGraphT graphs. Applet based on
 * JGraphAdapterDemo.
 *
 */
public class JGraphXAdapter
        extends
        JApplet
{
    private static final long serialVersionUID = 2202072534703043194L;

    private static final Dimension DEFAULT_SIZE = new Dimension(600, 400);

    private org.jgrapht.ext.JGraphXAdapter<String, DefaultEdge> jgxAdapter;
    private DefaultListenableGraph<String, DefaultEdge> graph;

    public JGraphXAdapter(DefaultListenableGraph<String, DefaultEdge> graph) throws HeadlessException {
        this.graph = graph;
    }

    @Override
    public void init()
    {
        // create a visualization using JGraph, via an adapter
        jgxAdapter = new org.jgrapht.ext.JGraphXAdapter<String, DefaultEdge>(graph);

        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(DEFAULT_SIZE);

        // positioning via jgraphx layouts
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);

        // center the circle
        int radius = 50;
        layout.setX0(10);
        layout.setY0(10);
        layout.setRadius(radius);
        layout.setMoveCircle(true);

        layout.execute(jgxAdapter.getDefaultParent());
        // that's all there is to it!...
    }
}