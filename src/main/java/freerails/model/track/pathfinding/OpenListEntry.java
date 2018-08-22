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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class OpenListEntry implements Comparable<OpenListEntry>, Serializable {

    private static final long serialVersionUID = -4873508719707382681L;
    final int f;
    final int node;

    OpenListEntry(int f, int node) {
        this.f = f;
        this.node = node;
    }

    @Override
    public int compareTo(@NotNull OpenListEntry o) {
        if (f == o.f) {
            return node - o.node;
        }
        return f - o.f;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OpenListEntry)) return false;
        final OpenListEntry other = (OpenListEntry) obj;

        return (f == other.f && node == other.node);
    }

    @Override
    public int hashCode() {
        int result = f;
        result = 29 * result + node;
        return result;
    }

    @Override
    public String toString() {
        return "OpenListEntry{node=" + node + ", f=" + f + '}';
    }

}
