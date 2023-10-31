/*
 * Copyright (C) 2023 Lucas Nishimura <lucas.nishimura at gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package dev.nishisan.graph.tests;

import dev.nishisan.graph.elements.impl.StringEdge;
import dev.nishisan.graph.elements.impl.StringVertex;
import dev.nishisan.graph.impl.StringGraph;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 27.10.2023
 */
public class DfsBasicStringGraphTest {

    @Test
    public void createGraphAndAddNodesTest() {
        StringGraph graph = new StringGraph();
        StringVertex node1 = graph.addVertex("NODE-1");
        StringVertex node2 = graph.addVertex("NODE-2");
        StringVertex node3 = graph.addVertex("NODE-3");
        StringVertex node4 = graph.addVertex("NODE-4");
        StringVertex node5 = graph.addVertex("NODE-5");
        StringVertex node6 = graph.addVertex("NODE-6");

        assertNotNull(node1);
        assertNotNull(node2);
        assertNotNull(node3);
        assertNotNull(node4);
        assertNotNull(node5);
        assertNotNull(node6);

        assertEquals(6, graph.getVertexCount());
    }

    @Test
    public void createGraphAndAddNodesAndConnectionsTest() {
        StringGraph graph = new StringGraph();
        StringVertex node1 = graph.addVertex("NODE-1");
        StringVertex node2 = graph.addVertex("NODE-2");
        StringVertex node3 = graph.addVertex("NODE-3");
        StringVertex node4 = graph.addVertex("NODE-4");
        StringVertex node5 = graph.addVertex("NODE-5");
        StringVertex node6 = graph.addVertex("NODE-6");

        assertNotNull(node1);
        assertNotNull(node2);
        assertNotNull(node3);
        assertNotNull(node4);
        assertNotNull(node5);
        assertNotNull(node6);

        StringEdge connection1 = graph.addEdge(node1, node2);
        assertNotNull(connection1);
        graph.addEdge(node2, node3);
        graph.addEdge(node3, node4);
        graph.addEdge(node4, node5);
        graph.addEdge(node5, node6);
        assertEquals(6, graph.getVertexCount());
        assertEquals(5, graph.getEdgeCount());
    }

    @Test
    public void createNodesConnectionsAndWalkSingleThread() {
        StringGraph graph = new StringGraph();

        StringVertex node1 = graph.addVertex("NODE-1");
        StringVertex node2 = graph.addVertex("NODE-2");
        StringVertex node3 = graph.addVertex("NODE-3");
        StringVertex node4 = graph.addVertex("NODE-4");
        StringVertex node5 = graph.addVertex("NODE-5");
        StringVertex node6 = graph.addVertex("NODE-6");

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node2, node5);
        graph.addEdge(node3, node4);
        graph.addEdge(node4, node5);
        graph.addEdge(node5, node6);
        System.out.println("start :createNodesConnectionsAndWalkSingleThread");
        AtomicLong total = new AtomicLong(0);
        graph.dfs(node1).forEach(e -> {
            total.incrementAndGet();
            System.out.println("P: " + e.size());
            System.out.println("Dump Path:");
            e.forEach(p -> {
                System.out.println(" P:" + p.getId());
            });
        });
        System.out.println("end :createNodesConnectionsAndWalkSingleThread");
        assertEquals(4, total.get());
    }

    @Test
    public void createNodesConnectionsAndWalkMultiThread() {
        StringGraph graph = new StringGraph();

        StringVertex node1 = graph.addVertex("NODE-1");
        StringVertex node2 = graph.addVertex("NODE-2");
        StringVertex node3 = graph.addVertex("NODE-3");
        StringVertex node4 = graph.addVertex("NODE-4");
        StringVertex node5 = graph.addVertex("NODE-5");
        StringVertex node6 = graph.addVertex("NODE-6");

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node2, node5);
        graph.addEdge(node3, node4);
        graph.addEdge(node4, node5);
        graph.addEdge(node5, node6);

        AtomicLong total = new AtomicLong(0);

        graph.dfs(node1, 0, 4).forEach(e -> {
            total.incrementAndGet();
            System.out.println("MThread : " + e.size());
            System.out.println("Dump Path - MThread:");
            e.forEach(p -> {
                System.out.println(" P:" + p.getId());
            });
        });
        System.out.println("Total Paths Found:" + total.get());
        assertEquals(4, total.get());
    }

    @Test
    public void createNodesConnectionsAndWalkWithoutAnswerMultiThread() {
        StringGraph graph = new StringGraph();

        StringVertex node1 = graph.addVertex("NODE-1");
        StringVertex node2 = graph.addVertex("NODE-2");
        StringVertex node3 = graph.addVertex("NODE-3");
        StringVertex node4 = graph.addVertex("NODE-4");
        StringVertex node5 = graph.addVertex("NODE-5");
        StringVertex node6 = graph.addVertex("NODE-6");

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node2, node5);
        graph.addEdge(node3, node4);
        graph.addEdge(node4, node5);
//        graph.addEdge(node5, node6); // do not connect

        AtomicLong total = new AtomicLong(0);

        graph.dfs(node1, node6, 0, 4).forEach(e -> {
            total.incrementAndGet();
            System.out.println("MThread : " + e.size());
            System.out.println("Dump Path - MThread:");
            e.forEach(p -> {
                System.out.println(" P:" + p.getId());
            });
        });
        System.out.println("Total Paths Found:" + total.get());
        assertEquals(0, total.get());
    }

    @Test
    public void createNodesConnectionsAndWalkToATargetMultiThreaded() {
        StringGraph graph = new StringGraph();

        StringVertex node1 = graph.addVertex("NODE-1");
        StringVertex node2 = graph.addVertex("NODE-2");
        StringVertex node3 = graph.addVertex("NODE-3");
        StringVertex node4 = graph.addVertex("NODE-4");
        StringVertex node5 = graph.addVertex("NODE-5");
        StringVertex node6 = graph.addVertex("NODE-6");

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node2, node5);
        graph.addEdge(node3, node4);
        graph.addEdge(node4, node5);
        graph.addEdge(node5, node6);

        AtomicLong total = new AtomicLong(0);
        graph.dfs(node1, node5, 0, 4).forEach(e -> {
            total.incrementAndGet();
            System.out.println("MThread : " + e.size());
            System.out.println("Dump Path - MThread:");
            e.forEach(p -> {
                System.out.println(" P:" + p.getId());
            });
        });
        System.out.println("Total Paths Found To Target:" + total.get());
        assertEquals(2, total.get());
    }

    @Test
    public void basicCircularReferenceProblemTest() {
        StringGraph graph = new StringGraph();

        StringVertex node1 = graph.addVertex("NODE-1");
        StringVertex node2 = graph.addVertex("NODE-2");
        StringVertex node3 = graph.addVertex("NODE-3");
        StringVertex node4 = graph.addVertex("NODE-4");
        StringVertex node5 = graph.addVertex("NODE-5");
        StringVertex node6 = graph.addVertex("NODE-6");
        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node3, node4);
        graph.addEdge(node4, node5);
        graph.addEdge(node5, node6);
        graph.addEdge(node6, node1);
        System.out.println("Circular Start");
        graph.dfs(node1).forEach(a -> {
            System.out.println("Achei !" + a.size());
        });
        System.out.println("Circular End");
    }
}
