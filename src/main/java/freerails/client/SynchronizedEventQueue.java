/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client;

import freerails.client.launcher.LauncherFrame;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO use some modern Java for it, make it independent of the Launcher
/**
 * This event queue is synchronized on the MUTEX. This lets one control when
 * events can be dispatched.
 * <p>
 * Note, changed to be a singleton to get it working on pre 1.4.2 VMs.
 */
public class SynchronizedEventQueue extends EventQueue {

    private static final TaggedLogger logger = Logger.tag("SynchronizedEventQueue");
    public static final Object MUTEX = new Object();
    private static final SynchronizedEventQueue instance = new SynchronizedEventQueue();
    private static boolean alreadyInUse = false;
    private final LinkedHashMap<AWTEvent, Throwable> list;
    private int count;
    private long last;

    /**
     * Enforce singleton property.
     */
    private SynchronizedEventQueue() {
        list = new LinkedHashMap<>();
    }

    /**
     * Performs the use operation.
     */
    public static synchronized void use() {
        if (!alreadyInUse) {
            // set up the synchronized event queue
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eventQueue.push(instance);
            alreadyInUse = true;
        }
    }

    /**
     * Returns the instance.
     *
     * @return the instance produced by this method
     */
    public static SynchronizedEventQueue getInstance() {
        return instance;
    }

    /**
     * Post event.
     * @param theEvent the the event
     */
    @Override
    public void postEvent(AWTEvent theEvent) {
        synchronized (list) {
            count++;
            list.put(theEvent, new RuntimeException("X"));
            if (System.currentTimeMillis() - last > 1000) {
                last = System.currentTimeMillis();
                int i = 10;
                for (Map.Entry<AWTEvent, Throwable> e : list.entrySet()) {
                    i--;
                    if (i == 0) {
                        break;
                    }
                }
                count = 0;
                list.clear();
            }
        }
        super.postEvent(theEvent);
    }

    /**
     * Dispatch event.
     * @param event the event
     */
    @Override
    protected void dispatchEvent(AWTEvent event) {
        synchronized (MUTEX) {
            try {
                super.dispatchEvent(event);
            } catch (Exception e) {
                /*
                 * If something goes wrong, lets kill the game straight away to
                 * avoid hard-to-track-down bugs.
                 */
                logger.error(e, "Unexpected exception while dispatching an event.");
                LauncherFrame.emergencyStop();
            }
        }
    }
}