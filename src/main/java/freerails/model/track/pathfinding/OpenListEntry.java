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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * A frontier node and its A* score, ordered first by score and then by node ID.
 */
public class OpenListEntry implements Comparable<OpenListEntry>, Serializable {

    private static final long serialVersionUID = -4873508719707382681L;
    final int f;
    final int node;

    /**
     * Creates a frontier entry.
     *
     * @param f A* score
     * @param node encoded node ID
     */
    OpenListEntry(int f, int node) {
        this.f = f;
        this.node = node;
    }

    /**
     * Compares entries by score and then by node ID.
     *
     * @param o entry to compare
     * @return negative, zero, or positive according to the ordering
     */
    @Override
    public int compareTo(@NotNull OpenListEntry o) {
        if (f == o.f) {
            return node - o.node;
        }
        return f - o.f;
    }

    /**
     * Tests equality of both the score and node ID.
     *
     * @param obj object to compare
     * @return {@code true} if both entry values match
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OpenListEntry)) return false;
        final OpenListEntry other = (OpenListEntry) obj;

        return f == other.f && node == other.node;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return entry hash code
     */
    @Override
    public int hashCode() {
        int result = f;
        result = 29 * result + node;
        return result;
    }

    /**
     * Returns a concise description of the node and score.
     *
     * @return textual entry representation
     */
    @Override
    public String toString() {
        return "OpenListEntry{node=" + node + ", f=" + f + '}';
    }

}
