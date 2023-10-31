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

import dev.nishisan.graph.elements.impl.StringVertex;
import dev.nishisan.graph.impl.StringGraph;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 29.10.2023
 */
public class DfsWalkToTargetTest {

    @Test
    public void createNodesConnectionsAndWalkToATargetSingleThreaded() {
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
        System.out.println("start :createNodesConnectionsAndWalkToATargetSingleThreaded");
        AtomicLong total = new AtomicLong(0);

        graph.dfs(node1, node6,0,4).forEach(e -> {
            total.incrementAndGet();
            System.out.println("MThread : " + e.size());
            System.out.println("Dump Path - MThread:");
            e.getVertices().forEach(p -> {
                System.out.println(" P:" + p.getId());
            });
        });

        System.out.println("Total Paths Found:" + total.get());
        System.out.println("end :createNodesConnectionsAndWalkToATargetSingleThreaded");
        assertEquals(2, total.get());
    }
}
