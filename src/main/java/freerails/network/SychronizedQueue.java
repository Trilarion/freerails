/*
 * Created on Dec 25, 2003
 */
package freerails.network;

import freerails.world.common.FreerailsSerializable;

import java.util.LinkedList;

/**
 * Intended to let objects be safely passed between threads. 666 perhaps an
 * arrayList is better (-> profile it)
 *
 * @author Luke
 */
public class SychronizedQueue {
    private final LinkedList<FreerailsSerializable> queue = new LinkedList<>();

    public synchronized void write(FreerailsSerializable f) {
        queue.add(f);
    }

    public synchronized FreerailsSerializable[] read() {
        int length = queue.size();
        FreerailsSerializable[] read = new FreerailsSerializable[length];

        for (int i = 0; i < length; i++) {
            read[i] = queue.removeFirst();
        }

        return read;
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized FreerailsSerializable getFirst() {
        return queue.removeFirst();
    }
}