package jfreerails.client.common;

import java.util.Vector;

import java.awt.AWTEvent;
import java.awt.EventQueue;

/**
 * This event queue is synchronized on all objects which are handed to it via
 * addMutex().  This lets one control when events can be dispatched.
 * @author Luke
 *
 */
final public class SynchronizedEventQueue extends EventQueue implements
MultiLockedRegion {
   
   private Object[] mutexCache = new Object[0];

    private Vector mutexes = new Vector();
    
    private AWTEvent event;

    public void addMutex(Object mutex) {
	synchronized (mutexes) {
	    mutexes.add(mutex);
	    mutexCache = mutexes.toArray();
	}
    }

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
