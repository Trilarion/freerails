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

package freerails.model.track.pathfinding;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * An OpenList for SimpleAStarPathFinder.
 */
public class OpenList implements Serializable {

    private static final long serialVersionUID = 3257282539419611442L;
    private final Map<Integer, OpenListEntry> map = new HashMap<>();
    private final PriorityQueue<OpenListEntry> queue = new PriorityQueue<>();

    public OpenList() {
    }

    void clear() {
        queue.clear();
        map.clear();
    }

    int getF(int node) {
        return map.get(node).f;
    }

    void add(int node, int f) {

        if (map.containsKey(node)) {
            OpenListEntry old = map.get(node);
            queue.remove(old);
            map.remove(node);
        }
        OpenListEntry entry = new OpenListEntry(f, node);
        queue.offer(entry);
        map.put(node, entry);
    }

    boolean contains(int node) {
        return map.containsKey(node);
    }

    int smallestF() {
        OpenListEntry entry = queue.peek();
        return entry.f;
    }

    int popNodeWithSmallestF() {
        OpenListEntry entry = queue.remove();
        int node = entry.node;
        OpenListEntry removed = map.remove(node);

        if (null == removed) {
            // TODO throwing an exception maybe?
            System.out.println("Shizer, size =" + queue.size());
        }
        return node;
    }

    int size() {
        return queue.size();
    }

}