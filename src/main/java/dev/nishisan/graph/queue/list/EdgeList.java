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
package dev.nishisan.graph.queue.list;

import dev.nishisan.graph.elements.IEdge;
import dev.nishisan.graph.elements.IVertex;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * created 30.10.2023
 * @param <E>
 */
public class EdgeList<E extends IEdge<?,? extends IVertex<?>>> extends ArrayList<E> {

    public EdgeList() {
    }

    public EdgeList(Collection<? extends E> c) {
        super(c);
    }

    /**
     * This returns the unique list of vertexes in the edge list
     *
     * @return
     */
    public VertexList<?> getVertices() {
        VertexList result = new VertexList();
        if (!this.isEmpty()) {
            this.forEach(e -> {
                if (!result.contains(e.getFrom())) {
                    result.add(e.getFrom());
                }
                if (!result.contains(e.getTo())) {
                    result.add(e.getTo());
                }
            });
        }
        return result;
    }

}
