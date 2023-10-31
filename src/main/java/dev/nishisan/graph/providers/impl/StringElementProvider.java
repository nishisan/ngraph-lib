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
package dev.nishisan.graph.providers.impl;

import dev.nishisan.graph.elements.impl.StringEdge;
import dev.nishisan.graph.elements.impl.StringVertex;
import dev.nishisan.graph.providers.IElementProvider;
import dev.nishisan.graph.queue.list.EdgeList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is a Sample Element Provider, with a fixed IN Memory Map
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 27.10.2023
 */
public class StringElementProvider implements IElementProvider<StringEdge, StringVertex> {

    private Map<String, StringEdge> edges = new ConcurrentHashMap<>();
    private Map<String, StringVertex> vertexes = new ConcurrentHashMap<>();

    @Override
    public StringEdge getEdgeById(String id) {
        return this.edges.get(id);
    }

    @Override
    public StringVertex getVertexById(String id) {
        return this.vertexes.get(id);
    }

    @Override
    public EdgeList<StringEdge> getAdjacentEdgesFromVertex(StringVertex vertex, String direction) {
        EdgeList<StringEdge> result = new EdgeList<>();
        if (direction.equalsIgnoreCase("ANY")) {
            result = this.edges.values().parallelStream().filter(e -> e.contains(vertex)).collect(Collectors.toCollection(EdgeList::new));
        } else if (direction.equals("OUTBOUND")) {
            result = this.edges.values().parallelStream().filter(e -> e.getFrom().equals(vertex)).collect(Collectors.toCollection(EdgeList::new));
        } else if (direction.equals("INBOUND")) {
            result = this.edges.values().parallelStream().filter(e -> e.getTo().equals(vertex)).collect(Collectors.toCollection(EdgeList::new));
        }

        return result;
    }

    @Override
    public EdgeList<StringEdge> getEdgesByVertex(StringVertex vertex) {
        EdgeList<StringEdge> result = this.edges.values().parallelStream().filter(e -> e.contains(vertex)).collect(Collectors.toCollection(EdgeList::new));
        return result;
    }

    @Override
    public StringEdge addEdge(StringEdge e) {
        this.edges.put(e.getId(), e);
        return e;
    }

    @Override
    public StringVertex addVertex(StringVertex v) {
        this.vertexes.put(v.getId(), v);
        return v;
    }

    @Override
    public Long getVertexCount() {
        return Long.valueOf(this.vertexes.size());
    }

    @Override
    public Long getEdgeCount() {
        return Long.valueOf(this.edges.size());
    }

    @Override
    public Stream<StringEdge> getEdges() {
        return this.edges.values().stream();
    }

}
