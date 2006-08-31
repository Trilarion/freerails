package jfreerails.client.top;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;

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
}