/*
 * Created on Dec 25, 2003
 */
package jfreerails.controller;

import java.util.LinkedList;
import jfreerails.world.common.FreerailsSerializable;


/**
 *  Intended to let objects be safely passed between threads.
 *
 *  @author Luke
 *
 */
public class SychronizedQueue {
    private final LinkedList queue = new LinkedList();

    public synchronized void write(FreerailsSerializable f) {
        queue.add(f);
    }

    public synchronized FreerailsSerializable[] read() {
        int length = queue.size();
        FreerailsSerializable[] read = new FreerailsSerializable[length];

        for (int i = 0; i < length; i++) {
            read[i] = (FreerailsSerializable)queue.removeFirst();
        }

        return read;
    }
}