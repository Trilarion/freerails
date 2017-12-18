/*
 * Created on Apr 14, 2004
 */
package freerails.network;

/**
 * Synchronized flag - used to tell threads whether they should keep going.
 * Note, thought about using volatile keyword but wasn't sure if it is
 * implemented on all JVMs
 *
 * @author Luke
 */
class SynchronizedFlag {
    private boolean isOpen = true;

    SynchronizedFlag(boolean b) {
        this.isOpen = b;
    }

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