package jfreerails.client.common;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;


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
     {
	
	public static final Object MUTEX = new Object();    
   
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


    protected void dispatchEvent(AWTEvent aEvent) {
        synchronized (MUTEX) {
           super.dispatchEvent(aEvent);
        }
    }
}