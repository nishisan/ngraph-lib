/*
 * Copyright (C) 2023 Lucas Nishimura <lucas.nishimura@gmail.com>
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

import dev.nishisan.graph.IGraph;
import dev.nishisan.graph.elements.impl.StringEdge;
import dev.nishisan.graph.elements.impl.StringVertex;
import dev.nishisan.graph.impl.StringGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * created 27.10.2023
 */
public class BasicStringGraphStressTest {

    @Test
    public void createLargeGraphTest() throws InterruptedException, ExecutionException {
        /**
         * Initiate interface
         */
        IGraph<StringEdge, StringVertex> graph = new StringGraph();

        int levels = 3;         // Número de níveis no grafo
        int nodesPerLevel = 4;  // Nós por nível

        // Lista para guardar os nós do nível anterior
        List<StringVertex> prevLevelNodes = new ArrayList<>();
        StringVertex firstNode = null;
        // Vamos começar a construir o grafo!
        for (int level = 0; level < levels; level++) {
            List<StringVertex> currentLevelNodes = new ArrayList<>();
            for (int i = 0; i < nodesPerLevel; i++) {
                // Criar um novo nó para este nível
                String nodeName = "NODE-" + level + "-" + i;
                StringVertex node = graph.addVertex(nodeName);
                if (firstNode == null) {
                    firstNode = node;
                }
                currentLevelNodes.add(node);

                // Conectar com os nós do nível anterior
                for (StringVertex prevNode : prevLevelNodes) {
                    graph.addEdge(prevNode, node);
                }
            }

            // Atualizar os nós do nível anterior
            prevLevelNodes = currentLevelNodes;
        }

        // Agora você tem um grafo circular com múltiplos níveis e elementos!
        // Você pode estressar seu algoritmo com esse grafo grande.
        System.out.println("::: Total Edges:" + graph.getEdgeCount());
        System.out.println("::: Total Vertex:" + graph.getVertexCount());
        System.out.println("Found Solutions:");
        AtomicLong total = new AtomicLong(0);
        // single thread

        graph.walk(firstNode, 0, 4).forEach(a -> {
            total.incrementAndGet();
            System.out.println("Found:" + total.get());
        });
        
//        graph.walk(firstNode).forEach(a -> {
//            total.incrementAndGet();
//        });

        /**
         * .88284 é o esperado para 3L 4N nodes
         */
        System.out.println("Found.." + total.get());
        System.out.println("Max Queue Usage.." + graph.getMaxQueueUsage());

    }

}
