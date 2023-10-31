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
package dev.nishisan.graph.impl;

import dev.nishisan.graph.AbsBaseGraph;
import dev.nishisan.graph.elements.impl.StringEdge;
import dev.nishisan.graph.elements.impl.StringVertex;
import dev.nishisan.graph.providers.IElementProvider;
import dev.nishisan.graph.providers.impl.StringElementProvider;

/**
 * This is a Sample String graph object
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 27.10.2023
 */
public class StringGraph extends AbsBaseGraph<StringEdge, StringVertex, String> {

    public StringGraph() {
        super(new StringElementProvider());
    }

    public StringGraph(IElementProvider<StringEdge, StringVertex> elementProvider) {
        super(elementProvider);
    }

    public StringGraph(IElementProvider<StringEdge, StringVertex> elementProvider, int queueCapacity) {
        super(elementProvider, queueCapacity);
    }

    @Override
    public StringEdge addEdge(StringEdge edge) {
        return this.getProvider().addEdge(edge);
    }

    @Override
    public StringEdge addEdge(String id) {
        StringEdge edge = new StringEdge(id, id);
        return this.addEdge(edge);
    }

    @Override
    public StringVertex addVertex(StringVertex vertex) {
        return this.getProvider().addVertex(vertex);
    }

    @Override
    public StringEdge addEdge(StringVertex from, StringVertex to) {
        StringEdge edge = new StringEdge(from.getId() + "." + to.getId(), from.getId() + "." + to.getId());
        edge.setFrom(from);
        edge.setTo(to);
        return this.addEdge(edge);
    }

    @Override
    public StringEdge addEdge(String fromId, String toId) {
        StringVertex from = this.addVertex(fromId);
        StringVertex to = this.addVertex(toId);
        return this.addEdge(from, to);
    }

    @Override
    public StringVertex addVertex(String id) {
        StringVertex vertex = new StringVertex(id, id);
        return this.addVertex(vertex);
    }

    @Override
    public StringEdge getEdgeById(String id) {
        return this.getProvider().getEdgeById(id);
    }

    @Override
    public StringVertex getVertexById(String id) {
        return this.getProvider().getVertexById(id);
    }

    @Override
    public Long getVertexCount() {
        return this.getProvider().getVertexCount();
    }

    @Override
    public Long getEdgeCount() {
        return this.getProvider().getEdgeCount();
    }

}
