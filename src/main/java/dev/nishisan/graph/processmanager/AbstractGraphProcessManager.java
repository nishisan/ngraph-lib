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
package dev.nishisan.graph.processmanager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * @created 29.10.2023
 */
public class AbstractGraphProcessManager implements IGraphProcessManager {

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String, Boolean> subProcesses = new ConcurrentHashMap<>();
    private final AtomicLong futureId = new AtomicLong(0);
    private final Map<Long, Future<?>> childThreads = new ConcurrentHashMap<>();
    private final AtomicBoolean isDone = new AtomicBoolean(false);
    private final AtomicBoolean isFirst = new AtomicBoolean(true);
    private final Thread statsThread = new Thread(new InternalStatsThread(), "ProcessManagerThread");
    private String lastUid = "";
    private String lastMsg = "";

//    @Override
//    public void notifyLastMsg(String uid, String msg) {
//        this.lastMsg = msg;
//        this.lastUid = uid;
//
//    }
    public AbstractGraphProcessManager() {
        /**
         * Auto Start the Internal Process Thread
         */
//        statsThread.start();
    }

    @Override
    public boolean hasStarted() {
        return started.get();
    }

    @Override
    public void setStarted() {
        if (!this.isDone.get()) {
            started.set(true);
        }
    }

    @Override
    public boolean isRunning() {
        if (isFirst.get()) {
            isFirst.set(false);
            return true;
        }
        if (this.started.get()) {
            if (!this.childThreads.isEmpty()) {
                boolean allDone = childThreads.values().stream().allMatch(Future::isDone);
                if (!allDone) {
                    if (this.subProcesses.isEmpty()) {
                        return true;
                    }
                }
            }

            if (!this.subProcesses.isEmpty()) {
                return true;
            }

            if (this.subProcesses.isEmpty()) {
                if (childThreads.values().stream().allMatch(Future::isDone)) {
                    this.setDone();
                }
            }
            /**
             * May Return False
             */
            return !this.isDone.get();
        } else {
            return true;
        }
    }

    private void setDone() {
        if (this.started.get()) {
            this.isDone.set(true);
        }
    }

    @Override
    public boolean isDone() {
        return this.isDone.get();
    }

    @Override
    public String notifySubprocessStarted() {
        String uuid = UUID.randomUUID().toString();
        subProcesses.put(uuid, true);
        if (this.isDone.get()) {
            this.isDone.set(false);
        }
        return uuid;
    }

    @Override
    public Long getActiveSubProcessRunning() {
        return Long.valueOf(this.subProcesses.size());
    }

    @Override
    public boolean notifySubProcessEnd(String uid) {
        if (this.subProcesses.containsKey(uid)) {
            this.subProcesses.remove(uid);
            if (this.subProcesses.isEmpty()) {
                //
                // 
                //
                if (!this.isDone.get()) {
                    this.isDone.set(true);
                }

                
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isSubProcessRunning(String uid) {
        if (this.subProcesses.containsKey(uid)) {
            return this.subProcesses.get(uid);
        } else {
            return false;
        }
    }

    @Override
    public void registerChildThread(Future<?> f) {
        this.childThreads.put(futureId.incrementAndGet(), f);
    }

    @Override
    public void reset() {
        this.started.set(false);
        this.subProcesses.clear();
        this.futureId.set(0);
        this.childThreads.clear();
        this.isDone.set(false);
        this.isFirst.set(true);
    }

    private class InternalStatsThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                /**
                 * Compute Processes Stats
                 */
                System.out.println("----------------- Process Manager Stats ----------------- ");
                System.out.println("Total Subprocess Count: " + subProcesses.size());
                System.out.println("Active Child Thread: " + childThreads.values().stream().filter(f -> !f.isDone()).count());
//                System.out.println("Is Running: " + isRunning());
                System.out.println("Is First: " + isFirst.get());
                System.out.println("Is Done: " + isDone.get());
                System.out.println("last msg: [" + lastMsg + "] From:[" + lastUid + "]");

                System.out.println("--------------------------------------------------------- ");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
