package jfreerails.client.common;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.Vector;


/**
 * This event queue is synchronized on all objects which are handed to it via
 * addMutex().  This lets one control when events can be dispatched.
 *
 * Note, changed to be a singleton to get it working on pre 1.4.2 VMs.
 *
 * @author Luke
 *
 */
final public class SynchronizedEventQueue extends EventQueue
    implements MultiLockedRegion {
    private Object[] mutexCache = new Object[0];
    private Vector mutexes = new Vector();
    private AWTEvent event;
    private static SynchronizedEventQueue instance = new SynchronizedEventQueue();
    private static boolean alreadyInUse = false;

    /** Enforce singleton property */
    private SynchronizedEventQueue() {
    }

    public static SynchronizedEventQueue getInstance() {
        return instance;
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

    /**
     * @deprecated
     */
    public void addMutex(Object mutex) {
        synchronized (mutexes) {
            mutexes.add(mutex);
            mutexCache = mutexes.toArray();
        }
    }

    /**
    * @deprecated
    */
    public void removeMutex(Object mutex) {
        synchronized (mutexes) {
            mutexes.remove(mutex);
            mutexCache = mutexes.toArray();
        }
    }

    public void multiLockedCallback() {
        super.dispatchEvent(event);
    }

    /**
     * Locks all the locks held by this SynchronizedEventQueue and calls the
     * callback in mlr
     * @param mlr MultiLockedRegion which implements code which must be
     * protected.
     */
    public void grabAllLocks(MultiLockedRegion mlr) {
        synchronized (mutexes) {
            dispatchEventImpl(mlr, 0);
        }
    }

    private void dispatchEventImpl(MultiLockedRegion mlr, int i) {
        if (i < mutexCache.length) {
            synchronized (mutexCache[i]) {
                dispatchEventImpl(mlr, i + 1);
            }
        } else {
            /* we got all the mutexes */
            mlr.multiLockedCallback();
        }
    }

    protected void dispatchEvent(AWTEvent aEvent) {
        synchronized (mutexes) {
            event = aEvent;
            dispatchEventImpl(this, 0);
        }
    }
}