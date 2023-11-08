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
import dev.nishisan.graph.processmanager.IGraphProcessManager;
import dev.nishisan.graph.processmanager.SimpleProcessManager;
import dev.nishisan.graph.providers.IElementProvider;
import dev.nishisan.graph.queue.list.EdgeList;
import dev.nishisan.graph.queue.GraphResultQueue;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
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
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 26.10.2023
 * @param <E>
 * @param <V>
 */
public abstract class AbstractGraph<T extends Serializable, E extends IEdge<T, V>, V extends IVertex<T, E>> implements IGraph<T, V, E> {

    /**
     * This flag tells if the graph should be multi threaded or not
     */
    private Boolean isMultiThreaded = false;

    /**
     * The process manager that take cares of the result queue
     */
    private IGraphProcessManager processManager = new SimpleProcessManager();

    /**
     * The iterator counter for the dfs process
     */
    private final AtomicLong iterationCounter = new AtomicLong(0L);

    /**
     * The element provider
     */
    private final IElementProvider<T, E, V> elementProvider;

    /**
     * The default threads workers
     */
    private int threadCount = 4;

    /**
     * THe thread pool executor
     */
    private ThreadPoolExecutor threadPool;

    /**
     * The blocking queue used in the internal thead pool
     */
    private BlockingQueue<Runnable> threadPoolQueue;

    /**
     * This queue, can lead to memory issues.
     */
    private final GraphResultQueue<EdgeList<T, V, E>> resultQueue;

    /**
     * Interna Thread Pool
     */
    private ExecutorService internalThreadPool = Executors.newFixedThreadPool(2);

    /**
     * Creates an Instance of the graph with the element provider
     *
     * @param elementProvider
     */
    public AbstractGraph(IElementProvider<T, E, V> elementProvider) {
        this.elementProvider = elementProvider;
        resultQueue = new GraphResultQueue<>();
    }

    /**
     * Creates an Instance with the ElementProvider and set the capacity of the
     * internal resultQueue
     *
     * @param elementProvider
     * @param queueCapacity
     */
    public AbstractGraph(IElementProvider<T, E, V> elementProvider, int queueCapacity) {
        this.elementProvider = elementProvider;
        resultQueue = new GraphResultQueue<>(queueCapacity);
    }

    /**
     * Sets the worker thread capacity
     *
     * @param threadCount
     */
    private void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * sets if the graph is multi threaded
     *
     * @param mThread
     */
    private void setMultiThreaded(Boolean mThread) {
        this.isMultiThreaded = mThread;
    }

    /**
     * Returns what ever the graph is or not multithreaded
     *
     * @return
     */
    @Override
    public Boolean isMultiThreaded() {
        return this.isMultiThreaded;
    }

    /**
     * Start a DFS process from a start vertex/node, this method will use a
     * multithread aproach.
     *
     * @param startVertex - The start vertex/node
     * @param maxDepth - The max depth the search will go, 0 means no limit
     * @param threadCount - How many worker threads can work on this task
     * @return - A stream of list of edges found in the process
     */
    @Override
    public Stream<EdgeList<T, V, E>> dfs(V startVertex, Integer maxDepth, Integer threadCount) {

        /**
         * This will create the Callee Thread...
         */
        this.resultQueue.reset();
        this.processManager.reset();
        this.processManager.setStarted();

        CompletableFuture running = CompletableFuture.runAsync(() -> {
            /**
             * This will create the processing thread
             */
            runDFS(startVertex, null, maxDepth, threadCount);

        });

        return generateStream(running);

    }

    /**
     * Starts a DFS task from a given vertex/node limiting the depth,this
     * aproach use a single thread task
     *
     * @param startVertex - The start vertex/node
     * @param maxDepth - The max Depth limit
     * @return - A stream of list of edges found in the process
     */
    @Override
    public Stream<EdgeList<T, V, E>> dfs(V startVertex, Integer maxDepth) {

        this.resultQueue.reset();
        this.processManager.reset();
        this.processManager.setStarted();
        /**
         * This will create the Callee Thread...
         */
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            /**
             * This will create the processing thread
             */
            runDFS(startVertex, null, maxDepth, null);
        });
        return generateStream(running);
    }

    /**
     * Perform a simple single thread dfs in the graph
     *
     * @param startVertex
     * @return - A stream of list of edges found in the process
     */
    @Override
    public Stream<EdgeList<T, V, E>> dfs(V startVertex) {

        this.resultQueue.reset();
        this.processManager.reset();
        this.processManager.setStarted();
        /**
         * This will create the Callee Thread...
         */

        CompletableFuture running = CompletableFuture.runAsync(() -> {
            /**
             * This will create the processing thread
             */
            runDFS(startVertex, null, 0, null);

        });
        return generateStream(running);
    }

    /**
     * Search for all possible paths between startVertex and endVertex. Single
     * Thread mode
     *
     * @param startVertex - The startVertex , where the dfs is going to start
     * @param endVertex - The endVertex , we will look for
     * @return - A Stream with all possible edges between startVertex and
     * endVertex
     */
    @Override
    public Stream<EdgeList<T, V, E>> dfs(V startVertex, V endVertex) {

        this.resultQueue.reset();
        this.processManager.reset();
        this.processManager.setStarted();
        /**
         * This will create the Callee Thread...
         */
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            /**
             * This will create the processing thread
             */
            runDFS(startVertex, endVertex, 0, null);

        });

        return generateStream(running);
    }

    @Override
    public Stream<EdgeList<T, V, E>> dfs(V startVertex, V endVertex, Integer maxDepth, Integer threadCount) {

        this.resultQueue.reset();
        this.processManager.reset();
        this.processManager.setStarted();
        /**
         * This will create the Callee Thread...
         */
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            /**
             * This will create the processing thread
             */
            runDFS(startVertex, endVertex, 0, null);

        });
        return generateStream(running);
    }

    @Override
    public Stream<EdgeList<T, V, E>> bfs(V startVertex) {

        this.resultQueue.reset();
        this.processManager.reset();
        this.processManager.setStarted();

        CompletableFuture running = CompletableFuture.runAsync(() -> {
            /**
             * This will create the processing thread
             */
            runBfs(startVertex, null, null);

        });
        return generateStream(running);
    }

    @Override
    public Stream<EdgeList<T, V, E>> bfs(V startVertex, V endVertex) {
        CompletableFuture running = CompletableFuture.runAsync(() -> {
            /**
             * This will create the processing thread
             */
            runBfs(startVertex, endVertex, null);

        });
        return generateStream(running);
    }

    private Future<?> runBfs(V startVertex, V endVertex, Integer threadCount) {
        this.processManager.reset();
        /**
         * Notify Process Manager a Process has Started
         */
        this.processManager.setStarted();

        if (threadCount != null) {
            this.setMultiThreaded(true);
            this.setThreadCount(threadCount);
            this.initThreadPool();
        } else {
            this.setMultiThreaded(false);
        }
        /**
         * Here a new DFS Thread is created...
         */
        return internalThreadPool.submit(() -> {
            Thread.currentThread().setName("RUNBFS");
            this.localBfs(startVertex, endVertex);
            internalThreadPool.shutdown();
        });
    }

    /**
     * Starts the DFS Sub process
     *
     * @param startVertex
     * @param endVertex
     * @param maxDepth
     * @param threadCount
     * @return
     */
    private Future<?> runDFS(V startVertex, V endVertex, Integer maxDepth, Integer threadCount) {
        /**
         * Notify Process Manager a Process has Started
         */

        if (threadCount != null) {
            this.setMultiThreaded(true);
            this.setThreadCount(threadCount);
            this.initThreadPool();
        } else {
            this.setMultiThreaded(false);
        }

        if (internalThreadPool.isShutdown()) {
            this.internalThreadPool = Executors.newFixedThreadPool(2);
        }

        /**
         * Here a new DFS Thread is created...
         */
        return internalThreadPool.submit(() -> {
            Thread.currentThread().setName("RUNDFS");
            EdgeList<T, V, E> currentPath = new EdgeList<>();
            Set<V> visitedVertex = Collections.newSetFromMap(new ConcurrentHashMap<>());
            this.dfs(startVertex, endVertex, currentPath, visitedVertex, 0, maxDepth, null, "ANY");
            internalThreadPool.shutdown();
        });
    }

    /**
     * Generate the Result Stream from the Blocking Queue
     *
     * @param running
     * @return
     */
    private Stream<EdgeList<T, V, E>> generateStream(CompletableFuture<?> running) {

        Iterator<EdgeList<T, V, E>> iterator = new Iterator<>() {

            @Override
            public boolean hasNext() {
                /**
                 * Note we wait for the method to finish and the queue to be
                 * empty
                 */
                boolean result = false;
                if (resultQueue.isEmpty()) {
                    if (processManager.isRunning()) {
                        return true;
                    }
                } else {
                    return true;
                }

                if (!result) {
                    try {
                        if (!resultQueue.isEmpty()) {
                            return true;
                        }
                        if (processManager.isRunning()) {
                            return true;
                        }
                        if (!internalThreadPool.isShutdown()) {
                            return true;
                        }
                        //
                        // Will return false.
                        //
                        resultQueue.close();
                    } catch (IOException ex) {

                    }
                }
                return result;
            }

            @Override
            public EdgeList<T, V, E> next() {
                try {
                    EdgeList<T, V, E> r = null;
                    AtomicLong dynamicTimeout = new AtomicLong(1);
                    AtomicLong missFiredCounter = new AtomicLong(1);
                    while (r == null) {
                        /**
                         * Still not perfect but avoids CPU sparks...
                         */

                        r = resultQueue.poll(dynamicTimeout.get(), TimeUnit.MILLISECONDS);

                        if (processManager.isDone()) {
                            if (r != null) {
                                 missFiredCounter.set(0);
                                 dynamicTimeout.set(1);
                                return r;
                            } else {
                                missFiredCounter.incrementAndGet();
                                if (missFiredCounter.get() > 5) {
                                    if (dynamicTimeout.get() < 10) {
                                        dynamicTimeout.incrementAndGet();
                                    }
                                }

                            }
                            return null;
                        }
                    }
                    return r;  // Wait until an element is available
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Returning Null 1");
                    return null;
                }
            }
        };

        return StreamSupport.stream(((Iterable<EdgeList<T, V, E>>) () -> iterator).spliterator(), false).filter(Objects::nonNull);
    }

    /**
     * Run Local BFS Implementation
     *
     * @param startVertex
     * @param endVertex
     */
    private void localBfs(V startVertex, V endVertex) {
        ConcurrentLinkedQueue<V> queue = new ConcurrentLinkedQueue<>();
        Set<V> visited = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Map<V, E> edgeTo = new ConcurrentHashMap<>();  // to track the edge to each vertex
        List<E> shortestPathEdges = new CopyOnWriteArrayList<>();

        queue.add(startVertex);
        visited.add(startVertex);

        while (!queue.isEmpty()) {
            if (this.isMultiThreaded()) {
                //
                // Multi Thread Implementation
                //
                if (this.threadPool.getActiveCount() < this.threadCount) {
                    /**
                     * Can Submit new thread
                     */
                    Future<?> f = this.threadPool.submit(() -> {
                        // I dont like the typecast, if someone can improve it will be great
                        V currentVertex = queue.poll();

                        // Check if endVertex is found (and endVertex is not null)
                        if (endVertex != null && currentVertex.equals(endVertex)) {
                            for (V at = endVertex; at != null && !at.equals(startVertex); at = (V) edgeTo.get(at).getOther(at)) {
                                shortestPathEdges.add(edgeTo.get(at));
                            }
                            Collections.reverse(shortestPathEdges);  // reverse the edges to get the correct order
                            this.resultQueue.add(new EdgeList<>(shortestPathEdges));
                        }
                        if (currentVertex != null) {
                            EdgeList<T, V, E> edges = this.elementProvider.getAdjacentEdgesFromVertex(currentVertex, "ANY");
                            for (E edge : edges) {
                                V neighbor = (V) edge.getOther(currentVertex);
                                if (!visited.contains(neighbor)) {
                                    edgeTo.put(neighbor, edge);  // track the edge to the neighbor
                                    queue.add(neighbor);
                                    visited.add(neighbor);
                                }
                            }
                        }

                    });
                    processManager.registerChildThread(f);
                } else {
                    /**
                     * Need to reuse the same thread, no more threads allowed
                     */
                    V currentVertex = queue.poll();

                    // Check if endVertex is found (and endVertex is not null)
                    if (endVertex != null && currentVertex.equals(endVertex)) {
                        for (V at = endVertex; at != null && !at.equals(startVertex); at = (V) edgeTo.get(at).getOther(at)) {
                            shortestPathEdges.add(edgeTo.get(at));
                        }
                        Collections.reverse(shortestPathEdges);  // reverse the edges to get the correct order
                        this.resultQueue.add(new EdgeList<>(shortestPathEdges));
                        return;
                    }
                    if (currentVertex != null) {
                        EdgeList<T, V, E> edges = this.elementProvider.getAdjacentEdgesFromVertex(currentVertex, "ANY");
                        for (E edge : edges) {

                            V neighbor = (V) edge.getOther(currentVertex);
                            if (!visited.contains(neighbor)) {
                                edgeTo.put(neighbor, edge);  // track the edge to the neighbor
                                queue.add(neighbor);
                                visited.add(neighbor);
                            }
                        }
                    }

                }

            } else {
                //
                // Single Thread BFS
                //
                V currentVertex = queue.poll();

                // Check if endVertex is found (and endVertex is not null)
                if (endVertex != null && currentVertex.equals(endVertex)) {
                    for (V at = endVertex; at != null && !at.equals(startVertex); at = (V) edgeTo.get(at).getOther(at)) {
                        shortestPathEdges.add(edgeTo.get(at));
                    }
                    Collections.reverse(shortestPathEdges);  // reverse the edges to get the correct order
                    this.resultQueue.add(new EdgeList<>(shortestPathEdges));
                    return;
                }
                if (currentVertex != null) {
                    EdgeList<T, V, E> edges = this.elementProvider.getAdjacentEdgesFromVertex(currentVertex, "ANY");
                    for (E edge : edges) {
                        V neighbor = (V) edge.getOther(currentVertex);
                        if (!visited.contains(neighbor)) {
                            edgeTo.put(neighbor, edge);  // track the edge to the neighbor
                            queue.add(neighbor);
                            visited.add(neighbor);
                        }
                    }
                }

            }
        }

        if (endVertex == null) {
            this.resultQueue.add(new EdgeList<>(shortestPathEdges));
        } else {
            this.resultQueue.add(new EdgeList<>(shortestPathEdges));
        }
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
    private synchronized void dfs(V currentVertex, V endVertex,
            EdgeList<T, V, E> currentPath, Set<V> visitedVertex,
            int currentDepth, int maxDepth,
            Predicate<E> nodeFilter, String direction) {
        /**
         * Notify process manager of the sub process
         */
        String uidInstance = this.processManager.notifySubprocessStarted();
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
//                    this.processManager.notifyLastMsg(uidInstance, "1");
                    this.resultQueue.put(currentPath);
//                    this.processManager.notifyLastMsg(uidInstance, "1.1");
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
//                    this.processManager.notifyLastMsg(uidInstance, "2");
                    this.resultQueue.put(currentPath);
//                    this.processManager.notifyLastMsg(uidInstance, "2.1");
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
//            this.processManager.notifyLastMsg(uidInstance, "A");
            EdgeList<T, V, E> adjacentEdges = this.elementProvider.getAdjacentEdgesFromVertex(currentVertex, direction);
//            this.processManager.notifyLastMsg(uidInstance, "A.1");
            /**
             * Ok, lets check the edges
             */
//            this.processManager.notifyLastMsg(uidInstance, "B");
            adjacentEdges.removeAll(currentPath);
//            this.processManager.notifyLastMsg(uidInstance, "B.1");
            if (!adjacentEdges.isEmpty()) {
                for (final E edge : adjacentEdges) {

                    final Set<V> newVisitedVertex = new HashSet<>(visitedVertex);
                    final EdgeList<T, V, E> newPath = new EdgeList<>(currentPath);

                    /**
                     * Here we should send it to the stream as a new path has
                     * been found
                     */
                    if (!newPath.contains(edge)) {
                        newPath.add(edge);
                        if (this.isMultiThreaded()) {

                            /**
                             * Multi Thread DFS Should Check for Available
                             * thread to start other wise will do in the current
                             * thread
                             */
//                            if (threadControlLock.tryLock(10, TimeUnit.MILLISECONDS)) {
                            if (this.threadPool.getActiveCount() < this.threadCount - 1) {
                                /**
                                 * Can Submit new thread
                                 */
//                                this.processManager.notifyLastMsg(uidInstance, "C");
                                Future<?> f = this.threadPool.submit(() -> {
                                    this.dfs(edge.getOther(currentVertex), endVertex, newPath, newVisitedVertex, currentDepth + 1, maxDepth, nodeFilter, direction);
                                });
//                                this.processManager.notifyLastMsg(uidInstance, "C.1");
                                processManager.registerChildThread(f);

                            } else {
                                /**
                                 * Need to reuse the same thread
                                 */
//                                this.processManager.notifyLastMsg(uidInstance, "D");
                                this.dfs(edge.getOther(currentVertex), endVertex, newPath, newVisitedVertex, currentDepth + 1, maxDepth, nodeFilter, direction);
//                                this.processManager.notifyLastMsg(uidInstance, "D.1");
                            }
                        } else {
                            /**
                             * Need to reuse the same thread because we didnt
                             * get a lock on the control thread
                             */
//                            this.processManager.notifyLastMsg(uidInstance, "E");
                            this.dfs(edge.getOther(currentVertex), endVertex, newPath, newVisitedVertex, currentDepth + 1, maxDepth, nodeFilter, direction);
//                            this.processManager.notifyLastMsg(uidInstance, "E.1");
                        }

//                        } else {
//                            /**
//                             * In this case by default will use a single thread
//                             */
//
//                            this.dfs(edge.getOther(currentVertex), endVertex, newPath, newVisitedVertex, currentDepth + 1, maxDepth, nodeFilter, direction);
//                        }
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
//                this.processManager.notifyLastMsg(uidInstance, "3");
                this.resultQueue.put(currentPath);
//                this.processManager.notifyLastMsg(uidInstance, "3.1");
            }
        } catch (InterruptedException ex) {
            /**
             * Only god know why, and perhaps someone else smarter than me
             */
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            if (!this.processManager.notifySubProcessEnd(uidInstance)) {
                System.out.println("Failed to remove: [" + uidInstance + "]");
            }
        }

    }

    /**
     * Initialize the internal thread pool
     */
    private void initThreadPool() {
        if (this.isMultiThreaded) {

            if (this.threadPool != null) {
                if (this.threadPool.getActiveCount() > 0 || !this.threadPoolQueue.isEmpty()) {
                    System.out.println("!!!! Warning, incomplete Jobs overrides....");
                }
            }
            int capacity = (this.threadCount * 2) + 1;
            this.threadPoolQueue = new LinkedBlockingQueue<>(capacity); // <- Always 1 plus
            RejectedExecutionHandler blockingHandler = (r, executor) -> {
                try {

//                    while (!executor.getQueue().offer(r, 100, TimeUnit.MILLISECONDS)) {
//                        System.out.println("Retrying::" + executor.getQueue().size() + "/" + capacity);
//                    }
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

    /**
     * Return the element provider object
     *
     * @return
     */
    @Override
    public IElementProvider<T, E, V> getProvider() {
        return this.elementProvider;
    }

    /**
     * For statistics will return the max capacity used by the result queue
     *
     * @return
     */
    @Override
    public Integer getMaxQueueUsage() {
        return this.resultQueue.getMaxUsedCapacity();
    }
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
//}
