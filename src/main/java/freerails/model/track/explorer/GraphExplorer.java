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

package freerails.model.track.explorer;

/**
 * Lets callers explore a graph without exposing its storage representation.
 * Vertices are packed into integers to avoid creating vertex objects.
 */
public interface GraphExplorer {

    /**
     * Returns the encoded vertex currently being explored.
     *
     * @return current vertex
     */
    int getPosition();

    /**
     * Sets the current vertex and resets edge iteration as defined by the
     * implementation.
     *
     * @param vertex encoded vertex to explore
     */
    void setPosition(int vertex);

    /**
     * Selects the next edge leaving the current vertex.
     *
     * @throws java.util.NoSuchElementException if no edge remains
     */
    void nextEdge();

    /**
     * Returns the vertex at the far end of the currently selected edge.
     *
     * @return encoded destination vertex
     */
    int getVertexConnectedByEdge();

    /**
     * Returns the cost of the currently selected edge.
     *
     * @return current edge cost
     */
    int getEdgeCost();

    /**
     * Tests whether an edge remains to be selected at the current vertex.
     *
     * @return {@code true} if another edge is available
     */
    boolean hasNextEdge();

    /**
     * Moves this GraphExplorer from the current vertex to the vertex that is
     * connected to the current vertex by the current edge.
     *
     * @throws IllegalStateException if no edge has yet been selected
     */
    void moveForward();

}