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
package dev.nishisan.graph.listeners;

import dev.nishisan.graph.elements.IEdge;
import dev.nishisan.graph.elements.IVertex;
import java.io.Serializable;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 11.11.2023
 */
public interface IGraphListener<T extends Serializable, V extends IVertex<T, E>, E extends IEdge<T, V>> {

    public void onEdgeAdded(E createdEdge);

    public void onVertexAdded(V createdVertex);

    public void onEdgeVisited(E edge, String visitorId);

    public void onVertexVisited(V vertex, String visitorId);
    
    public String getId();
}
