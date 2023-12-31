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

import java.util.concurrent.Future;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * @created 29.10.2023
 */
public interface IGraphProcessManager {

    public boolean hasStarted();

    public void setStarted();

    public boolean isRunning();
  
    public boolean isDone();

    public String notifySubprocessStarted();

    public Long getActiveSubProcessRunning();

    public boolean notifySubProcessEnd(String uid);
    
    public boolean isSubProcessRunning(String uid);
    
    public void registerChildThread(Future<?> f);
    
    /**
     * Resets the process manager to its initial state
     */
    public void reset();
    
    
//    public void notifyLastMsg(String uid, String msg);

}
