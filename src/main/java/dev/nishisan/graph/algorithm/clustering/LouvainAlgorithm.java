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
package dev.nishisan.graph.algorithm.clustering;

import dev.nishisan.graph.IGraph;
import dev.nishisan.graph.elements.IEdge;
import dev.nishisan.graph.elements.IVertex;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * I am Not A Mathematician, so please forgive me if before reading this....
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 30.10.2023
 */
public class LouvainAlgorithm<E extends IEdge<T, V>, V extends IVertex<T>, T extends Serializable> {

    private final IGraph<E, V, T> graph;

    public LouvainAlgorithm(IGraph<E, V, T> graph) {
        this.graph = graph;
    }

    private double calculateModularity(Map<V, V> nodeToCommunity) {
        final double[] modularity = {0.0};
        long totalEdges = this.graph.getProvider().getEdgeCount();

        this.graph.getProvider().getEdges().forEach((IEdge edge) -> {
            List<V> vertices = edge.getVertices();
            V u = vertices.get(0);
            V v = vertices.get(1);

            int Auv = 1;

            int ku = this.graph.getProvider().getEdgesByVertex(u).size();
            int kv = this.graph.getProvider().getEdgesByVertex(v).size();

            IVertex deltaU = nodeToCommunity.get(u);
            IVertex deltaV = nodeToCommunity.get(v);

            int delta = 0;
            if (deltaU.equals(deltaV)) {
                delta = 1;
            }

            modularity[0] += (Auv - ((double) ku * kv / (2 * totalEdges))) * delta;
        });

        modularity[0] /= (2 * totalEdges);
        return modularity[0];
    }

    private Map<V, V> initializeCommunities() {
        Map<V, V> nodeToCommunity = new HashMap<>();
        this.graph.getProvider().getEdges().forEach((E edge) -> {
            edge.getVertices().forEach((V v) -> {
                nodeToCommunity.put(v, v);
            });
        });
        return nodeToCommunity;
    }

    public Map<V, Set<V>> detectCommunities() {
        Map<V, V> nodeToCommunity = initializeCommunities();
        double modularity = calculateModularity(nodeToCommunity);

        final AtomicBoolean improvement = new AtomicBoolean(true);
        while (improvement.get()) {
            improvement.set(false);

            this.graph.getProvider().getEdges().forEach(edge -> {
                for (V node : edge.getVertices()) {
                    V bestCommunity = findBestCommunity(node, nodeToCommunity);
                    if (!bestCommunity.equals(nodeToCommunity.get(node))) {
                        nodeToCommunity.put(node, bestCommunity);
                        improvement.set(true);
                    }
                }
            });

            double newModularity = calculateModularity(nodeToCommunity);
            if (newModularity > modularity) {
                modularity = newModularity;
            } else {
                improvement.set(false);
            }
        }

        return buildCommunityStructure(nodeToCommunity);
    }

    private V findBestCommunity(V node, Map<V, V> nodeToCommunity) {
        V bestCommunity = nodeToCommunity.get(node);
        double bestModularityGain = 0.0;

        V currentCommunity = nodeToCommunity.get(node);

        Map<V, V> tempNodeToCommunity = new HashMap<>(nodeToCommunity);

        for (E edge : this.graph.getProvider().getEdgesByVertex(node)) {
            for (V neighbor : edge.getVertices()) {
                if (!neighbor.equals(node)) {
                    tempNodeToCommunity.put(node, nodeToCommunity.get(neighbor));
                    double modularityGain = calculateModularity(tempNodeToCommunity) - calculateModularity(nodeToCommunity);
                    if (modularityGain > bestModularityGain) {
                        bestModularityGain = modularityGain;
                        bestCommunity = nodeToCommunity.get(neighbor);
                    }
                    tempNodeToCommunity.put(node, currentCommunity);
                }
            }
        }

        return bestCommunity;
    }

    private Map<V, Set<V>> buildCommunityStructure(Map<V, V> nodeToCommunity) {
        Map<V, Set<V>> communityStructure = new HashMap<>();

        for (Map.Entry<V, V> entry : nodeToCommunity.entrySet()) {
            V node = entry.getKey();
            V community = entry.getValue();
            communityStructure.computeIfAbsent(community, k -> new HashSet<>()).add(node);
        }

        return communityStructure;
    }
}
