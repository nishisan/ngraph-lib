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
package dev.nishisan.graph;

import dev.nishisan.graph.elements.IEdge;
import dev.nishisan.graph.elements.IVertex;
import dev.nishisan.graph.providers.IElementProvider;
import dev.nishisan.graph.queue.list.EdgeList;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * @param <E>
 * @param <V> created 26.10.2023
 */
public interface IGraph<E extends IEdge<T, V>, V extends IVertex<T>, T extends Serializable> {

    public E addEdge(V from, V to) throws UnsupportedOperationException;

    /**
     * Add an Edge
     *
     * @param fromId
     * @param toId
     * @return
     */
    public E addEdge(String fromId, String toId) throws UnsupportedOperationException;

    /**
     * Adds an IEdge element to the graph
     *
     * @param edge
     * @return
     */
    public E addEdge(E edge) throws UnsupportedOperationException;

    /**
     * Creates and Add an IEdge Element to the graph
     *
     * @param id the IEdge Element ID
     * @return
     */
    public E addEdge(String id) throws UnsupportedOperationException;

    /**
     * Add an IVertex Element to The Graph
     *
     * @param vertex
     * @return
     */
    public V addVertex(V vertex) throws UnsupportedOperationException;

    /**
     * Creates and Add an IVertex Elment to the Graph
     *
     * @param id
     * @return
     */
    public V addVertex(String id) throws UnsupportedOperationException;

    /**
     * Retrives the IEdge element by ID
     *
     * @param id
     * @return
     */
    public E getEdgeById(String id);

    /**
     * Retrives the IVertex element by ID
     *
     * @param id
     * @return
     */
    public V getVertexById(String id);

    /**
     * Walks The Graph, DFS Simple
     *
     * @param startVertex
     * @return
     */
    public Stream<EdgeList<E>> dfs(V startVertex);

    /**
     *
     * @param startVertex - The Vertex to start dfsing from
     * @param maxDeph - The max deph, 0 means no limit
     * @return
     */
    public Stream<EdgeList<E>> dfs(V startVertex, Integer maxDeph);

    /**
     *
     * @param startVertex - The Vertex to start dfsing from
     * @param maxDeph - The max deph, 0 means no limit
     * @param threadCount - The amount of workers threads to perform the dfs
     * @return
     */
    public Stream<EdgeList<E>> dfs(V startVertex, Integer maxDeph, Integer threadCount);

    public Stream<EdgeList<E>> dfs(V startVertex, V endVertex, Integer maxDeph, Integer threadCount);

    public Stream<EdgeList<E>> dfs(V startVertex, V endVertex);

    /**
     * BFS Example
     *
     * @param startVertex
     * @return
     */
    public Stream<EdgeList<E>> bfs(V startVertex);

    public Stream<EdgeList<E>> bfs(V startVertex, V endVertex);

//    public void setMultiThreaded(Boolean mThread);
    public Boolean isMultiThreaded();

//    public void setThreadCount(int threadCount);
    public IElementProvider<E, V> getProvider();

    public Long getVertexCount();

    public Long getEdgeCount();

    public Integer getMaxQueueUsage();

}
