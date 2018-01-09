/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.util;

import java.io.Serializable;
import java.util.LinkedList;

// TODO perhaps an arrayList is better (-> profile it)

/**
 * Intended to let objects be safely passed between threads.
 */
public class SychronizedQueue {

    private final LinkedList<Serializable> queue = new LinkedList<>();

    /**
     * @param f
     */
    public synchronized void write(Serializable f) {
        queue.add(f);
    }

    /**
     * @return
     */
    public synchronized Serializable[] read() {
        int length = queue.size();
        Serializable[] read = new Serializable[length];

        for (int i = 0; i < length; i++) {
            read[i] = queue.removeFirst();
        }

        return read;
    }

    /**
     * @return
     */
    public synchronized int size() {
        return queue.size();
    }

    /**
     * @return
     */
    public synchronized Serializable getFirst() {
        return queue.removeFirst();
    }
}