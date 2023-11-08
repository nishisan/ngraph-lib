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
package dev.nishisan.graph.queue;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This queue was created so we can monitor the usage of it capacity during
 * processing
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 27.10.2023
 */
public class GraphResultQueue<T> extends LinkedBlockingQueue<T> implements Closeable {

    private final AtomicInteger maxObjectOnQueue = new AtomicInteger(0);
    private final AtomicLong addedObjectCount = new AtomicLong(0);
    private final Thread stats = new Thread(new InternalQueueStats());
    private boolean running = true;

    public GraphResultQueue() {
        stats.start();
    }

    public GraphResultQueue(int capacity) {
        super(capacity);
        stats.start();
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
    public boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
        int size = this.size();
        if (size > maxObjectOnQueue.get()) {
            this.maxObjectOnQueue.set(size);
        }
        boolean result = super.offer(e, timeout, unit);

        if (result) {
            if (e != null) {
                addedObjectCount.incrementAndGet();
            }
        }
        return result;
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

    public void reset() {
        this.maxObjectOnQueue.set(0);
        this.addedObjectCount.set(0);
    }

    @Override
    public void close() throws IOException {
        running = false;
    }

    private class InternalQueueStats implements Runnable {

        private Long lastValue = 0L;
        private Long currentValue = 0L;
        private Long delta = 0L;
        private Long lastTimeStamp = 0L;
        private Long elapsedTime = 0L;
        private Double recPerSeconds = 0D;

        @Override
        public void run() {
            while (running) {

                if (lastTimeStamp > 0) {
                    elapsedTime = System.currentTimeMillis() - lastTimeStamp;
                    currentValue = addedObjectCount.get();
                    delta = currentValue - lastValue;
                    lastValue = currentValue;

                    recPerSeconds = (delta / elapsedTime) * 1000D;
                }

                lastTimeStamp = System.currentTimeMillis();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GraphResultQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
