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
package dev.nishisan.graph.elements;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * @param <T>
 * @param <V> created 26.10.2023
 */
public abstract class AbsBaseEdge<T extends Serializable, V extends IVertex<T>> extends AbsBaseElement<T> implements IEdge<T, V> {

    private V from;

    private V to;

    public AbsBaseEdge(String id, T data) {
        super(id, data);
    }

    @Override
    public V getFrom() {
        return this.from;
    }

    @Override
    public V getTo() {
        return this.to;
    }

    @Override
    public void setFrom(V aPoint) {
        this.from = aPoint;
    }

    @Override
    public void setTo(V zPoint) {
        this.to = zPoint;
    }

    @Override
    public V getOther(V point) throws IllegalArgumentException {
        if (point == null) {
            throw new IllegalArgumentException("Point is Null Please Check");
        }
        if (this.from.equals(point) || this.to.equals(point)) {
            if (this.from.equals(point)) {
                return this.to;
            } else {
                return this.from;
            }
        }
        throw new IllegalArgumentException("Vertex ID:[" + point.getId() + "] Not Present in Current Edge");
    }

    @Override
    public boolean contains(V point) {
        if (this.from.equals(point) || this.to.equals(point)) {
            return true;
        }
        return false;
    }

    @Override
    public List<V> getVertices() {
        return List.of(this.from, this.to);
    }

}
