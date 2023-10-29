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
package dev.nishisan.graph;

import dev.nishisan.graph.elements.IEdge;
import dev.nishisan.graph.elements.IVertex;
import dev.nishisan.graph.providers.IElementProvider;
import dev.nishisan.graph.queue.GraphResultQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The Abstract Graph With DFS / Walk
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * created 26.10.2023
 * @param <E>
 * @param <V>
 */
public abstract class AbsBaseGraph<E extends IEdge, V extends IVertex> implements IGraph<E, V> {

    private Boolean isMultiThreaded = false;

    private final AtomicLong iterationCounter = new AtomicLong(0L);

    private final IElementProvider<E, V> elementProvider;

    private int threadCount = 4;

    private ThreadPoolExecutor threadPool;

    private BlockingQueue<Runnable> threadPoolQueue;

    /**
     * This queue, can lead to memory issues.
     */
    private final GraphResultQueue<List<E>> resultQueue = new GraphResultQueue<>(100);

    private final Set<Future<?>> workerList = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ExecutorService internalThreadPool = Executors.newFixedThreadPool(2);

    public AbsBaseGraph(IElementProvider<E, V> elementProvider) {
        this.elementProvider = elementProvider;
    }

    private void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    private void setMultiThreaded(Boolean mThread) {
        this.isMultiThreaded = mThread;
    }

    @Override
    public Boolean isMultiThreaded() {
        return this.isMultiThreaded;
    }

    @Override
    public Stream<List<E>> walk(V startVertex, Integer maxDeph, Integer threadCount) {
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            runDFS(startVertex, null, maxDeph, threadCount);  // Assume runDFS fills resultQueue
        });

//        Future<?> running = runDFS(startVertex, null, maxDeph, threadCount);
        return generateStream(running);
    }

    @Override
    public Stream<List<E>> walk(V startVertex, Integer maxDeph) {
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            runDFS(startVertex, null, maxDeph, null);
        });

        return generateStream(running);
    }

    @Override
    public Stream<List<E>> walk(V startVertex) {
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            runDFS(startVertex, null, 0, null);
        });
        return generateStream(running);
    }

    @Override
    public Stream<List<E>> walk(V startVertex, V endVertex) {
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            runDFS(startVertex, endVertex, 0, null);
        });
        return generateStream(running);
    }

    @Override
    public Stream<List<E>> walk(V startVertex, V endVertex, Integer maxDeph, Integer threadCount) {
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            runDFS(startVertex, endVertex, 0, null);
        });
        return generateStream(running);
    }

    /**
     * Starts the DFS Sub process
     *
     * @param startVertex
     * @param endVertex
     * @param maxDeph
     * @param threadCount
     * @return
     */
    private Future<?> runDFS(V startVertex, V endVertex, Integer maxDeph, Integer threadCount) {

        if (threadCount != null) {
            this.setMultiThreaded(true);
            this.setThreadCount(threadCount);
            this.initThreadPool();
        } else {
            this.setMultiThreaded(false);
        }

//        this.internalThreadPool.submit(new InternalStatsThread());
        return internalThreadPool.submit(() -> {
            List<E> currentPath = new ArrayList<>();
            Set<V> visitedVertex = Collections.newSetFromMap(new ConcurrentHashMap<>());
            this.dfs(startVertex, endVertex, currentPath, visitedVertex, 0, maxDeph, null, "ANY");
            internalThreadPool.shutdown();
        });
    }

    /**
     * Generate the Result Stream from the Blocking Queue
     *
     * @param running
     * @return
     */
    private Stream<List<E>> generateStream(CompletableFuture running) {

        Iterator<List<E>> iterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                /**
                 * Note we wait for the method to finish //
                 */
                if (!running.isDone()) {
                    return true;
                } else if (!resultQueue.isEmpty()) {
                    return true;
                } else if (isMultiThreaded) {
                    if (threadPool.getActiveCount() > 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            }

            @Override
            public List<E> next() {
                try {
                    return resultQueue.take();  // Wait until an element is available
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        };
        System.out.println("Returning...");
        return StreamSupport.stream(((Iterable<List<E>>) () -> iterator).spliterator(), false);

    }

    /**
     * The DFS Method, it feeds the result Stream
     *
     * @param currentVertex
     * @param endVertex
     * @param currentPath
     * @param visitedVertex
     * @param currentDepth
     * @param maxDepth
     * @param nodeFilter
     * @param direction
     */
    private void dfs(V currentVertex, V endVertex,
            List<E> currentPath, Set<V> visitedVertex,
            int currentDepth, int maxDepth,
            Predicate<E> nodeFilter, String direction) {

        iterationCounter.incrementAndGet();
        try {
            /**
             * Check if had Visited it before
             */
            if (visitedVertex.contains(currentVertex)) {
                /**
                 * Por se tratar de full Path, todas as soluções são aceitaveis
                 */
                if (endVertex == null) {
                    this.resultQueue.put(currentPath);
                }
                return;
            } else {
                visitedVertex.add(currentVertex);
            }

            if (endVertex != null) {
                if (currentVertex.equals(endVertex)) {
                    /**
                     * Here we have found a Solution
                     */
                    /**
                     * Append to the stream queue because is a full walk
                     */
                    this.resultQueue.put(currentPath);
                    return;
                }
            } else {
                /**
                 * This is a WALK, so there is no answer, just keep going
                 * forever and ever
                 */
            }

            /**
             * Check if we can go deeper
             */
            if (maxDepth > 0) {
                if (currentDepth >= maxDepth) {
                    return;
                }
            }

            /**
             * So lets find our neighboors
             */
            List<E> adjacentEdges = this.elementProvider.getAdjacentEdgesFromVertex(currentVertex, direction);
            /**
             * Ok lets check the edges
             */

            adjacentEdges.removeAll(currentPath);
            if (!adjacentEdges.isEmpty()) {
                for (final E edge : adjacentEdges) {

                    final Set<V> newVisitedVertex = new HashSet<>(visitedVertex);
                    final List<E> newPath = new ArrayList<>(currentPath);

                    /**
                     * Here we should send it to the stream as a new path has
                     * been found
                     */
                    if (!newPath.contains(edge)) {
                        newPath.add(edge);
                        if (this.isMultiThreaded()) {
                            /**
                             * Initi Thread pool
                             */
                            this.initThreadPool();

                            /**
                             * Multi Thread DFS Shout Check for Available thread
                             * to start other wise will do in the current thread
                             */
                            if (this.threadPool.getActiveCount() < this.threadCount) {
                                /**
                                 * Can Submit new thread
                                 */
                                Future<?> f = this.threadPool.submit(() -> {
                                    // I dont like the typecast, if someone can improve it will be great
                                    this.dfs((V) edge.getOther(currentVertex), endVertex, newPath, newVisitedVertex, currentDepth + 1, maxDepth, nodeFilter, direction);
                                });
                                workerList.add(f);
                            } else {
                                /**
                                 * Need to reuse the same thread
                                 */

                                // I dont like the typecast, if someone can improve it will be great
                                this.dfs((V) edge.getOther(currentVertex), endVertex, newPath, newVisitedVertex, currentDepth + 1, maxDepth, nodeFilter, direction);
                            }

                        } else {
                            /**
                             * In this case by default will use a single thread
                             */

                            // I dont like the typecast, if someone can improve it will be great
                            this.dfs((V) edge.getOther(currentVertex), endVertex, newPath, newVisitedVertex, currentDepth + 1, maxDepth, nodeFilter, direction);
                        }
                    } else {
                        /**
                         * Already Visited Path
                         */

                    }
                }
            } else {
                /**
                 * Here we've reached a dead end, append the path to the results
                 */

                /**
                 * Append to the stream queue because is a full walk
                 */
                this.resultQueue.put(currentPath);
            }
        } catch (InterruptedException ex) {
            /**
             * Only god know why, and perhaps someone else smarter than me
             */
            Thread.currentThread().interrupt();
        }

    }

    private void initThreadPool() {
        if (this.isMultiThreaded) {
            if (this.threadPool == null) {
                this.threadPoolQueue = new LinkedBlockingQueue<>(this.threadCount + 1); // <- Always 1 plus
                RejectedExecutionHandler blockingHandler = (r, executor) -> {
                    try {

                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                };

                this.threadPool = new ThreadPoolExecutor(
                        this.threadCount,
                        this.threadCount,
                        0L,
                        TimeUnit.MILLISECONDS,
                        this.threadPoolQueue,
                        blockingHandler
                );
            }
        }
    }

    @Override
    public IElementProvider<E, V> getProvider() {
        return this.elementProvider;
    }

    
    public Integer getMaxQueueUsage(){
        return this.resultQueue.getMaxUsedCapacity();
    }
//    private class InternalStatsThread implements Runnable {
//
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    System.out.println("Sent");
//                    resultQueue.add(new ArrayList<>());
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//
//    }
}