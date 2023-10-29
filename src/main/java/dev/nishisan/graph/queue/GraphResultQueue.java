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
package dev.nishisan.graph.queue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This queue was created so we can monitor the usage of it capacity during
 * processing
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * created 27.10.2023
 */
public class GraphResultQueue<T> extends LinkedBlockingQueue<T> {

    private AtomicInteger maxObjectOnQueue = new AtomicInteger(0);
    private AtomicLong addedObjectCount = new AtomicLong(0);

    public GraphResultQueue() {
    }

    public GraphResultQueue(int capacity) {
        super(capacity);
    }

    @Override
    public void put(T e) throws InterruptedException {
        int size = this.size();
        if (size > maxObjectOnQueue.get()) {
            this.maxObjectOnQueue.set(size);
        }
        super.put(e);
        if (e != null) {
            addedObjectCount.incrementAndGet();
        }
    }

    @Override
    public T take() throws InterruptedException {
        return super.take();
    }

    public Integer getMaxUsedCapacity() {
        return this.maxObjectOnQueue.get();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        T result = super.poll(timeout, unit);
        return result;
    }

    public Long getTotalObjectAdded() {
        return this.addedObjectCount.get();
    }
}
