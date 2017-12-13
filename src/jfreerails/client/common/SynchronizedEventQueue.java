/*
 * Copyright (C) Luke Lindsay
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

package jfreerails.client.common;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;


/**
 * This event queue is synchronized on the MUTEX. This lets one control when events can be dispatched.
 *
 * Note, changed to be a singleton to get it working on pre 1.4.2 VMs.
 *
 * @author Luke
 *
 */
final public class SynchronizedEventQueue extends EventQueue {
    public static final Object MUTEX = new Object();
    private static SynchronizedEventQueue instance = new SynchronizedEventQueue();
    private static boolean alreadyInUse = false;

    /** Enforce singleton property */
    private SynchronizedEventQueue() {
    }

    public static synchronized void use() {
        if (!alreadyInUse) {
            /* set up the synchronized event queue */
            EventQueue eventQueue = Toolkit.getDefaultToolkit()
                                           .getSystemEventQueue();
            eventQueue.push(instance);
            alreadyInUse = true;
        }
    }

    protected void dispatchEvent(AWTEvent aEvent) {
        synchronized (MUTEX) {
            super.dispatchEvent(aEvent);
        }
    }
}