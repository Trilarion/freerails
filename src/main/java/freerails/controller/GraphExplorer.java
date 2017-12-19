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

package freerails.controller;

/**
 * This interface lets the caller explorer a graph while hiding the way the
 * graph is stored. Vertices are packed into single ints to avoid the cost of
 * object creation and garbage collection.
 *
 * 24-Nov-2002
 *
 */
public interface GraphExplorer {
    /**
     * Return the current edge.
     * @return 
     */
    int getPosition();

    /**
     *
     * @param vertex
     */
    void setPosition(int vertex);

    /**
     * Sets the current edge to the current vertex's next edge. Throws a
     * NoSuchElementException if the vertex does not have another edge.
     */
    void nextEdge();

    /**
     * Returns the vertex that is connected to the current vertex by the current
     * edge.
     * @return 
     */
    int getVertexConnectedByEdge();

    /**
     * Returns the cost of the current edge.
     * @return 
     */
    int getEdgeCost();

    /**
     *
     * @return
     */
    boolean hasNextEdge();

    /**
     * Moves this GraphExplorer from the current vertex to the vertex that is
     * connected to the current vertex by the current edge.
     */
    void moveForward();

    /**
     *
     * @return
     */
    int getH();
}