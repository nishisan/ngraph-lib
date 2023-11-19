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
package dev.nishisan.graph.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 26.10.2023
 * @param <T>
 * @param <E>
 */
public abstract class AbstractVertex<T extends Serializable, E extends IEdge<T, ? extends IVertex<T, E>>> extends AbstractElement<T> implements IVertex<T, E> {

    private Map<T, E> directedEdges = new ConcurrentHashMap<>();

    public AbstractVertex(T id, T data) {
        super(id, data);
    }

    @Override
    public void addEdge(E edge) {
        if (!this.directedEdges.containsKey(edge.getId())) {
            this.directedEdges.put(edge.getId(), edge);
        }
    }

    @Override
    public Long getDegree() {
        Long degree = Long.valueOf(this.directedEdges.size());
        return degree;
    }

    @Override
    public List<E> getEdges() {
        List<E> edgesList = new ArrayList<>();
        edgesList.addAll(this.directedEdges.values());
        return edgesList;
    }

}
