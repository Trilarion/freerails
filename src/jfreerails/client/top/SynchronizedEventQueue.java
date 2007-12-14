package jfreerails.client.top;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.LinkedHashMap;
import java.util.Map;

import jfreerails.controller.ReportBugTextGenerator;

/**
 * This event queue is synchronized on the MUTEX. This lets one control when
 * events can be dispatched.
 * 
 * Note, changed to be a singleton to get it working on pre 1.4.2 VMs.
 * 
 * @author Luke
 * 
 */
final public class SynchronizedEventQueue extends EventQueue {
    public static final Object MUTEX = new Object();

    private static final SynchronizedEventQueue instance = new SynchronizedEventQueue();

    private static boolean alreadyInUse = false;

    /** Enforce singleton property. */
    private SynchronizedEventQueue() {
        list = new LinkedHashMap<AWTEvent, Throwable>();
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

    private int count;
    private long last;
    private LinkedHashMap<AWTEvent, Throwable> list;

    public void postEvent(AWTEvent aEvent) {
        synchronized (list) {
            count++;
            list.put(aEvent, new RuntimeException("X"));
            if (System.currentTimeMillis() - last > 1000) {
                last = System.currentTimeMillis();
                // System.out.println(count);
                // System.out.println("Num
                // DirtyReg:"+RepaintManagerForActiveRendering.getNumDirtyRequests());
                // System.out.println("Num
                // Repaints:"+RepaintManagerForActiveRendering.getNumRepaintRequests());
                int i = 10;
                for (Map.Entry<AWTEvent, Throwable> e : list.entrySet()) {
                    // System.out.println(e.getKey().getClass().getCanonicalName()+"/"+e.getKey().getSource().getClass().getCanonicalName());
                    // System.out.println(aEvent.paramString());
                    // e.getValue().printStackTrace();
                    i--;
                    if (i == 0) {
                        break;
                    }

                }
                count = 0;
                list.clear();
            }
        }
        super.postEvent(aEvent);
    }

    @Override
    protected void dispatchEvent(AWTEvent aEvent) {
        synchronized (MUTEX) {
            try {
                super.dispatchEvent(aEvent);
            } catch (Exception e) {
                /*
                 * If something goes wrong, lets kill the game straight away to
                 * avoid hard-to-track-down bugs.
                 */
                ReportBugTextGenerator.unexpectedException(e);
            }
        }
    }

    public static SynchronizedEventQueue getInstance() {
        return instance;
    }
}