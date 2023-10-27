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
package dev.nishisan.graph.providers;

import dev.nishisan.graph.elements.IEdge;
import dev.nishisan.graph.elements.IVertex;
import java.util.List;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * created 26.10.2023
 */
public interface IElementProvider<E extends IEdge, V extends IVertex> {

    public E getEdgeById(String id);

    public V getVertexById(String id);

    public List<E> getAdjacentEdgesFromVertex(V vertex, String direction);

    public List<E> getEdgesByVertex(V vertex);

    public E addEdge(E e);

    public V addVertex(V v);

    public Long getVertexCount();

    public Long getEdgeCount();

}
