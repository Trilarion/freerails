package jfreerails.controller;


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
     * the vertex does not have another edge.*/
    void nextEdge();

    /** Returns the vertex that is connected to the current vertex by the current edge. */
    int getVertexConnectedByEdge();

    /** Returns the cost of the current edge.        */
    int getEdgeCost();

    boolean hasNextEdge();

    /** Moves this GraphExplorer from the current vertex to the vertex
     * that is connected to the current vertex by the current edge.
     */
    void moveForward();

    int getH();
}