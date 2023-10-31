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
import dev.nishisan.graph.queue.list.EdgeList;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * created 26.10.2023
 */
public interface IElementProvider<E extends IEdge<? extends Serializable, ? extends IVertex<? extends Serializable>>, V extends IVertex<? extends Serializable>> {

    
    /**
     * Retrieve an Edge by its ID
     *
     * @param id
     * @return
     */
    public E getEdgeById(String id);

    /**
     * Retrieve a Vertex by its ID
     *
     * @param id
     * @return
     */
    public V getVertexById(String id);

    /**
     * Retrieves the direct connected edges from the vertex
     *
     * @param vertex
     * @param direction
     * @return
     */
    public EdgeList<E> getAdjacentEdgesFromVertex(V vertex, String direction);

    /**
     * Get the list of edges by a given vertex
     *
     * @param vertex
     * @return
     */
    public EdgeList<E> getEdgesByVertex(V vertex);

    /**
     * Adds an Edge to the the persistence layer
     *
     * @param e
     * @return
     */
    public E addEdge(E e);

    /**
     * Adds a vertex to the persistence layer
     *
     * @param v
     * @return
     */
    public V addVertex(V v);

    /**
     * Get the total vertex count in the persistence layer
     *
     * @return
     */
    public Long getVertexCount();

    /**
     * Get the total edge count in the persistence layer
     *
     * @return
     */
    public Long getEdgeCount();

    /**
     * Return a Stream of all edges
     *
     * @return
     */
    public Stream<E> getEdges();

}
