/*
 * Created on Dec 25, 2003
 */
package jfreerails.network;

import java.util.LinkedList;

import jfreerails.world.common.FreerailsSerializable;

/**
 * Intended to let objects be safely passed between threads.
 * 
 * @author Luke
 * 
 */
public class SychronizedQueue {
	private final LinkedList<FreerailsSerializable> queue = new LinkedList<FreerailsSerializable>();

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