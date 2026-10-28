/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 * Priority queue of A* frontier nodes, indexed by node ID for efficient cost
 * lookup and replacement.
 */
public class OpenList implements Serializable {

    private static final long serialVersionUID = 3257282539419611442L;
    private final Map<Integer, OpenListEntry> map = new HashMap<>();
    private final PriorityQueue<OpenListEntry> queue = new PriorityQueue<>();

    /**
     * Creates an empty frontier.
     */
    public OpenList() {
    }

    /**
     * Removes all nodes from the frontier.
     */
    void clear() {
        queue.clear();
        map.clear();
    }

    /**
     * Returns the current A* score for a node in the frontier.
     *
     * @param node encoded node ID
     * @return node's score
     * @throws NoSuchElementException if the node is not in the frontier
     */
    int getF(int node) {
        OpenListEntry entry = map.get(node);
        if (entry == null) {
            throw new NoSuchElementException("Node is not in the open list: " + node);
        }
        return entry.f;
    }

    /**
     * Adds a node with its score, replacing any existing frontier entry for it.
     *
     * @param node encoded node ID
     * @param f node's A* score
     */
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

    /**
     * Tests whether a node is in the frontier.
     *
     * @param node encoded node ID
     * @return {@code true} if the node is present
     */
    boolean contains(int node) {
        return map.containsKey(node);
    }

    /**
     * Returns the lowest score currently in the frontier.
     *
     * @return smallest queued score
     * @throws NoSuchElementException if the frontier is empty
     */
    int smallestF() {
        return queue.element().f;
    }

    /**
     * Removes and returns the node with the lowest score.
     *
     * @return node with the smallest score
     * @throws NoSuchElementException if the frontier is empty
     * @throws IllegalStateException if the queue and node index disagree
     */
    int popNodeWithSmallestF() {
        OpenListEntry entry = queue.remove();
        int node = entry.node;
        OpenListEntry removed = map.remove(node);

        if (removed != entry) {
            throw new IllegalStateException("Open-list map and priority queue are inconsistent.");
        }
        return node;
    }

    /**
     * Returns the number of nodes currently in the frontier.
     *
     * @return frontier size
     */
    int size() {
        return queue.size();
    }

}