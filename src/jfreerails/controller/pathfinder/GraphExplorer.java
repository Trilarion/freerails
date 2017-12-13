/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.controller.pathfinder;


/** This interface lets the caller explorer a graph while hiding the
 * way the graph is stored.  Vertices are packed into single ints
 * to avoid the cost of object creation and garbage collection.
 *
 * 24-Nov-2002
 * @author Luke Lindsay
 */
public interface GraphExplorer {
    void setPosition(int vertex);

    /** Return the current edge.        */
    int getPosition();

    /** Sets the current edge to the current vertex's next edge.  Throws a NoSuchElementException if
     * the vertex foes not have another edge.*/
    void nextEdge();

    /** Returns the vertex that is connected to the current vertex by the current edge. */
    int getVertexConnectedByEdge();

    /** Returns the length of the current edge        */
    int getEdgeLength();

    boolean hasNextEdge();

    /** Moves this GraphExplorer from the current vertex to the vertex
     * that is connected to the current vertex by the current edge.
     */
    void moveForward();
}