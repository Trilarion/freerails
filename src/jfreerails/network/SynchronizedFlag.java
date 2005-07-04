/*
 * Created on Apr 14, 2004
 */
package jfreerails.network;

/**
 * Synchronized flag - used to tell threads whether they should keep going.
 * Note, thought about using volatile keyword but wasn't sure if it is
 * implemented on all JVMs
 * 
 * @author Luke
 */
class SynchronizedFlag {
	SynchronizedFlag(boolean b) {
		this.isOpen = b;
	}

	private boolean isOpen = true;

	public synchronized boolean isOpen() {
		return isOpen;
	}

	public synchronized void close() {
		this.isOpen = false;
		notifyAll();
	}

	public synchronized void open() {
		this.isOpen = true;
		notifyAll();
	}
}